package com.michaldrabik.ui_my_movies.watchlist.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import javax.inject.Inject

class WatchlistSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(watchlistMoviesSortBy = sortOrder))
  }

  suspend fun loadSortOrder() =
    settingsRepository.load().watchlistMoviesSortBy
}
