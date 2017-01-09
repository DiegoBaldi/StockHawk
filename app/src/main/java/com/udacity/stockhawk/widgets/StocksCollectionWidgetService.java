package com.udacity.stockhawk.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by diego on 26/12/2016.
 */

public class StocksCollectionWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StocksCollectionRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}