package com.example.kotlinapp_2.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinapp_2.R
import com.example.kotlinapp_2.databinding.FragmentNoteListBinding
import com.example.kotlinapp_2.utils.showModernSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val notesViewModel: NotesViewModel by viewModels()

    private lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSwipeToDelete()
        setupFab()
        observeUiState()
        observeEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.notes)
        binding.toolbar.inflateMenu(R.menu.menu_notes)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sort_newest -> {
                    notesViewModel.onSortOptionChanged(NoteSortOption.NEWEST)
                    true
                }

                R.id.action_sort_oldest -> {
                    notesViewModel.onSortOptionChanged(NoteSortOption.OLDEST)
                    true
                }

                R.id.action_sort_title -> {
                    notesViewModel.onSortOptionChanged(NoteSortOption.TITLE)
                    true
                }

                else -> false
            }
        }

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_notes)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                notesViewModel.onSearchQueryChanged(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                notesViewModel.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onNoteClicked = { note ->
                val direction = NoteListFragmentDirections
                    .actionNoteListFragmentToAddEditNoteFragment(note.id)
                findNavController().navigate(direction)
            }
        )

        binding.recyclerView.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            val direction = NoteListFragmentDirections
                .actionNoteListFragmentToAddEditNoteFragment(-1)
            findNavController().navigate(direction)
        }
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = notesAdapter.currentList[viewHolder.bindingAdapterPosition]
                notesViewModel.onDeleteNote(note)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notesViewModel.uiState.collect { state ->
                    notesAdapter.submitList(state.notes)
                    binding.progressBar.isVisible = state.isLoading
                    binding.tvEmptyState.isVisible = !state.isLoading && state.notes.isEmpty()
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notesViewModel.events.collect { event ->
                    when (event) {
                        is NotesEvent.ShowMessage -> {
                            binding.root.showModernSnackbar(message = event.message)
                        }

                        is NotesEvent.ShowUndoDelete -> {
                            binding.root.showModernSnackbar(
                                message = event.message,
                                actionText = getString(R.string.undo),
                            ) {
                                    notesViewModel.onUndoDelete()
                                }
                        }
                    }
                }
            }
        }
    }
}
