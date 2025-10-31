package com.neuralic.voicecrm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralic.voicecrm.R
import com.neuralic.voicecrm.data.AppDatabase
import com.neuralic.voicecrm.data.ActionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var emptyText: TextView
    private val adapter = LogAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_log, container, false)
        rv = v.findViewById(R.id.rv_logs)
        emptyText = v.findViewById(R.id.empty_text)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadLogs()
        return v
    }

    private fun loadLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(requireContext())
            val logs = db.actionLogDao().getAll()
            withContext(Dispatchers.Main) {
                adapter.submitList(logs)
                emptyText.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}

class LogAdapter : RecyclerView.Adapter<LogViewHolder>() {
    private var items: List<ActionLog> = emptyList()
    fun submitList(list: List<ActionLog>) {
        items = list
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(v)
    }
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(items[position])
    }
    override fun getItemCount(): Int = items.size
}
class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<TextView>(R.id.log_title)
    private val subtitle = view.findViewById<TextView>(R.id.log_sub)
    private val ts = view.findViewById<TextView>(R.id.log_ts)
    fun bind(item: ActionLog) {
        title.text = item.type
        subtitle.text = item.summary
        ts.text = item.timestamp
    }
}
