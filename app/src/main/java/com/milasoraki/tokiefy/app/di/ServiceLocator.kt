package com.milasoraki.tokiefy.app.di

import android.content.Context
import com.milasoraki.tokiefy.data.ConversationRepository
import com.milasoraki.tokiefy.data.FeedRepository
import com.milasoraki.tokiefy.data.InteractionRepository
import com.milasoraki.tokiefy.data.MessageRepository
import com.milasoraki.tokiefy.data.NotificationRepository
import com.milasoraki.tokiefy.data.StickerRepository
import com.milasoraki.tokiefy.data.StoryRepository
import com.milasoraki.tokiefy.extractor.api.SessionHolder
import com.milasoraki.tokiefy.extractor.api.SessionManager
import com.milasoraki.tokiefy.extractor.api.TikTokApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Manual service locator (dependency container).
 *
 * Why a hand-rolled locator instead of Hilt/Koin:
 *
 * | Option        | Pros                                    | Cons                                  |
 * |---------------|-----------------------------------------|---------------------------------------|
 * | Hilt          | Standard, scoped, compile-time-checked  | Heavy annotation processing; kapt     |
 * | Koin          | Lightweight, no kapt                    | Runtime errors on missing bindings    |
 * | Manual (this) | Zero dependencies; no magic; easy to read | More boilerplate when adding deps |
 *
 * Given the small codebase and the NewPipe-inspired philosophy of
 * explicit wiring, the manual locator keeps the dependency graph
 * visible at a glance.
 *
 * Lifecycle: [init] is called exactly once from [TokiefyApp.onCreate].
 * The coroutine scope immediately starts observing the [SessionManager]
 * so [SessionHolder] is always up-to-date. This means logging in or
 * out takes effect on the next API call without app restart.
 */
public object ServiceLocator {

    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    public lateinit var appContext: Context
        private set
    public lateinit var sessionManager: SessionManager
        private set
    public lateinit var sessionHolder: SessionHolder
        private set
    public lateinit var api: TikTokApi
        private set

    public lateinit var feedRepository: FeedRepository
        private set
    public lateinit var conversationRepository: ConversationRepository
        private set
    public lateinit var messageRepository: MessageRepository
        private set
    public lateinit var stickerRepository: StickerRepository
        private set
    public lateinit var storyRepository: StoryRepository
        private set
    public lateinit var notificationRepository: NotificationRepository
        private set
    public lateinit var interactionRepository: InteractionRepository
        private set

    public fun init(context: Context, manager: SessionManager) {
        appContext = context.applicationContext
        sessionManager = manager
        sessionHolder = SessionHolder()
        api = TikTokApi.create(sessionHolder = sessionHolder)

        feedRepository = FeedRepository(api.feed, api.webFeed)
        conversationRepository = ConversationRepository(api.messaging)
        messageRepository = MessageRepository(api.messaging)
        stickerRepository = StickerRepository(api.messaging)
        storyRepository = StoryRepository()
        notificationRepository = NotificationRepository()
        interactionRepository = InteractionRepository(api.digg, api.relation)

        appScope.launch {
            sessionManager.session.collectLatest { session -> sessionHolder.set(session) }
        }
    }
}
