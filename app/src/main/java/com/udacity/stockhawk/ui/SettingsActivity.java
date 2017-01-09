package com.udacity.stockhawk.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.widgets.StocksCollectionWidgetProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.ui.SettingsActivityFragment.PREFS_NAME;

public class SettingsActivity extends AppCompatActivity {

    private int mAppWidgetId;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.confirm_settings)
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, SettingsActivityFragment.newInstance(mAppWidgetId)).commit();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startWidget();
            }
        });
    }

    private void startWidget() {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        Intent intent = new Intent(this, StocksCollectionWidgetProvider.class);
        intent.setAction(StocksCollectionWidgetProvider.AUTO_UPDATE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int interval = settings.getInt(String.format(getString(R.string.widget_pref_format), mAppWidgetId), 900000);
        long intervalUpdate = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        switch (interval){
            case 900000:
                intervalUpdate = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                break;
            case 3600000:
                intervalUpdate = AlarmManager.INTERVAL_HOUR;
                break;
            case 43200000:
                intervalUpdate = AlarmManager.INTERVAL_HALF_DAY;
                break;
            case 86400000:
                intervalUpdate = AlarmManager.INTERVAL_DAY;
                break;
            default:
        }
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), intervalUpdate, alarmIntent);
        finish();
    }
}
