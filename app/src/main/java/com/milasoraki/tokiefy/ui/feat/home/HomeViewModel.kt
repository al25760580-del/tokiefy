package com.milasoraki.tokiefy.ui.feat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.data.FeedRepository
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of the home (For You) screen state.
 *
 * @param items     list of videos in view order.
 * @param isLoading true while the initial page loads.
 */
public data class HomeUiState(
    val items: List<Aweme> = emptyList(),
    val isLoading: Boolean = false,
)

/** ViewModel for the vertical video feed. */
public class HomeViewModel(
    private val feedRepository: FeedRepository = ServiceLocator.feedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    public val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                items = runCatching { feedRepository.fetchForYou() }.getOrDefault(emptyList()),
                isLoading = false,
            )
        }
    }
}
