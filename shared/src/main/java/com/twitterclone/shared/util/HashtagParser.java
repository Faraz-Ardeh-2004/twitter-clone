package com.twitterclone.shared.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts hashtags from tweet text. Shared so the server can persist them and
 * the client can highlight them consistently.
 *
 * A hashtag is '#' followed by one or more letters/digits/underscores. Tags are
 * returned lower-cased and de-duplicated, without the leading '#'.
 */
public final class HashtagParser {

    private static final Pattern HASHTAG = Pattern.compile("#(\\w+)");

    private HashtagParser() {
    }

    public static List<String> extract(String text) {
        Set<String> tags = new LinkedHashSet<>();
        if (text != null) {
            Matcher m = HASHTAG.matcher(text);
            while (m.find()) {
                tags.add(m.group(1).toLowerCase());
            }
        }
        return new ArrayList<>(tags);
    }
}
