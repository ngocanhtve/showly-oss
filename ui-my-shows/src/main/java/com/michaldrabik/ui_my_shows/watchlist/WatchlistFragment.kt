package com.michaldrabik.ui_my_shows.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.utilities.OnSortClickListener
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistAdapter
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment :
  BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSortClickListener {

  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

  private lateinit var adapter: WatchlistAdapter
  private lateinit var layoutManager: LinearLayoutManager
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyShowsComponentProvider).provideMyShowsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      loadShows()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = WatchlistAdapter().apply {
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      missingTranslationListener = { viewModel.loadMissingTranslation(it) }
      itemClickListener = { openShowDetails(it.show) }
      listChangeListener = { watchlistRecycler.scrollToPosition(0) }
    }
    watchlistRecycler.apply {
      setHasFixedSize(true)
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding = if (moviesEnabled) R.dimen.watchlistTabsViewPadding else R.dimen.watchlistTabsViewPaddingNoModes
    if (statusBarHeight != 0) {
      watchlistContent.updatePadding(top = watchlistContent.paddingTop + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(recyclerPadding))
      return
    }
    watchlistContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(recyclerPadding))
    }
  }

  private fun showSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      items?.let {
        val notifyChange = scrollToTop?.consume() == true
        adapter.setItems(it, notifyChange = notifyChange)
        watchlistEmptyView.fadeIf(it.isEmpty())
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it) }
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onSortClick(page: Int) = viewModel.loadSortOrder()

  override fun onScrollReset() {
    watchlistRecycler.scrollToPosition(0)
  }

  override fun onTraktSyncProgress() = viewModel.loadShows()
}
