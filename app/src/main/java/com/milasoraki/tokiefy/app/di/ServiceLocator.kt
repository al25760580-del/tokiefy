package com.milasoraki.tokiefy.app.di

import com.milasoraki.tokiefy.data.ConversationRepository
import com.milasoraki.tokiefy.data.FeedRepository
import com.milasoraki.tokiefy.data.InteractionRepository
import com.milasoraki.tokiefy.data.MessageRepository
import com.milasoraki.tokiefy.data.NotificationRepository
import com.milasoraki.tokiefy.data.StickerRepository
import com.milasoraki.tokiefy.data.StoryRepository
import com.milasoraki.tokiefy.extractor.api.TikTokApi

/**
 * Manual service locator (dependency container).
 *
 * Why a hand-rolled locator instead of Hilt/Koin:
 *
 * | Option      | Pros                                   | Cons                                  |
 * |-------------|----------------------------------------|---------------------------------------|
 * | Hilt        | Generated, standard, scopes            | Heavy annotation processing; kapt     |
 * | Koin        | Lightweight, no kapt                   | Runtime errors on missing bindings    |
 * | Manual (this)| Zero dependencies; no magic; easy to read | Slightly more boilerplate on first add |
 *
 * Given the small codebase and the NewPipe-inspired philosophy of
 * explicit wiring, the manual locator keeps the dependency graph
 * visible at a glance.
 */
public object ServiceLocator {
    public val api: TikTokApi = TikTokApi.create()

    public val feedRepository: FeedRepository = FeedRepository(api.feed)
    public val conversationRepository: ConversationRepository = ConversationRepository(api.messaging)
    public val messageRepository: MessageRepository = MessageRepository(api.messaging)
    public val stickerRepository: StickerRepository = StickerRepository(api.messaging)
    public val storyRepository: StoryRepository = StoryRepository()
    public val notificationRepository: NotificationRepository = NotificationRepository()
    public val interactionRepository: InteractionRepository = InteractionRepository(api.digg, api.relation)
}
