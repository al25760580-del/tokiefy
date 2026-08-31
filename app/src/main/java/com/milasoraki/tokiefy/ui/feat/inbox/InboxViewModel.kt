package com.milasoraki.tokiefy.ui.feat.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.data.ConversationRepository
import com.milasoraki.tokiefy.data.InboxNotification
import com.milasoraki.tokiefy.data.NotificationRepository
import com.milasoraki.tokiefy.data.StoryCircle
import com.milasoraki.tokiefy.data.StoryRepository
import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of the inbox screen state.
 *
 * @property conversations            conversation list (most recent first).
 * @property stories                  story circles for the top horizontal rail.
 * @property notifications            banners/cards above the list.
 * @property dismissedNotificationIds ids of notifications the user swiped away.
 * @property isLoading                true during the initial load.
 */
public data class InboxUiState(
    val conversations: List<Conversation> = emptyList(),
    val stories: List<StoryCircle> = emptyList(),
    val notifications: List<InboxNotification> = emptyList(),
    val dismissedNotificationIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
)

/** ViewModel for the inbox (messages) screen. */
public class InboxViewModel(
    private val conversationRepository: ConversationRepository = ServiceLocator.conversationRepository,
    private val storyRepository: StoryRepository = ServiceLocator.storyRepository,
    private val notificationRepository: NotificationRepository = ServiceLocator.notificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState(isLoading = true))
    public val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = InboxUiState(
                conversations = runCatching { conversationRepository.fetchConversations() }
                    .getOrDefault(emptyList()),
                stories = storyRepository.fetchStories(),
                notifications = notificationRepository.initial(),
            )
        }
    }

    /** Marks the notification with [id] as dismissed. */
    public fun dismissNotification(id: String) {
        _uiState.value = _uiState.value.copy(
            dismissedNotificationIds = _uiState.value.dismissedNotificationIds + id,
        )
    }
}
