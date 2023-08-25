package com.uvg.gt.smartfridgeandroid

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager

import com.uvg.gt.smartfridgeandroid.databinding.FragmentIngredientHolderBinding

/**
 * [RecyclerView.Adapter] that can display a [IngredientHolder].
 * TODO: Replace the implementation with code for your data type.
 */
class IngredientHolderRecyclerViewAdapter(
    private val values: List<IngredientHolder>
) : RecyclerView.Adapter<IngredientHolderRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentIngredientHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.tvTitle.text = item.category

        with(holder.rvList) {
            layoutManager = LinearLayoutManager(context)
            adapter = IngredientRecyclerViewAdapter(item.values)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentIngredientHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.ingredientHolderTitle
        val rvList: RecyclerView = binding.ingredientHolderList

        override fun toString(): String {
            return super.toString() + " '" + tvTitle.text + "'"
        }
    }

}