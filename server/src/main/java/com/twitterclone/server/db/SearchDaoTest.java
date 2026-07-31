package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;

import java.util.List;

public class SearchDaoTest {
    public static void main(String[] args) throws Exception {
        UserDAO userDAO = new UserDAO();
        TweetDAO tweetDAO = new TweetDAO();
        SearchDAO searchDAO = new SearchDAO();

        // یه یوزر و چند تا توییت تست بساز
        User carol = new User();
        carol.setUsername("carol_search_test");
        carol.setEmail("carol@test.com");
        carol.setPasswordHash(PasswordUtil.hash("pass789"));
        carol = userDAO.insertUser(carol);
        System.out.println("✅ Created carol with id: " + carol.getId());

        Tweet t1 = new Tweet();
        t1.setAuthorId(carol.getId());
        t1.setContent("I love PostgreSQL and Java!");
        tweetDAO.saveTweet(t1);

        Tweet t2 = new Tweet();
        t2.setAuthorId(carol.getId());
        t2.setContent("Just had some coffee.");
        tweetDAO.saveTweet(t2);

        // تست searchUsers
        List<User> foundUsers = searchDAO.searchUsers("carol");
        System.out.println("✅ searchUsers('carol') found " + foundUsers.size() + " user(s) (expected 1)");

        List<User> foundUsersUpper = searchDAO.searchUsers("CAROL"); // case-insensitive check
        System.out.println("✅ searchUsers('CAROL') found " + foundUsersUpper.size() + " user(s) (expected 1, case-insensitive)");

        // تست searchTweets
        List<Tweet> foundTweets = searchDAO.searchTweets("postgresql");
        System.out.println("✅ searchTweets('postgresql') found " + foundTweets.size() + " tweet(s) (expected 1)");
        for (Tweet t : foundTweets) {
            System.out.println("   - @" + t.getAuthorUsername() + ": " + t.getContent());
        }

        List<Tweet> noMatch = searchDAO.searchTweets("nonexistentword12345");
        System.out.println("✅ searchTweets('nonexistentword12345') found " + noMatch.size() + " tweet(s) (expected 0)");

        System.out.println("\n🎉 All search tests ran without exceptions!");
        System.exit(0);
    }
}