package com.uvg.gt.smartfridgeandroid

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children

import com.uvg.gt.smartfridgeandroid.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [Recipe].
 * TODO: Replace the implementation with code for your data type.
 */
class RecipeRecyclerViewAdapter(
    private val values: List<Recipe>
) : RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.reset()

        val item = values[position]
        holder.titleView.text = item.name

        for (tag in item.tags) {
            val tv = TextView(holder.context)
            tv.text = tag
            holder.tagsContainer.addView(tv)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val bannerView: ImageView = binding.cvBanner
        val titleView: TextView = binding.cvTitle
        val tagsContainer: LinearLayout = binding.cvTagContainer
        val context: Context? = binding.root.context

        fun reset() {
            tagsContainer.removeAllViews()
        }
    }

}