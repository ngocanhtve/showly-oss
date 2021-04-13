package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.ui_progress.ProgressItem
import javax.inject.Inject

@AppScope
class ProgressPinnedItemsCase @Inject constructor(
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  fun addPinnedItem(item: ProgressItem) = pinnedItemsRepository.addPinnedItem(item.show)

  fun removePinnedItem(item: ProgressItem) = pinnedItemsRepository.removePinnedItem(item.show)
}
