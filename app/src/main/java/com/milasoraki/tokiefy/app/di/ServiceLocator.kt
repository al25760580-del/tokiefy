package com.milasoraki.tokiefy.app.di

import android.content.Context
import android.webkit.CookieManager
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
 * Manual service locator.
 *
 * The single [com.milasoraki.tokiefy.extractor.remote.TiktokCookieJar]
 * lives on [api] (`api.cookieJar`) and is shared between the native
 * and web HTTP clients so cookies set by either lane (msToken, odin_tt,
 * ttwid) are replayed on subsequent requests — required for TikTok's
 * anti-bot challenge flow.
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
        api = TikTokApi.create(context = appContext, sessionHolder = sessionHolder)

        feedRepository = FeedRepository(api.feed, api.nativeFeed)
        conversationRepository = ConversationRepository(api.messaging)
        messageRepository = MessageRepository(api.messaging)
        stickerRepository = StickerRepository(api.messaging)
        storyRepository = StoryRepository()
        notificationRepository = NotificationRepository()
        interactionRepository = InteractionRepository(api.digg, api.relation)

        // Whenever sessionManager signals a new session (WebView login
        // completed or manual paste), merge its cookies into the shared
        // CookieJar so OkHttp starts sending them on the next request.
        appScope.launch {
            sessionManager.session.collectLatest { session ->
                sessionHolder.set(session)
                val raw = session.cookies
                    .distinctBy { it.name }
                    .joinToString("; ") { "${it.name}=${it.value}" }
                if (raw.isNotBlank()) api.cookieJar.mergeFromWebView(raw)
            }
        }
    }
}
