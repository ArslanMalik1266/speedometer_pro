package com.example.speedometerpro.presentation.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.speedometerpro.R
import com.example.speedometerpro.domain.model.SpeedHistory
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.util.HistoryListItem

class HistoryAdapter(
    private var speedUnit: SpeedUnit = SpeedUnit.KMH
) : ListAdapter<HistoryListItem, RecyclerView.ViewHolder>(HistoryDiffCallback()) {

    fun updateUnit(newUnit: SpeedUnit) {
        speedUnit = newUnit
        notifyDataSetChanged() // rebind views to reflect unit change
    }

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_HISTORY = 1

        fun formatDuration(millis: Long): String {
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return buildString {
                if (hours > 0) append("${hours} hr ")
                if (minutes > 0 || hours > 0) append("${minutes}m ")
                append("${seconds}s")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryListItem.DateHeader -> TYPE_DATE
            is HistoryListItem.SpeedItem -> TYPE_HISTORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item_date, parent, false)
                DateViewHolder(view)
            }
            TYPE_HISTORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item_layout, parent, false)
                SpeedViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryListItem.DateHeader -> (holder as DateViewHolder).bind(item)
            is HistoryListItem.SpeedItem -> (holder as SpeedViewHolder).bind(item.data, speedUnit)
        }
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.headerTitle)
        fun bind(item: HistoryListItem.DateHeader) {
            dateText.text = item.date
        }
    }

    class SpeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Value TextViews
        private val distanceValue: TextView = itemView.findViewById(R.id.distance_value)
        private val timeValue: TextView = itemView.findViewById(R.id.time_value)
        private val maxSpeedValue: TextView = itemView.findViewById(R.id.max_speed_value)
        private val avgSpeedValue: TextView = itemView.findViewById(R.id.avg_speed_value)
        private val currentSpeedValue: TextView = itemView.findViewById(R.id.distance_text_value)

        // Unit Label TextViews (The ones that were hardcoded)
        private val avgUnitLabel: TextView = itemView.findViewById(R.id.avg_speed_value_unit)
        private val maxUnitLabel: TextView = itemView.findViewById(R.id.max_speed_value_unit)
        private val currentUnitLabel: TextView = itemView.findViewById(R.id.distance_text_value_unit)

        fun bind(speed: SpeedHistory, unit: SpeedUnit) {
            val maxSpeed = convertSpeed(speed.max_speed, unit)
            val avgSpeed = convertSpeed(speed.avg_speed, unit)
            val currentSpeed = convertSpeed(speed.current_speed, unit)
            val distance = convertDistance(speed.distance, unit)

            val speedUnitText = if (unit == SpeedUnit.KMH) "km/h" else "mph"
            val distUnitText = if (unit == SpeedUnit.KMH) "km" else "mi"

            // 1. Update the Numbers
            maxSpeedValue.text = "%.1f".format(maxSpeed)
            avgSpeedValue.text = "%.1f".format(avgSpeed)
            currentSpeedValue.text = "%.1f".format(currentSpeed)

            // Format distance specifically
            distanceValue.text = if (distance < 1) "%.2f %s".format(distance, distUnitText)
            else "%.1f %s".format(distance, distUnitText)

            // 2. Update the dedicated Unit Labels
            avgUnitLabel.text = speedUnitText
            maxUnitLabel.text = speedUnitText
            currentUnitLabel.text = speedUnitText

            // 3. Update Time
            timeValue.text = formatDuration(speed.durationMillis)
        }

        private fun convertSpeed(value: Double, unit: SpeedUnit): Double {
            return if (unit == SpeedUnit.MPH) value * 0.621371 else value
        }

        private fun convertDistance(value: Double, unit: SpeedUnit): Double {
            // Value is already in KM from the database/usecase
            return if (unit == SpeedUnit.MPH) value * 0.621371 else value
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryListItem>() {
        override fun areItemsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
            return if (oldItem is HistoryListItem.DateHeader && newItem is HistoryListItem.DateHeader) {
                oldItem.date == newItem.date
            } else if (oldItem is HistoryListItem.SpeedItem && newItem is HistoryListItem.SpeedItem) {
                oldItem.data.id == newItem.data.id
            } else false
        }

        override fun areContentsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean {
            return oldItem == newItem
        }
    }
}