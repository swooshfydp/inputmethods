package com.swoosh.inputmethods;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String SERVICEENABLEKEY = "serviceEnabledKey";
    private PermissionChecker mPermissionChecker;
    private final static String LOGKEY = "SWOOSH_INPUT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        enableHeadServiceCheckbox(false);

        mPermissionChecker = new PermissionChecker(getActivity());
        if(!mPermissionChecker.isRequiredPermissionGranted()){
            enableHeadServiceCheckbox(false);
            Intent intent = mPermissionChecker.createRequiredPermissionIntent();
            startActivityForResult(intent, PermissionChecker.REQUIRED_PERMISSION_REQUEST_CODE);
        } else {
            enableHeadServiceCheckbox(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionChecker.REQUIRED_PERMISSION_REQUEST_CODE) {
            if (!mPermissionChecker.isRequiredPermissionGranted()) {
                Toast.makeText(getActivity(), "Required permission is not granted. Please restart the app and grant required permission.", Toast.LENGTH_LONG).show();
            } else {
                enableHeadServiceCheckbox(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(SERVICEENABLEKEY.equals(s)) {
            boolean enabled = sharedPreferences.getBoolean(s, false);
            if(enabled) {
                startHeadService();
            } else {
                stopHeadService();
            }
        }
    }

    private void enableHeadServiceCheckbox(boolean enabled) {
        getPreferenceScreen().findPreference(SERVICEENABLEKEY).setEnabled(enabled);
    }

    private void startHeadService() {
        Context context = getActivity();
        context.startService(new Intent(context, OverlayService.class));
        context.startService(new Intent(context, AutofillService.class));
    }

    private void stopHeadService() {
        Context context = getActivity();
        context.stopService(new Intent(context, OverlayService.class));
        context.stopService(new Intent(context, AutofillService.class));
    }
}
