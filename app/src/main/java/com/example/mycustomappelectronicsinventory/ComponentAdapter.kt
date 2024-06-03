package com.example.mycustomappelectronicsinventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComponentAdapter(
    private var components: List<Component>,
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<ComponentAdapter.ComponentViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    class ComponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.componentId)
        val name: TextView = itemView.findViewById(R.id.componentName)
        val category: TextView = itemView.findViewById(R.id.componentCategory)
        val type: TextView = itemView.findViewById(R.id.componentType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_component, parent, false)
        return ComponentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
        val component = components[position]
        holder.id.text = component.id
        holder.name.text = component.name
        holder.category.text = component.category
        holder.type.text = component.type

        holder.itemView.isSelected = selectedPosition == position
        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onItemClicked(component.id ?: "")
        }
    }

    override fun getItemCount(): Int {
        return components.size
    }

    fun updateComponents(newComponents: List<Component>) {
        components = newComponents
        notifyDataSetChanged()
    }

    fun getSelectedComponent(): Component? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            components[selectedPosition]
        } else {
            null
        }
    }

    fun clearSelection() {
        notifyItemChanged(selectedPosition)
        selectedPosition = RecyclerView.NO_POSITION
    }
}
