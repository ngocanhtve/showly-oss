package com.michaldrabik.ui_progress.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.cases.CalendarRatingsCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarRecentsCase
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_progress.main.ProgressMainUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
  private val recentsCase: CalendarRecentsCase,
  private val futureCase: CalendarFutureCase,
  private val ratingsCase: CalendarRatingsCase,
  private val imagesProvider: ShowImagesProvider,
  private val translationsRepository: TranslationsRepository,
) : BaseViewModel<CalendarUiModel>() {

  private val language by lazy { translationsRepository.getLanguage() }
  var isQuickRateEnabled = false
  var mode = CalendarMode.PRESENT_FUTURE

  private val _itemsLiveData = MutableLiveData<List<CalendarListItem>>()
  val itemsLiveData: LiveData<List<CalendarListItem>> get() = _itemsLiveData

  fun handleParentAction(model: ProgressMainUiModel) {
    mode = model.calendarMode ?: CalendarMode.PRESENT_FUTURE
    loadItems(model.searchQuery ?: "")
  }

  private fun loadItems(searchQuery: String = "") {
    viewModelScope.launch {
      val items = when (mode) {
        CalendarMode.PRESENT_FUTURE -> futureCase.loadItems(searchQuery)
        CalendarMode.RECENTS -> recentsCase.loadItems(searchQuery)
      }
      _itemsLiveData.postValue(items)
    }
  }

  fun addRating(rating: Int, bundle: EpisodeBundle) {
    viewModelScope.launch {
      try {
        ratingsCase.addRating(bundle.episode, rating)
        _messageLiveData.value = MessageEvent.info(R.string.textRateSaved)
        Analytics.logEpisodeRated(bundle.show.traktId, bundle.episode, rating)
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun findMissingImage(item: CalendarListItem, force: Boolean) {
    check(item is CalendarListItem.Episode)
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }

  fun findMissingTranslation(item: CalendarListItem) {
    check(item is CalendarListItem.Episode)
    if (item.translations?.show != null || language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = translationsRepository.loadTranslation(item.show, language)
        val translations = item.translations?.copy(show = translation)
        updateItem(item.copy(translations = translations))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "CalendarViewModel::findMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: CalendarListItem.Episode) {
    val currentItems = _itemsLiveData.value?.toMutableList() ?: mutableListOf()
    currentItems.findReplace(new) { it.isSameAs(new) }
    _itemsLiveData.postValue(currentItems)
  }

  fun checkQuickRateEnabled() {
    viewModelScope.launch {
      isQuickRateEnabled = ratingsCase.isQuickRateEnabled()
    }
  }
}
