package com.example.stickyheader

import android.content.Context
import android.support.annotation.Px
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout


class StickyHeaderLayoutManager(
    context: Context,
    private val listener: StickyHeaderListener
) : LinearLayoutManager(context, VERTICAL, false) {

    private val headerPositions: MutableList<Int> = mutableListOf()

    private var header: View? = null

    private var nextHeader: View? = null

    private var recyclerView: RecyclerView? = null

    private var currentPosition: Int? = null

    private val recyclerParent: ViewGroup?
        get() = (recyclerView?.parent as ViewGroup?)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        headerPositions.clear()
        (0..itemCount).asSequence().filter { listener.isSticky(it) }.forEach {
            headerPositions.add(it)
        }
        super.onLayoutChildren(recycler, state)
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        changeGraph(view)
        recyclerView = view
    }

    private fun changeGraph(view: RecyclerView) {
        val params = view.layoutParams
        val parent = view.parent
        if (
            parent !is FrameLayout
            || params.width != MarginLayoutParams.MATCH_PARENT
            || params.height != MarginLayoutParams.MATCH_PARENT
        ) {
            val group = parent as ViewGroup
            for (i in 0..group.childCount) {
                if (group.getChildAt(i) == view) {
                    val layout = FrameLayout(view.context)
                    layout.layoutParams = view.layoutParams
                    val frameParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    group.removeViewAt(i)
                    group.addView(layout, i)
                    view.layoutParams = frameParams
                    layout.addView(view)
                    break
                }
            }
        }
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)
        recyclerView = null
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        updateHeader()
        val scroll = super.scrollVerticallyBy(dy, recycler, state)
        if (Math.abs(scroll) > 0) {
            translateHeader()
        }
        return scroll
    }

    private fun translateHeader() {
        val header = header
        val nextHeader = nextHeader
        if (header != null && nextHeader != null) {
            if (header.height == 0) {
                header.visibility = View.INVISIBLE
                recyclerView?.post {
                    header.visibility = View.VISIBLE
                    translateHeader()
                }
                return
            }
            if (nextHeader.y < header.height) {
                val offset = (nextHeader.y - header.height)
                header.translationY = offset - header.top
            }
        }
    }

    private fun updateHeader() {
        val index = findStickPositon(findFirstVisibleItemPosition()) ?: return
        val position = headerPositions[index]

        if (currentPosition != position) {
            header?.dettachFromHeader()
            attachHeader(position)
            currentPosition = position
            if (headerPositions.size - 1 > index) {
                nextHeader = findViewByPosition(headerPositions[index + 1])
            } else {
                nextHeader = null
            }
        }
    }

    private fun View.dettachFromHeader() {
        recyclerParent?.removeView(this)
    }

    private fun View.attachToParent() {
        recyclerParent?.addView(this)
    }

    private fun attachHeader(position: Int) {
        header = recyclerView?.let {
            val adapter = it.adapter
            if (adapter != null) {
                val holder = adapter.createViewHolder(it, adapter.getItemViewType(position))
                adapter.bindViewHolder(holder, position)
                holder.itemView
            } else {
                null
            }
        }
        header?.attachToParent()
        header?.requestLayout()
        header?.let(this::updateLayoutParams)
    }

    private fun findStickPositon(position: Int): Int? {
        val index = headerPositions.indexOfLast { it <= position }
        return if (index < 0) {
            null
        } else {
            index
        }
    }

    private fun updateLayoutParams(currentHeader: View) {
        val params = currentHeader.layoutParams as MarginLayoutParams
        matchMarginsToPadding(params)
    }

    private fun matchMarginsToPadding(layoutParams: MarginLayoutParams) {
        recyclerView?.let { recyclerView ->
            @Px val leftMargin = if (orientation == LinearLayoutManager.VERTICAL)
                recyclerView.paddingLeft
            else
                0
            @Px val topMargin = if (orientation == LinearLayoutManager.VERTICAL)
                0
            else
                recyclerView.paddingTop
            @Px val rightMargin = if (orientation == LinearLayoutManager.VERTICAL)
                recyclerView.paddingRight
            else
                0
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0)
        }
    }


    interface StickyHeaderListener {

        fun isSticky(position: Int): Boolean
    }
}