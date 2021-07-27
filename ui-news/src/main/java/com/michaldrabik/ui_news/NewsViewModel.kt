package com.michaldrabik.ui_news

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.BaseViewModel2
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_model.NewsItem.Type.MOVIE
import com.michaldrabik.ui_model.NewsItem.Type.SHOW
import com.michaldrabik.ui_news.cases.NewsLoadItemsCase
import com.michaldrabik.ui_news.recycler.NewsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
  private val loadNewsCase: NewsLoadItemsCase,
) : BaseViewModel2() {

  private var previousRefresh = 0L
  private var currentTypes: List<NewsItem.Type> = listOf(SHOW, MOVIE)

  private val itemsState = MutableLiveData<List<NewsListItem>>()
  private val loadingState = MutableLiveData<Boolean>()

  val uiLiveData = MediatorLiveData<NewsUiState>().apply {
    value = NewsUiState()
    addSource(itemsState) { value = value?.copy(items = it) }
    addSource(loadingState) { value = value?.copy(isLoading = it) }
  }

  init {
    loadItems()
  }

  fun loadItems(
    forceRefresh: Boolean = false,
    types: List<NewsItem.Type>? = null,
  ) {
    if (types != null) {
      currentTypes = types.toList()
      if (types.isEmpty()) {
        currentTypes = listOf(SHOW, MOVIE)
      }
    }

    if (forceRefresh && nowUtcMillis() - previousRefresh < TimeUnit.SECONDS.toMillis(30)) {
      loadingState.value = false
      return
    }

    viewModelScope.launch {
      val progressJob = launchDelayed(700) {
        loadingState.value = true
      }
      try {
        if (!forceRefresh) {
          val cachedItems = loadNewsCase.preloadItems(currentTypes)
          itemsState.value = cachedItems
        } else {
          loadingState.value = true
        }

        val items = loadNewsCase.loadItems(forceRefresh, currentTypes)
        itemsState.value = items
        loadingState.value = false

        if (forceRefresh) {
          previousRefresh = nowUtcMillis()
        }
      } catch (error: Throwable) {
        loadingState.value = false
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
        rethrowCancellation(error)
      } finally {
        progressJob.cancel()
      }
    }
  }
}
