package com.twitterclone.server.db;

import com.twitterclone.shared.model.Tweet;
import com.twitterclone.shared.model.User;

import java.util.List;

public class TweetDaoTest {
    public static void main(String[] args) throws Exception {
        UserDAO userDAO = new UserDAO();
        TweetDAO tweetDAO = new TweetDAO();
        FollowDAO followDAO = new FollowDAO();
        LikeDAO likeDAO = new LikeDAO();

        // --- ۱. دو تا یوزر تست بساز ---
        User alice = new User();
        alice.setUsername("alice_test");
        alice.setEmail("alice@test.com");
        alice.setPasswordHash(PasswordUtil.hash("pass123"));
        alice = userDAO.insertUser(alice);
        System.out.println("✅ Created alice with id: " + alice.getId());

        User bob = new User();
        bob.setUsername("bob_test");
        bob.setEmail("bob@test.com");
        bob.setPasswordHash(PasswordUtil.hash("pass456"));
        bob = userDAO.insertUser(bob);
        System.out.println("✅ Created bob with id: " + bob.getId());

        // --- ۲. تست saveTweet ---
        Tweet tweet1 = new Tweet();
        tweet1.setAuthorId(alice.getId());
        tweet1.setContent("Hello, this is my first tweet!");
        tweet1 = tweetDAO.saveTweet(tweet1);
        System.out.println("✅ Alice's tweet saved with id: " + tweet1.getId() + " at " + tweet1.getCreatedAt());

        Tweet tweet2 = new Tweet();
        tweet2.setAuthorId(bob.getId());
        tweet2.setContent("Hi from bob!");
        tweet2 = tweetDAO.saveTweet(tweet2);
        System.out.println("✅ Bob's tweet saved with id: " + tweet2.getId());

        // --- ۳. تست getGlobalTweets ---
        List<Tweet> globalFeed = tweetDAO.getGlobalTweets(10, 0);
        System.out.println("✅ Global feed has " + globalFeed.size() + " tweets (expected 2)");
        for (Tweet t : globalFeed) {
            System.out.println("   - @" + t.getAuthorUsername() + ": " + t.getContent());
        }

        // --- ۴. تست follow ---
        followDAO.follow(alice.getId(), bob.getId()); // alice follows bob
        System.out.println("✅ Alice now follows Bob: " + followDAO.isFollowing(alice.getId(), bob.getId()));
        System.out.println("✅ Bob's follower count: " + followDAO.countFollowers(bob.getId()));

        // --- ۵. تست getPersonalizedFeed (باید توییت خود alice + توییت bob رو ببینه) ---
        List<Tweet> personalizedFeed = tweetDAO.getPersonalizedFeed(alice.getId(), 10, 0);
        System.out.println("✅ Alice's personalized feed has " + personalizedFeed.size() + " tweets (expected 2)");

        // --- ۶. تست reply ---
        Tweet reply = new Tweet();
        reply.setAuthorId(bob.getId());
        reply.setContent("Replying to alice!");
        reply.setParentTweetId(tweet1.getId());
        reply = tweetDAO.saveTweet(reply);
        List<Tweet> replies = tweetDAO.getReplies(tweet1.getId());
        System.out.println("✅ Tweet1 has " + replies.size() + " replies (expected 1)");

        // --- ۷. تست like ---
        likeDAO.like(bob.getId(), tweet1.getId());
        System.out.println("✅ Bob liked alice's tweet. Like count: " + likeDAO.countLikes(tweet1.getId()));
        System.out.println("✅ Has bob liked it? " + likeDAO.hasLiked(bob.getId(), tweet1.getId()));

        // --- ۸. تست getUserTweets ---
        List<Tweet> aliceTweets = tweetDAO.getUserTweets(alice.getId());
        System.out.println("✅ Alice has " + aliceTweets.size() + " tweets (expected 1)");

        // --- ۹. تست deleteTweet ---
        boolean deleted = tweetDAO.deleteTweet(tweet2.getId(), bob.getId());
        System.out.println("✅ Bob's tweet deleted: " + deleted);

        System.out.println("\n🎉 All tests ran without exceptions!");
        System.exit(0);
    }
}