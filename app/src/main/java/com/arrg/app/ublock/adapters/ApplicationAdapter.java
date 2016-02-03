package com.arrg.app.ublock.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.views.ATECheckBox;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.model.Applications;
import com.arrg.app.ublock.util.PackageUtils;
import com.arrg.app.ublock.util.SharedPreferencesUtil;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.UTextView;

import java.util.ArrayList;

/*
 * Created by albert on 28/12/2015.
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private AppCompatActivity activity;
    private ArrayList<Applications> applicationsArrayList;
    private Boolean useCompatPadding;

    private PackageReceiver mPackageReceiver;
    private SharedPreferences lockedAppsPreferences;
    private SharedPreferences packagesAppsPreferences;
    private SharedPreferencesUtil preferencesUtil;

    public ApplicationAdapter(AppCompatActivity activity, ArrayList<Applications> applicationsArrayList, SharedPreferences lockedAppsPreferences, SharedPreferences packagesAppsPreferences, SharedPreferences settingsPreferences, SharedPreferencesUtil preferencesUtil) {
        this.activity = activity;
        this.applicationsArrayList = applicationsArrayList;
        this.lockedAppsPreferences = lockedAppsPreferences;
        this.packagesAppsPreferences = packagesAppsPreferences;
        this.preferencesUtil = preferencesUtil;

        useCompatPadding = preferencesUtil.getBoolean(settingsPreferences, R.string.use_compat_padding, R.bool.use_compat_padding);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View applications = inflater.inflate(R.layout.app_list_row, parent, false);

        return new ViewHolder(applications, activity, applicationsArrayList, preferencesUtil, lockedAppsPreferences);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Applications application = applicationsArrayList.get(position);

        holder.container.setUseCompatPadding(useCompatPadding);
        if (useCompatPadding) {
            holder.container.setRadius(2.5f);
        }
        holder.appIcon.setImageDrawable(application.getAppIcon());
        holder.appName.setText(application.getAppName());
        holder.appSelected.setChecked(preferencesUtil.getBoolean(lockedAppsPreferences, application.getAppPackage(), false));
        holder.divider.setVisibility(useCompatPadding ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return applicationsArrayList.size();
    }

    public void registerPackageReceiver() {
        mPackageReceiver = new PackageReceiver();
        IntentFilter packageFilter = new IntentFilter();

        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme("package");

        activity.registerReceiver(mPackageReceiver, packageFilter);
    }

    public void unRegisterPackageReceiver() {
        activity.unregisterReceiver(mPackageReceiver);
    }

    public void addApplication(Applications applications, Integer position) {
        applicationsArrayList.add(applications);
        notifyItemInserted(position);
    }

    public void removeApplication(int position) {
        applicationsArrayList.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private CardView container;
        private ImageView appIcon;
        private UTextView appName;
        private ATECheckBox appSelected;
        private View divider;

        private AppCompatActivity activity;
        private ArrayList<Applications> applicationsArrayList;
        private SharedPreferences lockedAppsPreferences;
        private SharedPreferencesUtil preferencesUtil;
        private String appPackageUninstalled;

        public ViewHolder(View itemView, AppCompatActivity activity, ArrayList<Applications> applicationsArrayList, SharedPreferencesUtil preferencesUtil, SharedPreferences lockedAppsPreferences) {
            super(itemView);
            ATE.apply(itemView, null);

            container = (CardView) itemView.findViewById(R.id.container);
            appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            appName = (UTextView) itemView.findViewById(R.id.app_name);
            appSelected = (ATECheckBox) itemView.findViewById(R.id.app_selected);
            divider = itemView.findViewById(R.id.divider);

            this.activity = activity;
            this.applicationsArrayList = applicationsArrayList;
            this.lockedAppsPreferences = lockedAppsPreferences;
            this.preferencesUtil = preferencesUtil;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Applications application = applicationsArrayList.get(getLayoutPosition());
            application.setIsChecked(!application.isChecked());
            appSelected.setChecked(!appSelected.isChecked());

            preferencesUtil.putValue(lockedAppsPreferences, application.getAppPackage(), appSelected.isChecked());
        }

        @Override
        public boolean onLongClick(View v) {
            final Applications application = applicationsArrayList.get(getLayoutPosition());

            new MaterialDialog.Builder(activity)
                    .icon(application.getAppIcon())
                    .limitIconToDefaultSize()
                    .title(application.getAppName())
                    .content(R.string.open_uninstall_message_dialog)
                    .positiveText(R.string.open)
                    .negativeText(R.string.uninstall)
                    .neutralText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(application.getAppPackage());
                            Util.open(activity, launchIntent, true);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            appPackageUninstalled = application.getAppPackage();
                            PackageUtils.uninstall(activity, appPackageUninstalled);
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.setCancelable(true);
                            dialog.dismiss();
                        }
                    })
                    .cancelable(false)
                    .show();

            return false;
        }
    }

    public class PackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                try {
                    ApplicationInfo applicationInfo = activity.getPackageManager().getApplicationInfo(intent.getData().getSchemeSpecificPart(), 0);

                    Drawable appIcon = applicationInfo.loadIcon(activity.getPackageManager());
                    String appName = applicationInfo.loadLabel(activity.getPackageManager()).toString();
                    String appPackage = intent.getData().getSchemeSpecificPart();

                    preferencesUtil.putValue(packagesAppsPreferences, appPackage, appName);

                    addApplication(new Applications(appIcon, appName, appPackage), applicationsArrayList.size());
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String appPackage = intent.getData().getSchemeSpecificPart();

                for (int i = 0; i < applicationsArrayList.size(); i++) {
                    if (applicationsArrayList.get(i).getAppPackage().equals(appPackage)) {
                        preferencesUtil.deleteValue(lockedAppsPreferences, appPackage);
                        preferencesUtil.deleteValue(packagesAppsPreferences, appPackage);
                        removeApplication(i);

                        break;
                    }
                }
            }
        }
    }
}
