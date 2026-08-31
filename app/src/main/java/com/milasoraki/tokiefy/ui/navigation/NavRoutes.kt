package com.milasoraki.tokiefy.ui.navigation

/**
 * Type-safe route constants for the Jetpack Navigation graph.
 *
 * Why a dedicated object instead of string literals:
 * typos in route strings only surface at runtime. Constants centralise
 * them and make refactors safe. Helper functions like [chatRoute]
 * encode the argument shape so callers cannot forget to substitute it.
 */
public object NavRoutes {
    public const val HOME: String = "home"
    public const val FRIENDS: String = "friends"
    public const val INBOX: String = "inbox"
    public const val PROFILE: String = "profile"
    public const val CHAT: String = "chat/{conversationId}"

    /** Argument name used in the chat route. */
    public const val ARG_CONVERSATION_ID: String = "conversationId"

    /** Builds a concrete chat route for the given [conversationId]. */
    public fun chatRoute(conversationId: String): String = "chat/$conversationId"
}
