package com.example.sentinel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AlertAdapter(
    private val onViewClick: (Alert) -> Unit,       // plays camera stream
    private val onEditClick: (Alert) -> Unit        // edits camera name
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = emptyList()

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cameraThumbnail: ImageView = itemView.findViewById(R.id.camera_thumbnail)
        val cameraName: TextView = itemView.findViewById(R.id.btn_edit_camera_name)
        val statusText: TextView = itemView.findViewById(R.id.status_text)
        val editButton: ImageButton = itemView.findViewById(R.id.btn_live_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_camera_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentAlert = alerts[position]

        // Show camera name
        holder.cameraName.text = currentAlert.cameraName.ifEmpty {
            "Camera: ${currentAlert.cameraid}"
        }

        // Status text (can be dynamic later)
        holder.statusText.text = "Online"

        // Load image/thumbnail
        Glide.with(holder.itemView.context)
            .load(currentAlert.image_url)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.cameraThumbnail)

        // Click listeners
        holder.itemView.setOnClickListener {
            onViewClick(currentAlert)
        }
        holder.cameraName.setOnClickListener {
            onViewClick(currentAlert)
        }
        holder.editButton.setOnClickListener {
            onEditClick(currentAlert)
        }
    }

    override fun getItemCount() = alerts.size

    fun setData(newAlerts: List<Alert>) {
        this.alerts = newAlerts
        notifyDataSetChanged()
    }
}
