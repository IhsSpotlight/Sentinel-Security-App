package com.example.sentinel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// The click listener is passed to the constructor
class AlertAdapter(private val onAlertClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = emptyList()

    // ✨ FIXED: This ViewHolder now uses the correct IDs from your XML file.
    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Correct ID for the ImageView is 'camera_thumbnail'
        val cameraThumbnail: ImageView = itemView.findViewById(R.id.camera_thumbnail)

        // Correct ID for the main title/name is 'camera_name'
        val cameraName: TextView = itemView.findViewById(R.id.camera_name)

        // The status text view ID is 'status_text'
        val statusText: TextView = itemView.findViewById(R.id.status_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        // This inflates your item_camera_card.xml layout file
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_camera_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentAlert = alerts[position]

        // ✨ FIXED: Bind data to the correct views.
        // We'll use the 'cameraid' to set the main text.
        holder.cameraName.text = "Camera: ${currentAlert.cameraid}"

        // Set the status text.
        holder.statusText.text = "Unknown" // This can be made dynamic later.

        // Use Glide to load an image from the URL.
        // Since your URL is a video stream, Glide will likely show an error or placeholder, which is expected.
        Glide.with(holder.itemView.context)
            .load(currentAlert.image_url)
            .placeholder(R.drawable.ic_launcher_background) // Fallback image while loading
            .error(R.drawable.ic_launcher_foreground)       // Image to show if URL is invalid or fails
            .into(holder.cameraThumbnail) // Load image into the correct ImageView

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener {
            onAlertClick(currentAlert)
        }
    }

    override fun getItemCount() = alerts.size

    // Function to update the adapter's data and refresh the RecyclerView
    fun setData(newAlerts: List<Alert>) {
        this.alerts = newAlerts
        notifyDataSetChanged() // This tells the RecyclerView to redraw itself
    }
}
