package com.twitterclone.server.handler;

import com.google.gson.JsonObject;
import com.twitterclone.server.auth.Authenticator;
import com.twitterclone.server.db.PasswordUtil;
import com.twitterclone.server.db.UserDAO;
import com.twitterclone.server.network.ClientHandler;
import com.twitterclone.server.network.ConnectionRegistry;
import com.twitterclone.shared.model.User;
import com.twitterclone.shared.protocol.Packet;
import com.twitterclone.shared.protocol.PacketType;

import java.sql.SQLException;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 1 - Day 3-5
 * ============================================================
 * Business logic for REGISTER / LOGIN / LOGOUT. This class is the bridge
 * between Dispatcher and UserDAO/Authenticator. There should be no raw SQL
 * here (that's UserDAO's job).
 *
 * Expected payload shape for each request (coordinate with Faraz):
 *   REGISTER -> { "username": "...", "email": "...", "password": "..." }
 *   LOGIN    -> { "username": "...", "password": "..." }
 *   LOGOUT   -> {} (the token is read from packet.getToken() itself)
 */
public class AuthHandler {

    private static final UserDAO userDAO = new UserDAO();

    private AuthHandler() {
    }

    /**
     * TODO(Hesam):
     *  1. Read username/email/password from request.getPayload().
     *  2. Basic validation (not empty, password length) - you can validate on
     *     both the client (Faraz) and here (server, the real source of truth).
     *  3. Build a new User, hash the password with PasswordUtil.hash(...).
     *  4. Call userDAO.insertUser(user); if it throws SQLException due to a
     *     UNIQUE constraint (duplicate username/email), return a Packet.error.
     *  5. On success return Packet.ok(PacketType.REGISTER, payload) (the
     *     payload can include the new user's id).
     */
    public static Packet handleRegister(Packet request) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.REGISTER, "Missing payload");
        }

        // TODO(Hesam): full implementation per the guide above.
        throw new UnsupportedOperationException("TODO(Hesam): implement handleRegister in Phase 1");
    }

    /**
     * TODO(Hesam):
     *  1. Read username/password from the payload.
     *  2. Call userDAO.getUserByUsername(username); if null -> error "user not found".
     *  3. Check PasswordUtil.check(password, user.getPasswordHash()); if false -> error "wrong password".
     *  4. If everything is correct: call Authenticator.createSession(user.getId()) to get a token.
     *  5. Call handler.setUserId(user.getId()) and
     *     ConnectionRegistry.register(user.getId(), handler) (these two lines
     *     are exactly what Phase 2 - real-time - depends on).
     *  6. Return the response Packet with the token filled in (Packet.ok
     *     doesn't include a token; build a new Packet(...) directly, or set
     *     the token field after building it).
     */
    public static Packet handleLogin(Packet request, ClientHandler handler) {
        JsonObject payload = request.getPayload();
        if (payload == null) {
            return Packet.error(PacketType.LOGIN, "Missing payload");
        }

        // TODO(Hesam): full implementation per the guide above.
        throw new UnsupportedOperationException("TODO(Hesam): implement handleLogin in Phase 1");
    }

    /**
     * TODO(Hesam):
     *  1. Authenticator.invalidate(request.getToken())
     *  2. If handler.getUserId() != null, also call ConnectionRegistry.unregister(...).
     */
    public static Packet handleLogout(Packet request, ClientHandler handler) {
        // TODO(Hesam): full implementation per the guide above.
        throw new UnsupportedOperationException("TODO(Hesam): implement handleLogout in Phase 1");
    }
}
