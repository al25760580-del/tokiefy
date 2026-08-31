package com.milasoraki.tokiefy.data

/**
 * A "story circle" that appears in the top row of the inbox.
 *
 * @property id              stable identifier for list keys.
 * @property avatarUrl       profile picture / story thumbnail.
 * @property label          caption shown under the circle.
 * @property isComposeButton true for the leading "Your story" tile with a
 *                           "+" badge.
 * @property isOnline        whether to show the online (green) dot.
 * @property hasFollowBadge  whether to show the add-person badge beneath
 *                           the avatar.
 */
public data class StoryCircle(
    val id: String,
    val avatarUrl: String,
    val label: String,
    val isComposeButton: Boolean = false,
    val isOnline: Boolean = false,
    val hasFollowBadge: Boolean = false,
)

/**
 * Produces the story/friends row for the top of the inbox.
 *
 * Why it exists:
 * The top row mixes the user's own "Your story" tile with friends that
 * have posted stories AND recent contacts. The repository isolates the
 * (currently mocked) data source so later we can swap in the real
 * `/aweme/v1/im/story/` endpoint without touching the UI.
 */
public class StoryRepository {
    /** Returns the ordered list of circles for the horizontal rail. */
    public fun fetchStories(): List<StoryCircle> {
        return listOf(
            StoryCircle(
                id = "me",
                avatarUrl = "https://i.pravatar.cc/100?u=me",
                label = "Your story",
                isComposeButton = true,
            ),
            StoryCircle(id = "s1", avatarUrl = "https://i.pravatar.cc/100?u=a", label = "misky"),
            StoryCircle(id = "s2", avatarUrl = "https://i.pravatar.cc/100?u=b", label = "riana", isOnline = true, hasFollowBadge = true),
            StoryCircle(id = "s3", avatarUrl = "https://i.pravatar.cc/100?u=c", label = "sebas"),
            StoryCircle(id = "s4", avatarUrl = "https://i.pravatar.cc/100?u=d", label = "lu"),
            StoryCircle(id = "s5", avatarUrl = "https://i.pravatar.cc/100?u=e", label = "tay", isOnline = true),
        )
    }
}
