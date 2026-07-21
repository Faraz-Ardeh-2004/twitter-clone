package com.twitterclone.server.db;

import com.twitterclone.shared.model.User;

public class UserDaoTest {
    public static void main(String[] args) throws Exception {
        UserDAO userDAO = new UserDAO();

        // یه یوزر جدید بساز و ذخیره کن
        User newUser = new User();
        newUser.setUsername("testuser1");
        newUser.setEmail("testuser1@example.com");
        newUser.setPasswordHash(PasswordUtil.hash("mypassword123"));

        User saved = userDAO.insertUser(newUser);
        System.out.println("✅ Inserted user with id: " + saved.getId());

        // همون یوزر رو با username پیدا کن
        User foundByUsername = userDAO.getUserByUsername("testuser1");
        System.out.println("✅ Found by username: " + foundByUsername.getUsername()
                + " / " + foundByUsername.getEmail());

        // چک کن پسورد هش شده درست کار می‌کنه
        boolean passwordMatches = PasswordUtil.check("mypassword123", foundByUsername.getPasswordHash());
        System.out.println("✅ Password check result: " + passwordMatches);

        // همون یوزر رو با id پیدا کن
        User foundById = userDAO.getUserById(saved.getId());
        System.out.println("✅ Found by id: " + foundById.getUsername());

        System.exit(0);
    }
}