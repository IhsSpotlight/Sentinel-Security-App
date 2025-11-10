package com.example.sentinel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Adapter for displaying both stored alerts and automatically discovered IP cameras.
 */
class AlertAdapter(
    private val onAlertClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = emptyList()

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cameraThumbnail: ImageView = itemView.findViewById(R.id.camera_thumbnail)
        val cameraName: TextView = itemView.findViewById(R.id.camera_name)
        val statusText: TextView = itemView.findViewById(R.id.status_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_camera_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentAlert = alerts[position]

        // Show camera name (or IP if discovered)
        holder.cameraName.text = currentAlert.cameraid ?: "Unknown Camera"

        // Show status if available
        holder.statusText.text = currentAlert.status ?: "Online"

        // Try to show snapshot/thumbnail if available
        Glide.with(holder.itemView.context)
            .load(currentAlert.image_url)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.cameraThumbnail)

        // Click listener
        holder.itemView.setOnClickListener { onAlertClick(currentAlert) }
    }

    override fun getItemCount() = alerts.size

    /**
     * Update the list of cameras or alerts.
     */
    fun setData(newAlerts: List<Alert>) {
        this.alerts = newAlerts
        notifyDataSetChanged()
    }
}
