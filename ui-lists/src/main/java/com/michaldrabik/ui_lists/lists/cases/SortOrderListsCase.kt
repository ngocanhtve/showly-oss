package com.michaldrabik.ui_lists.lists.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import javax.inject.Inject

class SortOrderListsCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(listsSortBy = sortOrder))
  }

  suspend fun loadSortOrder() =
    settingsRepository.load().listsSortBy
}
