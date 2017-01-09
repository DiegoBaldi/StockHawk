package com.udacity.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.udacity.stockhawk.R;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREFS_NAME = "WidgetsPreference";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static SettingsActivityFragment newInstance(int appWidgetId) {
        SettingsActivityFragment myFragment = new SettingsActivityFragment();

        Bundle args = new Bundle();
        args.putInt("appWidgetId", appWidgetId);
        myFragment.setArguments(args);

        return myFragment;
    }

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppWidgetId = getArguments().getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.pref_update_interval_key))) {
            String value = sharedPreferences.getString(s, "");
            Timber.d(value);
            SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(String.format(getString(R.string.widget_pref_format), mAppWidgetId), Integer.valueOf(value));
            editor.commit();
        }
    }
}
