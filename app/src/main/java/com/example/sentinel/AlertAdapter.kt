package com.example.sentinel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// 1. Add a lambda function to the constructor for handling clicks
class AlertAdapter(private val onAlertClick: (Alert) -> Unit) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = emptyList()

    // ViewHolder remains the same
    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alertImage: ImageView = itemView.findViewById(R.id.icon_alert)
        val alertTimestamp: TextView = itemView.findViewById(R.id.icon_alert)
        val cameraId: TextView = itemView.findViewById(R.id.icon_alert)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentAlert = alerts[position]
        holder.alertTimestamp.text = currentAlert.timestamp
        holder.cameraId.text = "Camera: ${currentAlert.cameraid}"

        // Use Glide to load the thumbnail
        Glide.with(holder.itemView.context)
            .load(currentAlert.image_url)
            .placeholder(R.drawable.ic_launcher_background) // A placeholder image
            .into(holder.alertImage)

        // 2. Set the click listener on the item view
        holder.itemView.setOnClickListener {
            onAlertClick(currentAlert)
        }
    }

    override fun getItemCount() = alerts.size

    // Function to update the data in the adapter
    fun setData(newAlerts: List<Alert>) {
        this.alerts = newAlerts
        notifyDataSetChanged() // Reload the list
    }
}
