package com.milasoraki.tokiefy.util

import java.security.MessageDigest

/**
 * MD5 and other digest helpers used by signing and body integrity code.
 *
 * Why this exists:
 * Multiple places (body `X-SS-STUB`, per-request `cdid`, cookie hashes)
 * need a lowercase hex MD5. A single helper keeps encoding consistent
 * and avoids scattering `MessageDigest.getInstance(...)` across files.
 */
public object Digest {
    /** Returns the lowercase hex MD5 digest of [input]. */
    public fun md5Hex(input: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
