package com.example.kotlinapp_2.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinapp_2.R
import com.example.kotlinapp_2.data.local.Note
import com.example.kotlinapp_2.databinding.ItemNoteBinding
import com.example.kotlinapp_2.utils.toReadableDate
import kotlin.math.abs

class NotesAdapter(
    private val onNoteClicked: (Note) -> Unit,
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvPreview.text = note.content
            binding.tvTimestamp.text = note.timestamp.toReadableDate()

            val palette = listOf(
                R.color.note_card_1,
                R.color.note_card_2,
                R.color.note_card_3,
                R.color.note_card_4,
                R.color.note_card_5,
            )
            val colorRes = palette[abs(note.id) % palette.size]
            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )

            val previewLines = 3 + (abs(note.id) % 4)
            binding.tvPreview.maxLines = previewLines

            binding.root.setOnClickListener {
                onNoteClicked(note)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }
}
