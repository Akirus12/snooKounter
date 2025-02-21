package com.akirus.snookounter;

import android.app.Application;

public class UserSettings extends Application {
    public static final String PREFERENCES = "preferences";

    public static final String IS_FIRST_BOOT_KEY = "isFirstBootKey";
    public static boolean isFirstBoot;

    public static final String FULL_SCREEN_KEY = "fullScreenKey";
    public static final boolean FULL_SCREEN_ON = true;
    public static boolean fullScreenSwitch;

    public static final String ANIMATION_KEY = "animationKey";
    public static final boolean ANIMATION_ON = true;
    public static boolean animationSwitch;

    public static final String LEAD_FLAIR_KEY = "leadFlairKey";
    public static final boolean LEAD_FLAIR_ON = true;
    public static boolean leadFlairSwitch;
}
