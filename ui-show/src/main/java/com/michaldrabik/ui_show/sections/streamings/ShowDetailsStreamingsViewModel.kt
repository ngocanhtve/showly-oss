package com.michaldrabik.ui_show.sections.streamings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.sections.streamings.ShowDetailsStreamingsUiState.StreamingsState
import com.michaldrabik.ui_show.sections.streamings.cases.ShowDetailsStreamingCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowDetailsStreamingsViewModel @Inject constructor(
  private val streamingCase: ShowDetailsStreamingCase,
) : ViewModel() {

  private lateinit var show: Show

  private val streamingsState = MutableStateFlow<StreamingsState?>(null)
  private val loadingState = MutableStateFlow(false)

  fun loadStreamings(show: Show) {
    if (this::show.isInitialized) return
    this.show = show
    viewModelScope.launch {
      loadingState.value = true
      try {
        val localStreamings = streamingCase.getLocalStreamingServices(show)
        streamingsState.value = StreamingsState(localStreamings, isLocal = true)

        val remoteStreamings = streamingCase.loadStreamingServices(show)
        streamingsState.value = StreamingsState(remoteStreamings, isLocal = false)
      } catch (error: Throwable) {
        streamingsState.value = StreamingsState(emptyList(), isLocal = false)
        rethrowCancellation(error)
      } finally {
        loadingState.value = false
      }
    }
  }

  val uiState = combine(
    streamingsState,
    loadingState
  ) { s1, _ ->
    ShowDetailsStreamingsUiState(
      streamings = s1
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = ShowDetailsStreamingsUiState()
  )
}
