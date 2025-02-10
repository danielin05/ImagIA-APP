package com.example.myapplication.imagia_app

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val items: List<Pair<String, String>>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val (imageUriString, description) = items[position]

        val imageUri = Uri.parse(imageUriString)

        val bitmap = uriToBitmap(holder.imageView.context.contentResolver, imageUri)
        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap)
        } else {
            Log.e("HistoryAdapter", "Error: No se pudo cargar la imagen desde URI en la posición $position")
        }

        holder.textViewDescription.text = description
    }

    override fun getItemCount() = items.size


    private fun uriToBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
