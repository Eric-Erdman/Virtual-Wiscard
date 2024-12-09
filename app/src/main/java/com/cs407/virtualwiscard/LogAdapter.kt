package com.cs407.virtualwiscard
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(private val logList: List<String>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.log_item, parent, false)
        return LogViewHolder(view)
    }
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.textView.text = logList[position]
    }
    override fun getItemCount(): Int = logList.size
    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.logTextView)
    }
}