package com.uvg.gt.smartfridgeandroid

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.uvg.gt.smartfridgeandroid.databinding.FragmentIngredientItemBinding

/**
 * [RecyclerView.Adapter] that can display a [Ingredient].
 */
class IngredientRecyclerViewAdapter(
    private val values: List<Ingredient>
) : RecyclerView.Adapter<IngredientRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentIngredientItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.tvDate.text = item.date.toString()
        holder.tvName.text = item.name
        holder.tvQualifier.text = item.quantity.qualifier
        holder.tvQuantity.text = item.quantity.quantity.toString()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentIngredientItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvDate: TextView = binding.ingredientDate
        val tvName: TextView = binding.ingredientName
        val tvQuantity: TextView = binding.ingredientQuantity
        val tvQualifier: TextView = binding.ingredientQualifier

        override fun toString(): String {
            return super.toString() + " '" + tvName.text + "'"
        }
    }

}