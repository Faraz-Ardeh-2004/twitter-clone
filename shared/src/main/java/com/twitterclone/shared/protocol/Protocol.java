package com.twitterclone.shared.protocol;

/**
 * Shared protocol-level constants so client and server agree on the same rules
 * (e.g. the tweet character limit is validated on both sides).
 */
public final class Protocol {

    /** Maximum tweet length, matching X/Twitter's classic limit. */
    public static final int MAX_TWEET_LENGTH = 280;

    /** Maximum number of images attachable to one tweet. */
    public static final int MAX_MEDIA_PER_TWEET = 4;

    /** Maximum base64 length for a single stored image (~2.5 MB raw). */
    public static final int MAX_MEDIA_BASE64_LENGTH = 3_500_000;

    private Protocol() {
    }
}
