package com.milasoraki.tokiefy.ui.feat.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milasoraki.tokiefy.app.di.ServiceLocator
import com.milasoraki.tokiefy.data.ConversationRepository
import com.milasoraki.tokiefy.data.MessageRepository
import com.milasoraki.tokiefy.data.StickerRepository
import com.milasoraki.tokiefy.extractor.model.messaging.Conversation
import com.milasoraki.tokiefy.extractor.model.messaging.DirectMessage
import com.milasoraki.tokiefy.extractor.model.messaging.MessageType
import com.milasoraki.tokiefy.extractor.model.sticker.Sticker
import com.milasoraki.tokiefy.extractor.model.sticker.StickerPack
import com.milasoraki.tokiefy.extractor.model.sticker.StickerStoreResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Snapshot of the chat screen state.
 *
 * @property conversationId        stable identifier of the opened chat.
 * @property conversation          conversation metadata (peer, online, unread).
 * @property messages              message list sorted in chronological order.
 * @property recentStickers        sticker row shown at the top of the panel.
 * @property savedStickerPacks     sticker packs available under "Saved".
 * @property isStickerPanelOpen    whether the sticker picker is visible.
 * @property draft                 text currently typed in the input field.
 * @property isLoading             true while the initial load is in flight.
 * @property error                 user-visible error message, if any.
 */
public data class ChatUiState(
    val conversationId: String = "",
    val conversation: Conversation? = null,
    val messages: List<DirectMessage> = emptyList(),
    val recentStickers: List<Sticker> = emptyList(),
    val savedStickerPacks: List<StickerPack> = emptyList(),
    val isStickerPanelOpen: Boolean = false,
    val draft: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel for a single 1-to-1 chat.
 *
 * The [SavedStateHandle] supplies the `conversationId` argument produced by
 * the navigation component so the screen never has to know how the
 * argument is encoded.
 */
public class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository = ServiceLocator.messageRepository,
    private val conversationRepository: ConversationRepository = ServiceLocator.conversationRepository,
    private val stickerRepository: StickerRepository = ServiceLocator.stickerRepository,
) : ViewModel() {

    private val conversationId: String = requireNotNull(savedStateHandle["conversationId"]) {
        "ChatViewModel requires a conversationId argument"
    }

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(conversationId = conversationId, isLoading = true))
    public val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val messages: List<DirectMessage> =
                runCatching { messageRepository.fetchMessages(conversationId) }
                    .getOrDefault(emptyList())
            val conversations: List<Conversation> =
                runCatching { conversationRepository.fetchConversations() }
                    .getOrDefault(emptyList())
            val conversation: Conversation? =
                conversations.firstOrNull { it.conversationId == conversationId }
            val store: StickerStoreResponse =
                runCatching { stickerRepository.fetchStore() }
                    .getOrDefault(StickerStoreResponse())
            _uiState.value = ChatUiState(
                conversationId = conversationId,
                conversation = conversation,
                messages = messages,
                recentStickers = store.recentStickers,
                savedStickerPacks = store.savedPacks,
                isLoading = false,
            )
        }
    }

    /** Replaces the current draft text. */
    public fun updateDraft(text: String) {
        _uiState.value = _uiState.value.copy(draft = text)
    }

    /** Toggles the sticker panel. Also hides the keyboard in the UI layer. */
    public fun toggleStickerPanel(open: Boolean) {
        _uiState.value = _uiState.value.copy(isStickerPanelOpen = open)
    }

    /**
     * Sends the current [ChatUiState.draft] as a text message.
     *
     * An optimistic local message is appended immediately so the user sees
     * the message while the network call is in flight. On failure the
     * repository will surface a retry flow in a follow-up; for now errors
     * are swallowed to keep the mock usable.
     */
    public fun sendDraftText() {
        val text: String = _uiState.value.draft.trim()
        if (text.isEmpty()) return
        val optimistic = DirectMessage(
            messageId = "local_${System.nanoTime()}",
            conversationId = conversationId,
            senderUid = SENDER_SELF,
            type = MessageType.TEXT,
            content = text,
            stickerId = null,
            mediaUrl = null,
            createTimeEpochSeconds = System.currentTimeMillis() / 1000,
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + optimistic,
            draft = "",
            isStickerPanelOpen = false,
        )
        viewModelScope.launch {
            runCatching { messageRepository.sendText(conversationId, text) }
        }
    }

    /** Sends a sticker with optimistic local echo, analogous to [sendDraftText]. */
    public fun sendSticker(sticker: Sticker) {
        val optimistic = DirectMessage(
            messageId = "local_stk_${System.nanoTime()}",
            conversationId = conversationId,
            senderUid = SENDER_SELF,
            type = MessageType.STICKER,
            content = sticker.url,
            stickerId = sticker.stickerId,
            createTimeEpochSeconds = System.currentTimeMillis() / 1000,
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + optimistic,
        )
        viewModelScope.launch {
            runCatching {
                messageRepository.sendSticker(conversationId, sticker.stickerId, sticker.url)
            }
        }
    }

    public companion object {
        /**
         * Synthetic sender UID used for locally-echoed messages while the
         * real session is not yet authenticated. It is stable across
         * recompositions so `LazyColumn` keys remain valid.
         */
        public const val SENDER_SELF: String = "me"
    }
}
