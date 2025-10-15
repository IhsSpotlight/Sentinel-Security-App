package com.sentinel.security

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sentinel.R

class AlertAdapter : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = listOf()

    fun setData(data: List<Alert>) {
        alerts = data
        notifyDataSetChanged()
    }

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timestamp: TextView = view.findViewById(R.id.timestamp)
        val camera: TextView = view.findViewById(R.id.camera)
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.timestamp.text = alert.timestamp
        holder.camera.text = alert.camera_id
        Glide.with(holder.itemView.context).load(alert.image_url).into(holder.image)
    }

    override fun getItemCount() = alerts.size
}
