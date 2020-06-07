package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.MessageEvent
import com.michaldrabik.showly2.utilities.extensions.findReplace
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor,
  private val showsRepository: ShowsRepository
) : BaseViewModel<WatchlistUiModel>() {

  fun loadWatchlist() {
    viewModelScope.launch {
      val shows = showsRepository.myShows.loadAll()
      interactor.preloadEpisodes(shows.map { it.traktId })

      val items = shows.map { show ->
        async {
          val item = interactor.loadWatchlistItem(show)
          val image = interactor.findCachedImage(show, POSTER)
          item.copy(image = image)
        }
      }.awaitAll()
        .filter { it.episodesCount != 0 && it.episode.firstAired != null }
        .groupBy { it.episode.hasAired(it.season) }

      val aired = (items[true] ?: emptyList())
        .sortedWith(compareByDescending<WatchlistItem> { it.isNew() }.thenBy { it.show.title.toLowerCase() })
      val notAired = (items[false] ?: emptyList())
        .sortedBy { it.episode.firstAired?.toInstant()?.toEpochMilli() }

      val allItems = (aired + notAired).toMutableList()

      val headerIndex = allItems.indexOfFirst { !it.isHeader() && !it.episode.hasAired(it.season) }
      if (headerIndex != -1) {
        val item = allItems[headerIndex]
        allItems.add(headerIndex, item.copy(headerTextResId = R.string.textWatchlistIncoming))
      }

      uiState = WatchlistUiModel(allItems)
    }
  }

  fun setWatchedEpisode(item: WatchlistItem) {
    viewModelScope.launch {
      if (!item.episode.hasAired(item.season)) {
        _messageLiveData.value = MessageEvent.info(R.string.errorEpisodeNotAired)
        return@launch
      }
      interactor.setEpisodeWatched(item)
      loadWatchlist()
    }
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
