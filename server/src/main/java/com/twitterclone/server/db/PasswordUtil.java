package com.twitterclone.server.db;

import org.mindrot.jbcrypt.BCrypt;

/**
 * ============================================================
 * Owner: AmirAli (Database) | Phase 1 - Day 3-5
 * ============================================================
 * Simple wrapper around the BCrypt library. Never store a raw (plain-text)
 * password in the database; always use hash(...) before INSERT and
 * check(...) during LOGIN.
 *
 * TODO(AmirAli):
 *  1. In UserDAO.insertUser, before saving: user.setPasswordHash(PasswordUtil.hash(rawPassword))
 *  2. In AuthHandler.handleLogin (or wherever Hesam calls it), to verify the password:
 *     PasswordUtil.check(rawPasswordFromClient, user.getPasswordHash())
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    /** Hashes a raw password (BCrypt generates the salt automatically). */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /** Compares the raw password entered by the user against the hash stored in the database. */
    public static boolean check(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
