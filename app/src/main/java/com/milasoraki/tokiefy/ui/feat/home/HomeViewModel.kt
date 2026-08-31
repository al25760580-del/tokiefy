package com.milasoraki.tokiefy.ui.feat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.data.FeedRepository
import com.milasoraki.tokiefy.data.FeedResult
import com.milasoraki.tokiefy.extractor.model.feed.Aweme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of the home (For You) screen state.
 *
 * @param items        list of videos in view order.
 * @param isLoading    true while the initial page loads.
 * @param source       which backend produced [items] (live web, live native,
 *                     or mock fallback). Surfaced in a small chip so the user
 *                     can tell at a glance whether they are seeing their real
 *                     feed or sample data.
 * @param statusText   optional non-fatal warning (e.g. "live feed blocked,
 *                     showing samples").
 */
public data class HomeUiState(
    val items: List<Aweme> = emptyList(),
    val isLoading: Boolean = false,
    val source: FeedResult.Source = FeedResult.Source.MOCK,
    val statusText: String? = null,
)

/** ViewModel for the vertical video feed. */
public class HomeViewModel(
    private val feedRepository: FeedRepository = ServiceLocator.feedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    public val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    /** Re-fetches the feed (used e.g. after login completes). */
    public fun refresh() { load() }

    private fun load() {
        viewModelScope.launch {
            val result: FeedResult = feedRepository.fetchForYou()
            _uiState.value = HomeUiState(
                items = result.items,
                isLoading = false,
                source = result.source,
                statusText = result.errorMessage,
            )
        }
    }
}
