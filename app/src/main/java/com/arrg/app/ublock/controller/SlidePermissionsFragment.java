package com.arrg.app.ublock.controller;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.Util;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SlidePermissionsFragment extends Fragment {

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public SlidePermissionsFragment() {
        // Required empty public constructor
    }

    @OnClick({R.id.btn_usageStats})
    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_usageStats:
                Util.open(getActivity(), new Intent(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)), false);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_slide_permissions, container, false);

        ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ATE.apply(this, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ButterKnife.findById(view, R.id.btn_drawPermission).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(getActivity())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                        }
                    }
                }
            });
        }
    }
}
