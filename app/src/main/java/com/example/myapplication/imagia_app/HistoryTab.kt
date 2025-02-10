package com.example.myapplication.imagia_app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class HistoryTab : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    val imageHistory = mutableListOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        historyAdapter = HistoryAdapter(imageHistory)
        recyclerView.adapter = historyAdapter

        loadImageHistory()
    }

    private fun loadImageHistory() {
        val sharedPreferences = requireContext().getSharedPreferences("image_data", Context.MODE_PRIVATE)
        val jsonString = sharedPreferences.getString("image_list", null)

        if (!jsonString.isNullOrEmpty()) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val uri = obj.getString("imageUri")
                val desc = obj.getString("description")
                imageHistory.add(Pair(uri, desc))
            }
        }

        historyAdapter.notifyDataSetChanged()
    }

    fun clearHistory() {
        imageHistory.clear()
        historyAdapter.notifyDataSetChanged()
    }

}
