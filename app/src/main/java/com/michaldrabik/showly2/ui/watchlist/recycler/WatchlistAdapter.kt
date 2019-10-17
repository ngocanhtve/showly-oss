package com.michaldrabik.showly2.ui.watchlist.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.ui.watchlist.views.WatchlistItemView

class WatchlistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<WatchlistItem> = mutableListOf()

  fun setItems(newItems: List<WatchlistItem>) {
    val diffCallback = WatchlistItemDiffCallback(items, newItems)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    this.items.apply {
      clear()
      addAll(newItems)
    }
    diffResult.dispatchUpdatesTo(this)
  }

  fun clearItems() {
    items.clear()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(WatchlistItemView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as WatchlistItemView).bind(items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}