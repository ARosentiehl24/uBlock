package com.arrg.app.ublock.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.Constants;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.adapters.ApplicationAdapter;
import com.arrg.app.ublock.model.Applications;
import com.arrg.app.ublock.util.AppUtils;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.sw926.imagefileselector.ImageFileSelector;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.cketti.mailto.EmailIntentBuilder;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;
import it.gmariotti.recyclerview.itemanimator.SlideInOutBottomItemAnimator;

public class ApplicationsListActivity extends ATEActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<Applications> applicationsArrayList;
    private ApplicationAdapter adapter;
    public static ApplicationsListActivity listActivity;
    private Boolean doubleBackToExitPressedOnce = false;
    private Handler handler;
    private ImageFileSelector imageFileSelector;
    private ProgressDialog progress = null;
    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            swipeRefreshLayout.setRefreshing(true);
        }
    };
    private Runnable removeRefresh = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    YoYo.with(Techniques.ZoomOut).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(progressBar);
                    YoYo.with(Techniques.FadeInUp).duration(Constants.DURATIONS_OF_ANIMATIONS).playOn(rvApplications);

                    rvApplications.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                }
            });
        }
    };
    private SharedPreferences lockedAppsPreferences;
    private SharedPreferences packagesAppsPreferences;
    private SharedPreferences settingsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    @Bind(R.id.progressBar)
    CircleProgressBar progressBar;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;

    @Bind(R.id.nav_view)
    NavigationView navigationView;

    @Bind(R.id.rv_applications)
    RecyclerView rvApplications;

    @Bind(R.id.SwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications_list);

        ButterKnife.bind(this);
        Log.d("LifeCycle", "onCreate");

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        setupSharedPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LifeCycle", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle", "onResume");

        listActivity = this;

        setupNavigationDrawer(navigationView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LifeCycle", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LifeCycle", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.unRegisterPackageReceiver();
        }

        listActivity = null;

        System.gc();

        Log.d("LifeCycle", "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageFileSelector.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                if (SplashScreenActivity.splashScreenActivity != null) {
                    SplashScreenActivity.splashScreenActivity.finish();
                }

                ApplicationsListActivity.super.onBackPressed();

                Util.close(this, true);

                return;
            }

            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.please_press_back_again), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2500);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        drawer.closeDrawer(GravityCompat.START);

        final int id = item.getItemId();

        drawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (id == R.id.pin_settings) {
                    Util.open(ApplicationsListActivity.this, PinSettingsActivity.class, false);
                } else if (id == R.id.pattern_settings) {
                    Util.open(ApplicationsListActivity.this, PatternSettingsActivity.class, false);
                } else if (id == R.id.fingerprint_settings) {
                    Util.open(ApplicationsListActivity.this, FingerprintPreferenceSettingsActivity.class, false);
                } else if (id == R.id.more_settings) {
                    Util.open(ApplicationsListActivity.this, AdvancedPreferenceOptionsActivity.class, false);
                } else if (id == R.id.background_settings) {
                    Util.open(ApplicationsListActivity.this, BackgroundSettingsActivity.class, false);
                } else if (id == R.id.color_settings) {
                    Util.open(ApplicationsListActivity.this, ThemePreferenceActivity.class, false);
                } else if (id == R.id.font_settings) {
                    Util.open(ApplicationsListActivity.this, FontActivity.class, false);
                } else if (id == R.id.developer_settings) {
                    Uri webPage = Uri.parse("https://plus.google.com/u/0/108168960305991028461/posts");
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
                    startActivity(webIntent);
                } else if (id == R.id.lic_open_source) {
                    new MaterialDialog.Builder(ApplicationsListActivity.this)
                            .title(R.string.lic_open_source)
                            .positiveText(android.R.string.ok)
                            .typeface(Util.getTypeface(ApplicationsListActivity.this, preferencesUtil, settingsPreferences), Util.getTypeface(ApplicationsListActivity.this, preferencesUtil, settingsPreferences))
                            .customView(R.layout.lic_open_source, true)
                            .build()
                            .show();
                } else if (id == R.id.exit) {
                    SplashScreenActivity.splashScreenActivity.finish();

                    Util.closeInverse(ApplicationsListActivity.this, true);
                } else if (id == R.id.update_settings) {
                    new CheckForUpdateTask(progress).execute();
                } else if (id == R.id.send_bug_report) {
                    EmailIntentBuilder.from(ApplicationsListActivity.this)
                            .to(getString(R.string.support_email))
                            .subject(String.format(getString(R.string.bug_report_subject), getString(R.string.app_name), AppUtils.getVerName(ApplicationsListActivity.this)))
                            .start();
                }
            }
        }, Constants.DURATIONS_OF_ANIMATIONS);

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        imageFileSelector.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageFileSelector.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imageFileSelector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setupSharedPreferences() {
        preferencesUtil = new SharedPreferencesUtil(this);
        lockedAppsPreferences = getSharedPreferences(Constants.LOCKED_APPS_PREFERENCES, Context.MODE_PRIVATE);
        packagesAppsPreferences = getSharedPreferences(Constants.PACKAGES_APPS_PREFERENCES, Context.MODE_PRIVATE);
        settingsPreferences = getSharedPreferences(Constants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
        handler = new Handler();
        progress = new ProgressDialog(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null) {
                            if (bundle.getBoolean(getString(R.string.was_open_from_ublock_screen))) {
                                drawer.openDrawer(GravityCompat.START);
                            }
                        }

                        new LoadApplications().execute();
                    }
                });
            }
        }).start();
    }

    public void setupNavigationDrawer(NavigationView navigationView) {
        if (!Util.isSamsungDevice(this) || !Util.isFingerprintEnabled(this)) {
            navigationView.getMenu().getItem(0).getSubMenu().removeItem(R.id.fingerprint_settings);
        }

        imageFileSelector = new ImageFileSelector(ApplicationsListActivity.this);
        imageFileSelector.setCallback(new ImageFileSelector.Callback() {
            @Override
            public void onSuccess(String chosenFile) {
                Intent editImageIntent = new Intent(ApplicationsListActivity.this, SetUserPhotoActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.background), chosenFile);

                editImageIntent.putExtras(bundle);

                Util.openInverse(ApplicationsListActivity.this, editImageIntent, false);
            }

            @Override
            public void onError() {

            }
        });

        View header = navigationView.getHeaderView(0);

        ImageView profilePicture = ButterKnife.findById(header, R.id.profile_picture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Point size = new Point();

                Display display = getWindowManager().getDefaultDisplay();
                display.getSize(size);

                int width = size.x;
                int height = size.y;

                imageFileSelector.setQuality(80);
                imageFileSelector.setOutPutImageSize(width, height);

                new MaterialDialog.Builder(ApplicationsListActivity.this)
                        .content(R.string.picture_source)
                        .positiveText(R.string.from_gallery)
                        .negativeText(R.string.from_camera)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                imageFileSelector.selectImage(ApplicationsListActivity.this);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                imageFileSelector.takePhoto(ApplicationsListActivity.this);
                            }
                        })
                        .build().show();
            }
        });

        TextView appVersion = ButterKnife.findById(header, R.id.app_version);
        appVersion.setText(String.format(getString(R.string.current_version), AppUtils.getVerName(this)));

        if (isPictureSelected()) {
            String chosenPicture = preferencesUtil.getString(settingsPreferences, R.string.user_picture_preference, null);

            Bitmap bitmap = BitmapFactory.decodeFile(chosenPicture);
            profilePicture.setImageBitmap(bitmap);
        }
    }

    public boolean isPictureSelected() {
        return (preferencesUtil.getString(settingsPreferences, R.string.user_picture_preference, null) != null);
    }

    public ArrayList<Applications> generateData() {
        ArrayList<Applications> applicationsArrayList = new ArrayList<>();
        List<ApplicationInfo> installedApplications = checkForLaunchIntent(getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));

        for (int i = 0; i < installedApplications.size(); i++) {
            ApplicationInfo packageInfo = installedApplications.get(i);

            if (!packageInfo.loadLabel(getPackageManager()).toString().equals(getString(R.string.app_name))) {
                applicationsArrayList.add(new Applications(packageInfo.loadIcon(getPackageManager()), packageInfo.loadLabel(getPackageManager()).toString(), packageInfo.packageName));
            }

            preferencesUtil.putValue(packagesAppsPreferences, packageInfo.packageName, packageInfo.loadLabel(getPackageManager()).toString());
        }
        return applicationsArrayList;
    }

    public ArrayList<Applications> generateData(Map<String, ?> packages) {
        ArrayList<Applications> applicationsArrayList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : packages.entrySet()) {
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(entry.getKey(), 0);

                if (!entry.getValue().toString().equals(getString(R.string.app_name)) && getPackageManager().getLaunchIntentForPackage(entry.getKey()) != null) {
                    applicationsArrayList.add(new Applications(applicationInfo.loadIcon(getPackageManager()), entry.getValue().toString(), entry.getKey()));
                    Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return applicationsArrayList;
    }

    public List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applicationInfoArrayList = new ArrayList<>();
        for (ApplicationInfo info : list) {
            try {
                if (getPackageManager().getLaunchIntentForPackage(info.packageName) != null) {
                    applicationInfoArrayList.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applicationInfoArrayList;
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
            progress.setCancelable(true);
            progress.dismiss();

            if (!version.equals(getPackageManager().getPackageInfo(getPackageName(), 0).versionName)) {
                final String url = link;

                new MaterialDialog.Builder(this)
                        .title(getString(R.string.updated_app_available))
                        .content(getString(R.string.updated_app_available_message))
                        .positiveText(android.R.string.yes)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent updateAppIntent = new Intent(ApplicationsListActivity.this, UpdateAppActivity.class);

                                updateAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                                Bundle bundle = new Bundle();

                                bundle.putString(getString(R.string.link_of_ublock_update), url);

                                updateAppIntent.putExtras(bundle);

                                Util.openInverse(ApplicationsListActivity.this, updateAppIntent, true);
                            }
                        }).negativeText(android.R.string.no)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).typeface(Util.getTypeface(this, preferencesUtil, settingsPreferences), Util.getTypeface(this, preferencesUtil, settingsPreferences))
                        .show();
            } else {
                Toast.makeText(this, R.string.there_isnt_updated_app_available, Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    class CheckForUpdateTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        public CheckForUpdateTask(ProgressDialog progress) {
            this.progressDialog = progress;
            this.progressDialog.setMessage(getString(R.string.check_for_update_message));
            this.progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            this.progressDialog.show();
        }

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

    class LoadApplications extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setColorSchemeColors(Config.primaryColor(ApplicationsListActivity.this, null), Config.primaryColorDark(ApplicationsListActivity.this, null));
            handler.post(refresh);
        }

        @Override
        protected String doInBackground(String... params) {
            if (adapter == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, ?> packages = packagesAppsPreferences.getAll();

                        if (packages.isEmpty()) {
                            applicationsArrayList = generateData();
                        } else {
                            applicationsArrayList = generateData(packages);
                        }

                        Collections.sort(applicationsArrayList, new Comparator<Applications>() {
                            @Override
                            public int compare(Applications lhs, Applications rhs) {
                                return lhs.getAppName().compareTo(rhs.getAppName());
                            }
                        });

                        adapter = new ApplicationAdapter(ApplicationsListActivity.this, applicationsArrayList, lockedAppsPreferences, packagesAppsPreferences, settingsPreferences, preferencesUtil);
                        adapter.registerPackageReceiver();

                        AlphaAnimatorAdapter animatorAdapter = new AlphaAnimatorAdapter(adapter, rvApplications);
                        rvApplications.setAdapter(animatorAdapter);
                        rvApplications.setHasFixedSize(true);
                        rvApplications.setItemAnimator(new SlideInOutBottomItemAnimator(rvApplications));
                        rvApplications.setLayoutManager(new LinearLayoutManager(ApplicationsListActivity.this));
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            handler.postDelayed(removeRefresh, 250);
        }
    }
}
