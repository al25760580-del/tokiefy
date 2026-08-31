package com.milasoraki.tokiefy.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.milasoraki.tokiefy.R
import com.milasoraki.tokiefy.util.RelativeTime.RelativeTimeValue
import com.milasoraki.tokiefy.util.RelativeTime.Style
import com.milasoraki.tokiefy.util.RelativeTime.TimeUnitKey

/**
 * Render a decomposed relative-time value using locale resources.
 *
 * Keeping the resource lookup outside `util/RelativeTime` preserves the
 * pure-Kotlin, Android-free nature of the util module.
 */
@Composable
public fun RelativeTimeValue.format(): String {
    if (isJustNow) {
        return when (style) {
            Style.LONG -> stringResource(R.string.time_sent_moment)
            Style.SHORT -> stringResource(R.string.time_ago_moment)
            Style.AWEME_SHARE -> stringResource(R.string.time_shared_moment)
        }
    }
    val unit: String = when (unitKey) {
        TimeUnitKey.MINUTE -> stringResource(R.string.time_unit_minute)
        TimeUnitKey.HOUR -> stringResource(R.string.time_unit_hour)
        TimeUnitKey.DAY -> stringResource(R.string.time_unit_day)
    }
    return when (style) {
        Style.LONG -> stringResource(R.string.time_sent_pattern, count, unit)
        Style.SHORT -> stringResource(R.string.time_ago_pattern, count, unit)
        Style.AWEME_SHARE -> stringResource(R.string.time_shared_pattern, count, unit)
    }
}
