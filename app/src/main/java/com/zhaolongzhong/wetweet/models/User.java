package com.zhaolongzhong.wetweet.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zhaolongzhong.wetweet.main.RestApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import static com.zhaolongzhong.wetweet.oauth.LoginActivity.LOGIN_USER_ID;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

//@Parcel//Replace using id to object
@RealmClass
public class User implements RealmModel {
    private static final String TAG = User.class.getSimpleName();

    // JSON key
    public static final String JSON_KEY_FOLLOWING = "following";
    public static final String JSON_KEY_NOTIFICATIONS = "notifications";
    public static final String JSON_KEY_SCREEN_NAME = "screen_name";

    @PrimaryKey
    private long id;
    private String name;
    private String screenName;
    private String profileImageUrl;
    private String profileBackgroundImageUrl;
    private String profileBannerImageUrl;
    private String description;
    private String location;
    private long followersCount;
    private long friendsCount;
    private long listedCount;
    private long favouritesCount;
    private long statusesCount;
    private boolean following;
    private boolean followRequestSent;
    private boolean notifications;
    private String createdAt;
    private String friendsIds;
    private String followersIds;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
    }

    public String getProfileBannerImageUrl() {
        return profileBannerImageUrl;
    }

    public void setProfileBannerImageUrl(String profileBannerImageUrl) {
        this.profileBannerImageUrl = profileBannerImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(long friendsCount) {
        this.friendsCount = friendsCount;
    }

    public long getListedCount() {
        return listedCount;
    }

    public void setListedCount(long listedCount) {
        this.listedCount = listedCount;
    }

    public long getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(long favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public long getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(long statusesCount) {
        this.statusesCount = statusesCount;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFollowRequestSent() {
        return followRequestSent;
    }

    public void setFollowRequestSent(boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFriendsIds() {
        return friendsIds;
    }

    public void setFriendsIds(String friendsIds) {
        this.friendsIds = friendsIds;
    }

    public String getFollowersIds() {
        return followersIds;
    }

    public void setFollowersIds(String followersIds) {
        this.followersIds = followersIds;
    }

    // Deserialize the JSON and build User object.
    public void fromJSON(JSONObject json) {
        Log.d(TAG, "start fromJSON.");

        try {
            this.id = json.getLong("id");
            this.name = json.getString("name");

            this.screenName = json.getString("screen_name");
            this.profileImageUrl = json.optString("profile_image_url");
            this.profileBackgroundImageUrl = json.optString("profile_background_image_url");
            this.profileBannerImageUrl = json.optString("profile_banner_url");
            this.description = json.optString("description");
            this.location = json.optString("location");
            this.followersCount = json.getLong("followers_count");
            this.friendsCount = json.getLong("friends_count");
            this.listedCount = json.getLong("listed_count");
            this.favouritesCount = json.getLong("favourites_count");
            this.statusesCount = json.getLong("statuses_count");
            this.following = json.getBoolean(JSON_KEY_FOLLOWING);
            this.followRequestSent = json.getBoolean("follow_request_sent");
            this.createdAt = json.getString("created_at");
            this.notifications = json.getBoolean(JSON_KEY_NOTIFICATIONS);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing user.", e);
        }
    }

    public static void updateFromJSON(User user, JSONObject json) {
        Log.d(TAG, "start updateFromJson.");

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        try {
            user.id = json.getLong("id");
            user.name = json.getString("name");

            Log.d(TAG, "zhaox updateFromJSON: " + user.name);

            user.screenName = json.getString("screen_name");
            user.profileImageUrl = json.optString("profile_image_url");
            user.profileBackgroundImageUrl = json.optString("profile_background_image_url");
            user.profileBannerImageUrl = json.optString("profile_banner_url");
            user.description = json.optString("description");
            user.location = json.optString("location");
            user.followersCount = json.getLong("followers_count");
            user.friendsCount = json.getLong("friends_count");
            user.listedCount = json.getLong("listed_count");
            user.favouritesCount = json.getLong("favourites_count");
            user.statusesCount = json.getLong("statuses_count");
            user.following = json.getBoolean(JSON_KEY_FOLLOWING);
            user.followRequestSent = json.getBoolean("follow_request_sent");
            user.createdAt = json.getString("created_at");
            user.notifications = json.getBoolean(JSON_KEY_NOTIFICATIONS);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing user.", e);
        }

        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
        realm.close();
    }

    public static ArrayList<User> fromJSONArray(JSONArray jsonArray) {
        RealmList<User> users = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                User user = new User();
                user.fromJSON(tweetJson);
                if (user != null) {
                    users.add(user);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing user json:" + users, e);
                continue;
            }
        }


        realm.copyToRealmOrUpdate(users);
        realm.commitTransaction();
        realm.close();

        Log.d(TAG, "Total tweets: " + users.size() + "/" + jsonArray.length());
        return new ArrayList<>(users);
    }

    public void fromIdsJSONArray(JSONArray jsonArray, boolean isFriend) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String id = jsonArray.getString(i);
                if (isFriend) {
                    String friendsIds = this.getFriendsIds();
                    if (friendsIds != null && !friendsIds.isEmpty() && friendsIds.contains(id)) {
                        continue;
                    }
                } else {
                    String followersIds = this.getFollowersIds();
                    if (followersIds != null && !followersIds.isEmpty() && followersIds.contains(id)) {
                        continue;
                    }
                }
                sb.append(id);
                sb.append(",");

            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing id json.", e);
                continue;
            }
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (isFriend) {
            Log.d(TAG, "zhao set friendsIds:" + sb.toString());
            this.setFriendsIds(sb.toString());
        } else {
            this.setFollowersIds(sb.toString());
        }

        realm.copyToRealmOrUpdate(this);
        realm.commitTransaction();
        realm.close();
    }

    /**
     * Return a user by id
     * @param userId - User Id
     * @return a User object
     */
    public static User getUserById(long userId) {
        Realm realm = Realm.getDefaultInstance();
        User user =  realm.where(User.class).
                equalTo("id", userId).findFirst();
        realm.close();
        return user;
    }

    /**
     * Return a user by screen name
     * @param screenName
     * @return
     */
    public static User getUserByScreenName(String screenName) {
        Realm realm = Realm.getDefaultInstance();
        User user =  realm.where(User.class).
                equalTo("screenName", screenName).findFirst();
        realm.close();
        return user;
    }

    public static User getLoginUser() {
        SharedPreferences sharedPref = RestApplication.getApplication().getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LOGIN_USER_ID, -1);

        Realm realm = Realm.getDefaultInstance();
        User user =  realm.where(User.class).
                equalTo("id", loginUserId).findFirst();
        realm.close();
        return user;
    }

    /**
     * @return all the users in database
     */
    private RealmResults<User> getAllUsers() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> users =  realm.where(User.class).findAll();
        realm.close();
        return users;
    }

    /**
     * @return a list of friends
     */
    public RealmList<User> getFriends() {
        RealmList<User> friends = new RealmList<>();
        String friendsIds = this.getFriendsIds();

        Log.d(TAG, "zhao friendsIds:" + friendsIds);
        if (this.getFriendsIds() == null || this.getFriendsIds().isEmpty()) {
            return friends;
        }

        List<String> ids = Arrays.asList(this.getFriendsIds().split(","));
        RealmResults<User> allUsers = this.getAllUsers();

        for (User user : allUsers) {
            if (ids.contains(String.valueOf(user.getId()))) {
                friends.add(user);
            }
        }

        return friends;
    }

    /**
     * @return a list of followers
     */
    public RealmList<User> getFollowers() {
        RealmList<User> followers = new RealmList<>();

        if (this.getFollowersIds() == null || this.getFollowersIds().isEmpty()) {
            return followers;
        }

        List<String> ids = Arrays.asList(this.getFollowersIds().split(","));
        RealmResults<User> allUsers = this.getAllUsers();

        for (User user : allUsers) {
            if (ids.contains(String.valueOf(user.getId()))) {
                followers.add(user);
            }
        }

        return followers;
    }
}
