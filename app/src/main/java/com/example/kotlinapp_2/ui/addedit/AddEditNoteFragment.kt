package com.example.kotlinapp_2.ui.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.kotlinapp_2.R
import com.example.kotlinapp_2.databinding.FragmentAddEditNoteBinding
import com.example.kotlinapp_2.utils.showModernSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditNoteFragment : Fragment() {

    private var _binding: FragmentAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val addEditNoteViewModel: AddEditNoteViewModel by viewModels()

    private var isInitialDataBound = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupEditorEnhancements()
        setupSaveButton()
        observeUiState()
        observeEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            addEditNoteViewModel.onSaveClicked(
                title = binding.etTitle.text?.toString().orEmpty(),
                content = binding.etContent.text?.toString().orEmpty(),
            )
        }
    }

    private fun setupEditorEnhancements() {
        binding.etTitle.doAfterTextChanged {
            updateEditorStatsAndSaveState()
        }
        binding.etContent.doAfterTextChanged {
            updateEditorStatsAndSaveState()
        }
        binding.btnClear.setOnClickListener {
            binding.etTitle.setText("")
            binding.etContent.setText("")
            updateEditorStatsAndSaveState()
        }
        updateEditorStatsAndSaveState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addEditNoteViewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.toolbar.title = getString(
                        if (state.isEditMode) R.string.edit_note else R.string.add_note
                    )

                    if (!isInitialDataBound && !state.isLoading) {
                        binding.etTitle.setText(state.title)
                        binding.etContent.setText(state.content)
                        isInitialDataBound = true
                        updateEditorStatsAndSaveState()
                    }
                }
            }
        }
    }

    private fun updateEditorStatsAndSaveState() {
        val title = binding.etTitle.text?.toString().orEmpty()
        val content = binding.etContent.text?.toString().orEmpty()
        val combinedText = "$title\n$content".trim()
        val charCount = combinedText.length
        val wordCount = if (combinedText.isBlank()) {
            0
        } else {
            combinedText.split(Regex("\\s+")).size
        }

        binding.tvStats.text = getString(R.string.editor_stats, charCount, wordCount)
        binding.btnSave.isEnabled = title.isNotBlank() && content.isNotBlank()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                addEditNoteViewModel.events.collect { event ->
                    when (event) {
                        is AddEditNoteEvent.NavigateBack -> {
                            findNavController().navigateUp()
                        }

                        is AddEditNoteEvent.ShowMessage -> {
                            binding.root.showModernSnackbar(message = event.message)
                        }
                    }
                }
            }
        }
    }
}
