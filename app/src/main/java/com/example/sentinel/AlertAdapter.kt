package com.example.sentinel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton // Import MaterialButton

// Note: We now inherit from ListAdapter
class AlertAdapter(
    private val onViewClick: (Alert) -> Unit,       // plays camera stream
    private val onEditClick: (Alert) -> Unit        // edits camera name
) : ListAdapter<Alert, AlertAdapter.AlertViewHolder>(AlertDiffCallback()) {

    // ViewHolder is mostly the same, but with corrected view types
    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cameraThumbnail: ImageView = itemView.findViewById(R.id.camera_thumbnail)
        // This is now a MaterialButton, not a TextView
        val editButton: MaterialButton = itemView.findViewById(R.id.btn_edit_camera_name)
        val statusText: TextView = itemView.findViewById(R.id.status_text)

        fun bind(alert: Alert) {
            // Set the button's text to the camera name
            editButton.text = alert.cameraName.ifEmpty {
                "Camera: ${alert.cameraid}"
            }
            statusText.text = "Online"

            // Load image/thumbnail
            Glide.with(itemView.context)
                .load(alert.image_url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(cameraThumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_camera_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentAlert = getItem(position)
        holder.bind(currentAlert) // Call the bind function

        // Click listeners
        holder.itemView.setOnClickListener {
            onViewClick(currentAlert)
        }
        // The edit button now handles the edit click
        holder.editButton.setOnClickListener {
            onEditClick(currentAlert)
        }
    }
}

// DiffUtil tells the ListAdapter how to calculate changes in the list
class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        // Use a unique ID for each camera
        return oldItem.cameraid == newItem.cameraid
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        // Check if the contents have changed
        return oldItem == newItem
    }
}
