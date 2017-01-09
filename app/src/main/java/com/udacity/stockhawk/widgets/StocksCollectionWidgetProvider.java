package com.udacity.stockhawk.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.StockDetailActivity;

import timber.log.Timber;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

/**
 * Created by diego on 26/12/2016.
 */

public class StocksCollectionWidgetProvider extends AppWidgetProvider {

    public static final String AUTO_UPDATE = "com.udacity.stockhawk.ACTION_DATA_TO_UPDATE";
    private boolean isLargeLayout = true;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    public static final String ACTION_STOCK_CLICKED  = "com.udacity.stockhawk.ACTION_WIDGET_ITEM_CLICKED";
    public static final String SYMBOL = "symbol";

    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (ACTION_DATA_UPDATED.equals(intent.getAction())) {
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stocks_list);
        } else if (ACTION_STOCK_CLICKED.equals(intent.getAction())) {
            String symbol = intent.getStringExtra(SYMBOL);
            Intent detailIntent = new Intent(context, StockDetailActivity.class);
            detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailIntent.putExtra(SYMBOL, symbol);
            context.startActivity(detailIntent);
        } else if(AUTO_UPDATE.equals(intent.getAction())){
            Timber.d("Widget Updated");
            QuoteSyncJob.syncImmediately(context);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            int minWidth = appWidgetManager.getAppWidgetOptions(appWidgetIds[i]).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            if(minWidth<250)
                isLargeLayout = false;
            else
                isLargeLayout = true;

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StocksCollectionWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.putExtra("layout", isLargeLayout);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stocks_collection_widget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.stocks_list, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.stocks_list, R.id.empty_view);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent detailIntent = new Intent(context, StocksCollectionWidgetProvider.class);
            detailIntent.setAction(StocksCollectionWidgetProvider.ACTION_STOCK_CLICKED);
            detailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent detailPendingIntent = PendingIntent.getBroadcast(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stocks_list, detailPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        if(minWidth<250)
            isLargeLayout = false;
        else
            isLargeLayout = true;
        Log.d(LOG_TAG, minWidth+"");
        Intent intent = new Intent(context, StocksCollectionWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("layout", isLargeLayout);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stocks_collection_widget);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setRemoteAdapter(R.id.stocks_list, intent);

        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        rv.setEmptyView(R.id.stocks_list, R.id.empty_view);

        // Here we setup the a pending intent template. Individuals items of a collection
        // cannot setup their own pending intents, instead, the collection as a whole can
        // setup a pending intent template, and the individual items can set a fillInIntent
        // to create unique before on an item to item basis.
        Intent toastIntent = new Intent(context, StocksCollectionWidgetProvider.class);
        toastIntent.setAction(StocksCollectionWidgetProvider.ACTION_STOCK_CLICKED);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.stocks_list, toastPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }
}
