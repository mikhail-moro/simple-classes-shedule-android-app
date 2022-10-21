package com.example.lessonsschedule

// In this code "meeting" means: lesson, class. (рус. Пара, урок, занятие)

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView

// Custom RecycleAdapter that fills RecycleView with data of meetings
class CustomRecyclerAdapter(private val meetings: List<Meeting>) : RecyclerView
    .Adapter<CustomRecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val location: TextView = itemView.findViewById(R.id.location)
        val theme: TextView = itemView.findViewById(R.id.theme)
        val teacher: TextView = itemView.findViewById(R.id.teacher)
        val startTime: TextView = itemView.findViewById(R.id.start_time)
        val endTime: TextView = itemView.findViewById(R.id.end_time)
        val type: TextView = itemView.findViewById(R.id.type)

        val time: LinearLayout = itemView.findViewById(R.id.time)
        val info: LinearLayout = itemView.findViewById(R.id.info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.name.text = meeting.name
        holder.location.text = meeting.aud
        holder.theme.text = meeting.theme
        holder.teacher.text = meeting.teachers
        holder.startTime.text = meeting.startTime
        holder.endTime.text = meeting.endTime
        holder.type.text = meeting.type

        // Cause color of the meeting`s background comes from server,
        // necessary set back color using method setColorFilter
        holder.time.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            Color.parseColor(meeting.color),
            BlendModeCompat.SRC_ATOP
        )
        holder.info.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            Color.parseColor(meeting.color),
            BlendModeCompat.SRC_ATOP
        )
    }

    override fun getItemCount() = meetings.size
}