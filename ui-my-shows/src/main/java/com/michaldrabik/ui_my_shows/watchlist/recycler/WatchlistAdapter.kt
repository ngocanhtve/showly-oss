package com.michaldrabik.ui_my_shows.watchlist.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.NORMAL
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersGridView
import com.michaldrabik.ui_my_shows.filters.FollowedShowsFiltersView
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowCompactView
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowGridTitleView
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowGridView
import com.michaldrabik.ui_my_shows.watchlist.views.WatchlistShowView

class WatchlistAdapter(
  private val itemClickListener: (WatchlistListItem) -> Unit,
  private val itemLongClickListener: (WatchlistListItem) -> Unit,
  private val sortChipClickListener: (SortOrder, SortType) -> Unit,
  private val upcomingChipClickListener: (Boolean) -> Unit,
  private val missingImageListener: (WatchlistListItem, Boolean) -> Unit,
  private val missingTranslationListener: (WatchlistListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<WatchlistListItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_SHOW = 1
    private const val VIEW_TYPE_FILTERS = 2
  }

  override val asyncDiffer = AsyncListDiffer(this, WatchlistItemDiffCallback())

  var listViewMode: ListViewMode = NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_SHOW -> BaseMovieAdapter.BaseViewHolder(
        when (listViewMode) {
          NORMAL -> WatchlistShowView(parent.context)
          COMPACT -> WatchlistShowCompactView(parent.context)
          GRID -> WatchlistShowGridView(parent.context)
          GRID_TITLE -> WatchlistShowGridTitleView(parent.context)
        }.apply {
          itemClickListener = this@WatchlistAdapter.itemClickListener
          itemLongClickListener = this@WatchlistAdapter.itemLongClickListener
          missingImageListener = this@WatchlistAdapter.missingImageListener
          missingTranslationListener = this@WatchlistAdapter.missingTranslationListener
        }
      )
      VIEW_TYPE_FILTERS -> BaseMovieAdapter.BaseViewHolder(
        when (listViewMode) {
          NORMAL, COMPACT -> FollowedShowsFiltersView(parent.context).apply {
            onSortChipClicked = this@WatchlistAdapter.sortChipClickListener
            onFilterUpcomingClicked = this@WatchlistAdapter.upcomingChipClickListener
            isUpcomingChipVisible = true
          }
          GRID, GRID_TITLE -> FollowedShowsFiltersGridView(parent.context).apply {
            onSortChipClicked = this@WatchlistAdapter.sortChipClickListener
            onFilterUpcomingClicked = this@WatchlistAdapter.upcomingChipClickListener
            isUpcomingChipVisible = true
          }
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is WatchlistListItem.FiltersItem ->
        when (listViewMode) {
          NORMAL, COMPACT ->
            (holder.itemView as FollowedShowsFiltersView).bind(item.sortOrder, item.sortType, item.isUpcoming)
          GRID ->
            (holder.itemView as FollowedShowsFiltersGridView).bind(item.sortOrder, item.sortType, item.isUpcoming)
          GRID_TITLE ->
            (holder.itemView as FollowedShowsFiltersGridView).bind(item.sortOrder, item.sortType, item.isUpcoming)
        }
      is WatchlistListItem.ShowItem ->
        when (listViewMode) {
          NORMAL -> (holder.itemView as WatchlistShowView).bind(item)
          COMPACT -> (holder.itemView as WatchlistShowCompactView).bind(item)
          GRID -> (holder.itemView as WatchlistShowGridView).bind(item)
          GRID_TITLE -> (holder.itemView as WatchlistShowGridTitleView).bind(item)
        }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is WatchlistListItem.ShowItem -> VIEW_TYPE_SHOW
      is WatchlistListItem.FiltersItem -> VIEW_TYPE_FILTERS
      else -> throw IllegalStateException()
    }
}
