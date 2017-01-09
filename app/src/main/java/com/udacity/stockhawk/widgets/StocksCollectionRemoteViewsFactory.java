package com.udacity.stockhawk.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by diego on 26/12/2016.
 */
public class StocksCollectionRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;
    private boolean isBigLayout;

    public StocksCollectionRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        isBigLayout = intent.getBooleanExtra("layout", true);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        final long token = Binder.clearCallingIdentity();
        try {
            mCursor = mContext.getContentResolver().query(Contract.Quote.uri, null, null, null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }

    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if(mCursor!=null && mCursor.moveToPosition(position)){
            int itemId = R.layout.widget_item_quote;
            if(!isBigLayout)
                itemId = R.layout.widget_item_quote_small;
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

            rv.setTextViewText(R.id.symbol, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
            rv.setTextViewText(R.id.price, dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if(isBigLayout){
                int changeId = 0;
                if (rawAbsoluteChange > 0) {
                    rv.setViewVisibility(R.id.change_plus, View.VISIBLE);
                    rv.setViewVisibility(R.id.change_minus, View.GONE);
                    changeId = R.id.change_plus;
                } else {
                    rv.setViewVisibility(R.id.change_plus, View.GONE);
                    rv.setViewVisibility(R.id.change_minus, View.VISIBLE);
                    changeId = R.id.change_minus;
                }
                if (PrefUtils.getDisplayMode(mContext)
                        .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                    rv.setTextViewText(changeId, change);
                } else {
                    rv.setTextViewText(changeId, percentage);
                }
            }
            // Next, set a fill-intent, which will be used to fill in the pending intent template
            // that is set on the collection view in StackWidgetProvider.
            Bundle extras = new Bundle();
            extras.putString(MainActivity.ARG_SYMBOL, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            // Make it possible to distinguish the individual on-click
            // action of a given item
            rv.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);
            return rv;
        }
        return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
