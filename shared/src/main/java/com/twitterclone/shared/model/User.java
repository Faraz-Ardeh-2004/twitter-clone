package com.twitterclone.shared.model;

/**
 * ============================================================
 * Primary owner: AmirAli (Database) | Phase 1 - Day 3-5
 * (Faraz also uses this same class on the client side to display profiles)
 * ============================================================
 * Simple user model (POJO - no database logic in here!). Logic for
 * saving/reading from the database lives in UserDAO, not here.
 *
 * TODO(AmirAli): if you add a new column to the users table (e.g. bio,
 * avatarUrl for a profile phase), add the matching field here too so client
 * and server stay in sync. Any change here must be communicated to Faraz and
 * Hesam since this class is shared.
 */
public class User {

    private int id;
    private String username;
    private String email;

    /**
     * WARNING: this field is server-only in practice (the hashed password).
     * When sending a User to the client (e.g. in a GET_FEED or profile
     * response), never populate this field; otherwise the password hash
     * leaks to the client.
     * TODO(Hesam/AmirAli): make sure that in any DTO sent to the client,
     * passwordHash is always null / omitted.
     */
    private transient String passwordHash;

    public User() {
    }

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
