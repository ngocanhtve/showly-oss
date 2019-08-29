package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.michaldrabik.showly2.MainActivity
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.fadeOut
import com.michaldrabik.showly2.utilities.visibleIf
import com.michaldrabik.showly2.utilities.withSpanSizeLookup
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>() {

  override val layoutResId = R.layout.fragment_discover

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private lateinit var adapter: DiscoverAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(DiscoverViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    viewModel.loadTrendingShows()
    Log.d("STATE", savedInstanceState.toString())
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, gridSpan)
    adapter = DiscoverAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { openShowDetails(it) }
    discoverRecycler.apply {
      setHasFixedSize(true)
      adapter = this@DiscoverFragment.adapter
      layoutManager = this@DiscoverFragment.layoutManager
    }
  }

  private fun openShowDetails(item: DiscoverListItem) {
    animateItemsExit(item)
    (activity as MainActivity).hideNavigation()
  }

  private fun animateItemsExit(item: DiscoverListItem) {
    val clickedIndex = adapter.findItemIndex(item)
    (0..adapter.itemCount).forEach {
      if (it != clickedIndex) {
        val view = discoverRecycler.findViewHolderForAdapterPosition(it)
        view?.let { v ->
          val randomDelay = Random.nextLong(50, 200)
          v.itemView.fadeOut(duration = 180, startDelay = randomDelay)
        }
      }
    }
    val clickedView = discoverRecycler.findViewHolderForAdapterPosition(clickedIndex)
    clickedView?.itemView?.fadeOut(duration = 150, startDelay = 350, endAction = {
      findNavController().navigate(R.id.actionDiscoverFragmentToShowDetailsFragment)
    })
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.trendingShows?.let {
      adapter.setItems(it)
      layoutManager.withSpanSizeLookup { pos -> it[pos].type.spanSize }
    }
    uiModel.showLoading?.let { discoverProgress.visibleIf(it) }
    uiModel.updateListItem?.let { adapter.updateItem(it) }
  }
}
