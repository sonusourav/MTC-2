package com.suliteos.towaso.user;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

class Analytics {

    Analytics(Context context) {
        FirebaseAnalytics.getInstance(context);
    }

    static void logEventComplain(Context context, Bundle bundle){
        FirebaseAnalytics.getInstance(context).logEvent("complain",bundle);
    }

    static void logEventFragmentOpened(Context context, Bundle bundle){
        FirebaseAnalytics.getInstance(context).logEvent("fragment",bundle);
    }
    
    static void setUserProperty(Context context,String key, String property){
        FirebaseAnalytics.getInstance(context).setUserProperty(key,property);
    }

    static void subscribeToTopic(String topic){
        if (topic.equals("login")){
            FirebaseMessaging.getInstance().unsubscribeFromTopic("logout");
        }else if (topic.equals("logout")){
            FirebaseMessaging.getInstance().unsubscribeFromTopic("login");
        }
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
}
