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
import androidx.cardview.widget.CardView
import androidx.core.view.children

import com.uvg.gt.smartfridgeandroid.databinding.FragmentRecipeItemBinding

/**
 * [RecyclerView.Adapter] that can display a [Recipe].
 * TODO: Replace the implementation with code for your data type.
 */
class RecipeRecyclerViewAdapter(
    private val values: List<Recipe>
) : RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentRecipeItemBinding.inflate(
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

        holder.container.setOnClickListener {
            // TODO Add navigation to recipe details view
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentRecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val container: CardView = binding.recipeItemContainer
        val bannerView: ImageView = binding.cvBanner
        val titleView: TextView = binding.cvTitle
        val tagsContainer: LinearLayout = binding.cvTagContainer
        val context: Context? = binding.root.context

        fun reset() {
            tagsContainer.removeAllViews()
        }
    }

}