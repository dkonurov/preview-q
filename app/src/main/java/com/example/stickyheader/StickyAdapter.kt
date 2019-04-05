package com.example.stickyheader

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.holder_sticky.view.*

class StickyAdapter : RecyclerView.Adapter<StickyHeaderHolder>(), StickyHeaderLayoutManager.StickyHeaderListener {


    private val list: List<Boolean> = (0..100).asSequence().map { it % 5 == 0 }.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickyHeaderHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.holder_sticky, parent, false)
        return StickyHeaderHolder(view)
    }

    override fun isSticky(position: Int) = position % 5 == 0

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: StickyHeaderHolder, position: Int) {
        val context = holder.itemView.context
        val color = if (list[position]) {
            if (position % 10 == 0) {
                ContextCompat.getColor(context, R.color.colorPrimary)
            } else {
                ContextCompat.getColor(context, R.color.colorAccent)
            }
        } else {
            ContextCompat.getColor(context, R.color.colorPrimaryDark)
        }
        holder.itemView.setBackgroundColor(color)
        holder.itemView.text.text = "$position"
    }
}