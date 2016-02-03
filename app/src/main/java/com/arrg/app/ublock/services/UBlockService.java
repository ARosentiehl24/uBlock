package com.arrg.app.ublock.services;

/*
 * Created by albert on 23/12/2015.
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.receivers.RestartServiceReceiver;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.views.ApplicationsListActivity;
import com.arrg.app.ublock.views.DialogActivity;
import com.arrg.app.ublock.views.SplashScreenActivity;
import com.arrg.app.ublock.views.UpdateAppActivity;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class UBlockService extends Service {

    private static final int NOTIFICATION_ID = 1;

    private static final String DISABLE_NOTIFICATION = Constants.PACKAGE_NAME + ".DISABLE_NOTIFICATION";
    private static final String ENABLE_NOTIFICATION = Constants.PACKAGE_NAME + ".ENABLE_NOTIFICATION";
    private static final String PACKAGE = "package";
    private static final String RESTART_SERVICE = Constants.PACKAGE_NAME + ".RESTART_SERVICE";
    public static UBlockService UBLOCK;
    private ActivityManager activityManager;
    private Handler handler;
    private Handler updateHandler;
    private HashMap<String, Boolean> lockedPackages;
    private HashMap<String, Runnable> mUnlockMap;
    private CheckForUpdateRunnable mCheckForUpdateRunnable;
    private MonitorRunnable mMonitorRunnable;
    private SharedPreferences lockedAppsPreferences;
    private SharedPreferences packagesAppsPreferences;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private String cameraOrGalleryPackage = "";
    private String close = "";
    private String lastPackageName = "";
    private String notificationMessage;
    private String notificationTitle;

    public static boolean isRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UBLOCK = this;

        setupSharedPreferences();
        appInstalledManager();
        applicationOnAppDrawerManager();
        notificationManager();
        screenManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean firstInstall = preferencesUtil.getBoolean(settingsPreferences, R.string.first_install, true);

        if (firstInstall) {
            preferencesUtil.putValue(settingsPreferences, R.string.selected_font, "Roboto");

            preferencesUtil.putValue(settingsPreferences, R.string.enable_icon_on_app_drawer, getResources().getBoolean(R.bool.enable_icon_on_app_drawer));
            preferencesUtil.putValue(settingsPreferences, R.string.enable_swipe_on_ublock_screen, getResources().getBoolean(R.bool.enable_swipe_on_ublock_screen));
            preferencesUtil.putValue(settingsPreferences, R.string.enable_notification_on_status_bar, getResources().getBoolean(R.bool.enable_notification_on_status_bar));
            preferencesUtil.putValue(settingsPreferences, R.string.is_pattern_visible, getResources().getBoolean(R.bool.is_pattern_visible));
        }

        applicationOnAppDrawerManager(preferencesUtil.getBoolean(settingsPreferences, R.string.enable_icon_on_app_drawer, R.bool.enable_icon_on_app_drawer));
        notificationManager(preferencesUtil.getBoolean(settingsPreferences, R.string.enable_notification_on_status_bar, R.bool.enable_notification_on_status_bar));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        restartServiceIfNeeded();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        restartServiceIfNeeded();
        super.onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        restartServiceIfNeeded();
        super.onTaskRemoved(rootIntent);
    }

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        lockedAppsPreferences = getSharedPreferences(Constants.LOCKED_APPS_PREFERENCES, Context.MODE_PRIVATE);
        packagesAppsPreferences = getSharedPreferences(Constants.PACKAGES_APPS_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);

        setupInitialVariables();
    }

    public void setupInitialVariables() {
        notificationMessage = getString(R.string.text_notification);
        notificationTitle = getString(R.string.title_notification);

        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        handler = new Handler();
        updateHandler = new Handler();
        lockedPackages = new HashMap<>();

        mCheckForUpdateRunnable = new CheckForUpdateRunnable();
        mCheckForUpdateRunnable.run();

        mMonitorRunnable = new MonitorRunnable();
        mMonitorRunnable.run();

        startMonitor();
    }

    public void checkRunningApps() {
        checkActivityOnTop(getTopPackageName());
    }

    public void checkActivityOnTop(String activityOnTop) {
        if (!activityOnTop.equals("")) {
            if (!lockedPackages.containsKey(activityOnTop)) {
                lockedPackages.put(activityOnTop, true);
            }

            if (appIsLocked(activityOnTop) && lockedPackages.get(activityOnTop)) {
                    /*Intent uBlockIntent = new Intent(UBlockService.this, UBlockScreenActivity.class);
                    uBlockIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    uBlockIntent.putExtra(getString(R.string.activityOnTop), activityOnTop);
                    startActivity(uBlockIntent);*/
                if (!isRunning(this, UBlockScreenService.class)) {
                    Intent uBlockIntent = new Intent(UBlockService.this, UBlockScreenService.class);
                    uBlockIntent.putExtra(getString(R.string.activityOnTop), activityOnTop);
                    startService(uBlockIntent);
                }
            }

            if (!activityOnTop.equals(lastPackageName) && !activityOnTop.equals("com.google.android.googlequicksearchbox")) {
                close = lastPackageName;
                lockApp(lastPackageName);

                Log.d("TopActivity", "Close: " + close + " -> Open: " + activityOnTop);
            }

            lastPackageName = activityOnTop;

            //Log.d("TopActivity", activityOnTop);
        }
    }

    public void unLockApp(String activityOnTop) {
        lockedPackages.put(activityOnTop, false);


    }

    public void lockApp(String activityOnTop) {
        if (!lockAfterScreenOff()) {
            lockedPackages.put(activityOnTop, true);
        }
    }

    public boolean appIsLocked(String appPackage) {
        return preferencesUtil.getBoolean(lockedAppsPreferences, appPackage, false);
    }

    public boolean lockAfterScreenOff() {
        return preferencesUtil.getBoolean(settingsPreferences, R.string.lock_apps_after_screen_off, R.bool.lock_apps_after_screen_off);
    }

    public String getTopPackageName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

            long time = System.currentTimeMillis();

            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 5, time);

            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }

                if (!mySortedMap.isEmpty()) {
                    return mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcesses) {
                if (appProcessInfo.pkgList.length == 1) {
                    return appProcessInfo.pkgList[0];
                }
            }
        }

        return "";
    }

    public void appInstalledManager() {
        PackageReceiver mPackageReceiver = new PackageReceiver();
        IntentFilter packageFilter = new IntentFilter();

        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme("package");

        registerReceiver(mPackageReceiver, packageFilter);
    }

    public void applicationOnAppDrawerManager() {
        ApplicationOnAppDrawerManagerReceiver mApplicationOnAppDrawerManager = new ApplicationOnAppDrawerManagerReceiver();
        IntentFilter applicationFilter = new IntentFilter();

        applicationFilter.addAction(Constants.PACKAGE_NAME + ".HIDE_APPLICATION");
        applicationFilter.addAction(Constants.PACKAGE_NAME + ".SHOW_APPLICATION");

        registerReceiver(mApplicationOnAppDrawerManager, applicationFilter);
    }

    public void applicationOnAppDrawerManager(boolean isChecked) {
        if (isChecked) {
            Intent intent = new Intent(Constants.PACKAGE_NAME + ".SHOW_APPLICATION");
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(Constants.PACKAGE_NAME + ".HIDE_APPLICATION");
            sendBroadcast(intent);
        }
    }

    public void notificationManager() {
        NotificationManagerReceiver mNotificationManagerReceiver = new NotificationManagerReceiver();
        IntentFilter notificationFilter = new IntentFilter();

        notificationFilter.addAction(ENABLE_NOTIFICATION);
        notificationFilter.addAction(DISABLE_NOTIFICATION);

        registerReceiver(mNotificationManagerReceiver, notificationFilter);
    }

    public void notificationManager(boolean isChecked) {
        if (isChecked) {
            Intent intent = new Intent(ENABLE_NOTIFICATION);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(DISABLE_NOTIFICATION);
            sendBroadcast(intent);
        }
    }

    public void startServiceForeground() {

        Intent notificationIntent = new Intent();
        notificationIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        Uri uri = Uri.fromParts(PACKAGE, Constants.PACKAGE_NAME, null);
        notificationIntent.setData(uri);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(SplashScreenActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        Notification notification = new NotificationCompat
                .Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_settings))
                .setSmallIcon(R.drawable.ic_launcher_small)
                .setColor(ContextCompat.getColor(this, R.color.blue_grey_900))
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setContentIntent(notificationPendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public void restartServiceIfNeeded() {
        RestartServiceReceiver mRestartServiceReceiver = new RestartServiceReceiver();
        IntentFilter restartServiceFilter = new IntentFilter();

        restartServiceFilter.addAction(RESTART_SERVICE);

        registerReceiver(mRestartServiceReceiver, restartServiceFilter);

        sendBroadcast(new Intent(RESTART_SERVICE));
    }

    public void screenManager() {
        ScreenManagerReceiver mScreenReceiver = new ScreenManagerReceiver();
        IntentFilter screenFilter = new IntentFilter();

        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(mScreenReceiver, screenFilter);
    }

    public void onScreenOn() {
        startMonitor();
    }

    public void onScreenOff() {
        stopMonitor();
    }

    public final void startMonitor() {
        handler.post(mMonitorRunnable);
    }

    public final void stopMonitor() {
        handler.removeCallbacks(mMonitorRunnable);
    }

    public void readJSon(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        String version = "";
        String link = "";

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            final boolean isNull = reader.peek() == JsonToken.NULL;

            if (name.equals("version") && !isNull) {
                version = reader.nextString();
                preferencesUtil.putValue(settingsPreferences, R.string.ublock_version, version);
            } else if (name.equals("link") && !isNull) {
                link = reader.nextString();
                preferencesUtil.putValue(settingsPreferences, R.string.link_of_ublock_update, link);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        try {
            if (!version.equals(getPackageManager().getPackageInfo(getPackageName(), 0).versionName)) {
                Log.d("Update", "There is an update avaliable.");
                sendUpdateNotification(link);
            } else {
                Log.d("Update", "There isnt an update avaliable.");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        System.gc();
    }

    public void sendUpdateNotification(String link) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(this, UpdateAppActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();

        bundle.putString(getString(R.string.link_of_ublock_update), link);

        notificationIntent.putExtras(bundle);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(UpdateAppActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_system_update)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.play_store_icon))
                .setColor(Config.primaryColorDark(this, null))
                .setContentTitle(getString(R.string.updated_app_available))
                .setContentText(getString(R.string.updated_app_available_message))
                .setContentIntent(notificationPendingIntent)
                .setSound(soundUri);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private class CheckForUpdateTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            int count;

            try {
                URL url = new URL("https://www.dropbox.com/s/dwevfzb7b79pznv/uBlockUpdate.json?dl=1");
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(getFilesDir() + "/uBlockUpdate.json");

                byte data[] = new byte[1024];

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                InputStream in = new FileInputStream(getFilesDir() + "/uBlockUpdate.json");
                readJSon(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class CheckForUpdateRunnable implements Runnable {

        @Override
        public void run() {
            try {
                new CheckForUpdateTask().execute();
                updateHandler.postDelayed(mCheckForUpdateRunnable, 24 * 60 * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MonitorRunnable implements Runnable {

        @Override
        public void run() {
            try {
                UBlockService.this.checkRunningApps();
                handler.postDelayed(mMonitorRunnable, 250);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class PackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String appPackage = intent.getData().getSchemeSpecificPart();

            if (isNewApp(intent, appPackage) && listActivityIsNotRunning()) {
                try {
                    ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(appPackage, 0);

                    String appName = applicationInfo.loadLabel(getPackageManager()).toString();

                    preferencesUtil.putValue(packagesAppsPreferences, appPackage, appName);

                    if (showDialogMessage()) {
                        Intent dialogIntent = new Intent(UBlockService.this, DialogActivity.class);
                        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialogIntent.putExtra(getString(R.string.app_installed), appPackage);
                        startActivity(dialogIntent);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isNewApp(Intent intent, String appPackage) {
            return intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && !packagesAppsPreferences.contains(appPackage);
        }

        public boolean listActivityIsNotRunning() {
            return ApplicationsListActivity.listActivity == null;
        }

        public boolean showDialogMessage() {
            return preferencesUtil.getBoolean(settingsPreferences, R.string.show_dialog_when_new_apps_are_installed, R.bool.show_dialog_when_new_apps_are_installed);
        }
    }

    class ApplicationOnAppDrawerManagerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            ComponentName mComponentName = new ComponentName(context.getApplicationContext(), Constants.ALIAS_CLASSNAME);

            if (intent.getAction().equals(Constants.PACKAGE_NAME + ".HIDE_APPLICATION")) {
                int setting = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                int current = context.getPackageManager().getComponentEnabledSetting(mComponentName);

                if (current != setting) {
                    context.getPackageManager().setComponentEnabledSetting(mComponentName, setting, PackageManager.DONT_KILL_APP);
                }
            }

            if (intent.getAction().equals(Constants.PACKAGE_NAME + ".SHOW_APPLICATION")) {
                int setting = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

                int current = context.getPackageManager().getComponentEnabledSetting(mComponentName);

                if (current != setting) {
                    context.getPackageManager().setComponentEnabledSetting(mComponentName, setting, PackageManager.DONT_KILL_APP);
                }
            }
        }
    }

    class NotificationManagerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ENABLE_NOTIFICATION)) {
                startServiceForeground();
            }

            if (intent.getAction().equals(DISABLE_NOTIFICATION)) {
                stopForeground(true);
            }
        }
    }

    class ScreenManagerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onScreenOn();

                Log.d("Screen", "ON");
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onScreenOff();

                if (lockAfterScreenOff()) {
                    lockAllApps();
                }

                Log.d("Screen", "OFF");
            }
        }

        public void lockAllApps() {
            for (Map.Entry<String, Boolean> entry : lockedPackages.entrySet()) {
                entry.setValue(true);
            }
        }
    }

    public class LocalBinder extends Binder {
        public UBlockService getInstance() {
            return UBlockService.this;
        }
    }
}