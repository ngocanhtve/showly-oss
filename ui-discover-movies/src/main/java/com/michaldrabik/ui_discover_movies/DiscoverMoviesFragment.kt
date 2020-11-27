package com.michaldrabik.ui_discover_movies

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMoviesAdapter
import kotlinx.android.synthetic.main.fragment_discover_movies.*

class DiscoverMoviesFragment : BaseFragment<DiscoverMoviesViewModel>(R.layout.fragment_discover_movies), OnTabReselectedListener {

  override val viewModel by viewModels<DiscoverMoviesViewModel> { viewModelFactory }

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private lateinit var adapter: DiscoverMoviesAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiDiscoverMoviesComponentProvider).provideDiscoverMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onPause() {
    enableUi()
    super.onPause()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()
    setupSwipeRefresh()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      loadMovies()
    }
  }

  private fun setupView() {
    discoverMoviesSearchView.run {
      sortIconVisible = true
      settingsIconVisible = false
      isClickable = false
      onClick { navigateToSearch() }
//      onSortClickListener = { toggleFiltersView() }
//      translationY = searchViewPosition
    }
    discoverMoviesTabsView.run {
//      translationY = tabsViewPosition
      onModeSelected = { setMode(it) }
      animateMovies()
    }
  }

  private fun setupStatusBar() {
    discoverMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      discoverMoviesRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.discoverMoviesRecyclerPadding))
      (discoverMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
//      (discoverFiltersView.layoutParams as ViewGroup.MarginLayoutParams)
//        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.searchViewHeight))
      (discoverMoviesTabsView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.showsMoviesTabsMargin))
      discoverMoviesSwipeRefresh.setProgressViewOffset(
        true,
        swipeRefreshStartOffset + statusBarSize,
        swipeRefreshEndOffset
      )
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, Config.MAIN_GRID_SPAN)
    adapter = DiscoverMoviesAdapter().apply {
//      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
//      itemClickListener = { navigateToDetails(it) }
      listChangeListener = { discoverMoviesRecycler.scrollToPosition(0) }
    }
    discoverMoviesRecycler.apply {
      adapter = this@DiscoverMoviesFragment.adapter
      layoutManager = this@DiscoverMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupSwipeRefresh() {
    discoverMoviesSwipeRefresh.apply {
      val color = requireContext().colorFromAttr(R.attr.colorNotification)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setOnRefreshListener {
//        searchViewPosition = 0F
//        tabsViewPosition = 0F
        viewModel.loadMovies(pullToRefresh = true)
      }
    }
  }

  private fun navigateToSearch() {
    disableUi()
//    saveUi()
    hideNavigation()
//    discoverFiltersView.fadeOut()
    discoverMoviesRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverMoviesFragmentToSearchFragment, null)
    }
  }

  private fun render(uiModel: DiscoverMoviesUiModel) {
    uiModel.run {
      movies?.let {
        adapter.setItems(it, resetScroll == true)
        layoutManager.withSpanSizeLookup { pos -> adapter.getItems()[pos].image.type.spanSize }
        discoverMoviesRecycler.fadeIn()
      }
      showLoading?.let {
        discoverMoviesSearchView.isClickable = !it
        discoverMoviesSearchView.sortIconClickable = !it
        discoverMoviesSearchView.isEnabled = !it
        discoverMoviesSwipeRefresh.isRefreshing = it
        discoverMoviesTabsView.isEnabled = !it
      }
      filters?.let {
//        discoverMoviesFiltersView.run {
//          if (!this.isVisible) bind(it)
//        }
        discoverMoviesSearchView.iconBadgeVisible = !it.isDefault()
      }
    }
  }

  override fun onTabReselected() = navigateToSearch()
}