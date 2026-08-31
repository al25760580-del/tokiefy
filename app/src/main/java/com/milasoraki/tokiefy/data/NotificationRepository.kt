package com.milasoraki.tokiefy.data

/**
 * A dismissible in-app notification card shown above the inbox list.
 *
 * @property id          stable identifier for dismissal.
 * @property kind        dictates layout (followers banner vs activity card).
 * @property description secondary text used in [InboxNotification.Kind.ACTIVITY].
 */
public data class InboxNotification(
    val id: String,
    val kind: Kind,
    val description: String = "",
) {
    public enum class Kind { FOLLOWERS_BANNER, ACTIVITY }
}

/**
 * Repository of notifications and banners for the inbox screen.
 *
 * Why it exists:
 * The blue "new followers" banner and the pink "Activity" card come from
 * different endpoints. This repository composes them into a single list
 * the UI can iterate over, and owns the (currently in-memory) dismissal
 * state.
 */
public class NotificationRepository {
    /** Returns the initial list of notifications to display. */
    public fun initial(): List<InboxNotification> = listOf(
        InboxNotification(id = "followers-banner", kind = InboxNotification.Kind.FOLLOWERS_BANNER),
        InboxNotification(id = "activity", kind = InboxNotification.Kind.ACTIVITY, description = "1 new like"),
    )
}
