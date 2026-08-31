package com.milasoraki.tokiefy.util

import java.util.concurrent.TimeUnit

/**
 * Human-readable relative time formatting.
 *
 * Why this exists:
 * Relative time strings ("Sent 2 min ago", "shared a video · 1 d") appear in
 * multiple places (message list, chat detail, activity notifications).
 * Centralizing the formatting keeps wording consistent across screens and
 * lets us pick an appropriate phrasing for each context (style).
 *
 * Input: a non-negative number of seconds measured relative to now.
 * Output: a [RelativeTimeValue] carrying a pluralisable quantity plus a
 * [Style] token; the UI layer combines it with locale strings from
 * `strings.xml`. Nothing in this module references `Context` or
 * `stringResource()` so it stays unit-testable.
 */
public object RelativeTime {

    private val MINUTE_SECONDS: Long = 60L
    private val HOUR_SECONDS: Long = TimeUnit.HOURS.toSeconds(1)
    private val DAY_SECONDS: Long = TimeUnit.DAYS.toSeconds(1)

    /**
     * Phrasing style that matches the surrounding UI copy.
     */
    public enum class Style {
        /** Long form used for the first item in the message list (e.g. "Sent N min ago"). */
        LONG,
        /** Short form used elsewhere (e.g. "N h ago"). */
        SHORT,
        /** Aweme/video share prefixed with "shared a video · N". */
        AWEME_SHARE,
    }

    /**
     * A decomposed relative-time value ready to be rendered by the UI.
     *
     * @property style   phrasing style (LONG / SHORT / AWEME_SHARE).
     * @property unitKey which time unit the [count] represents (minute, hour, day).
     * @property count   number of [unitKey]s elapsed. Will be 0 for the "just now" case.
     * @property isJustNow true when the elapsed time is below the smallest unit.
     */
    public data class RelativeTimeValue(
        val style: Style,
        val unitKey: TimeUnitKey,
        val count: Long,
        val isJustNow: Boolean,
    )

    /** Time unit used for pluralisation in the UI layer. */
    public enum class TimeUnitKey { MINUTE, HOUR, DAY }

    /**
     * Decompose [seconds] into a [RelativeTimeValue].
     *
     * @param seconds non-negative elapsed seconds.
     * @param style   phrasing requested by the caller.
     */
    public fun breakdown(seconds: Long, style: Style = Style.LONG): RelativeTimeValue {
        val nonNegative: Long = seconds.coerceAtLeast(0)
        return when {
            nonNegative < MINUTE_SECONDS -> RelativeTimeValue(style, TimeUnitKey.MINUTE, 0, isJustNow = true)
            nonNegative < HOUR_SECONDS -> RelativeTimeValue(style, TimeUnitKey.MINUTE, nonNegative / MINUTE_SECONDS, isJustNow = false)
            nonNegative < DAY_SECONDS -> RelativeTimeValue(style, TimeUnitKey.HOUR, nonNegative / HOUR_SECONDS, isJustNow = false)
            nonNegative < 2 * DAY_SECONDS -> RelativeTimeValue(style, TimeUnitKey.DAY, 1, isJustNow = false)
            else -> RelativeTimeValue(style, TimeUnitKey.DAY, nonNegative / DAY_SECONDS, isJustNow = false)
        }
    }
}
