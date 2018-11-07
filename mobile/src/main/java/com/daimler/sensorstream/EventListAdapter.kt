package com.daimler.sensorstream

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daimler.sensorstream.events.TestEvent
import kotlinx.android.synthetic.main.item_event.view.*


class EventListAdapter : ListAdapter<TestEvent, EventListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(event: TestEvent) {
            itemView.event_name.text = event.name
            itemView.event_timestamp.text =
                    DateFormat.getTimeFormat(itemView.context).format(event.date)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TestEvent>() {
        override fun areItemsTheSame(oldItem: TestEvent?, newItem: TestEvent?): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TestEvent?, newItem: TestEvent?): Boolean {
            return oldItem == newItem
        }
    }

}