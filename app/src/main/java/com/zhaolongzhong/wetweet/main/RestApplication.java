package com.zhaolongzhong.wetweet.main;

import android.app.Application;
import android.content.Context;

import com.zhaolongzhong.wetweet.services.RestClient;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

public class RestApplication extends com.activeandroid.app.Application {
    private static final String TAG = RestApplication.class.getSimpleName();

    private static Context context;
    private static RestApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        RestApplication.context = this;
        application = this;

        // Setup realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm.getDefaultInstance();
    }

    public static Application getApplication() {
        return application;
    }

    public static RestClient getRestClient() {
        return (RestClient) RestClient.getInstance(RestClient.class, RestApplication.context);
    }
}
