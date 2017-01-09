package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int DETAIL_LOADER = 0;

    @BindView(R.id.chart)
    LineChart mChart;

    @BindView(R.id.name)
    TextView mName;

    @BindView(R.id.current_price)
    TextView mCurrentPrice;

    @BindView(R.id.current_change)
    TextView mCurrentChange;

    private Uri mUri;

    private long mFirstTimestamp;

    private static final String[] DETAIL_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_NAME,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_QUOTE_ID = 0;
    public static final int COL_QUOTE_NAME = 1;
    public static final int COL_QUOTE_SYMBOL = 2;
    public static final int COL_QUOTE_PRICE = 3;
    public static final int COL_QUOTE_PERCENTAGE_CHANGE = 4;
    public static final int COL_QUOTE_ABSOLUTE_CHANGE = 5;
    public static final int COL_QUOTE_HISTORY = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        String symbol = bundle.getString(MainActivity.ARG_SYMBOL);
        mUri = Contract.Quote.makeUriForStock(symbol);
        Description description = new Description();
        description.setText("");
        mChart.setDescription(description);
        mChart.getAxisRight().setDrawLabels(false);
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    this,
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            int quoteId = data.getInt(COL_QUOTE_ID);
            String name = data.getString(COL_QUOTE_NAME);
            mName.setText(name);
            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            mCurrentPrice.setText(String.format(Locale.getDefault(), getString(R.string.current_price), dollarFormat.format(data.getFloat(COL_QUOTE_PRICE))));
            float rawAbsoluteChange = data.getFloat(COL_QUOTE_ABSOLUTE_CHANGE);
            if (rawAbsoluteChange > 0) {
                mCurrentChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                mCurrentChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            }
            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            mCurrentChange.setText(change);

            String history = data.getString(COL_QUOTE_HISTORY);
            String[] dataObjects = history.split("\n");
            Collections.reverse(Arrays.asList(dataObjects));
            List<Entry> entries = new ArrayList<Entry>();
            int i = 0;
            String[] xValues = new String[dataObjects.length];
            for (String dataElement : dataObjects) {
                String[] values = dataElement.split(", ");
                xValues[i] = values[0];
                if(i==0)
                    mFirstTimestamp = Long.valueOf(values[0]);
                // turn your data into Entry objects
                entries.add(new Entry(Float.valueOf(values[0])-mFirstTimestamp, Float.valueOf(values[1])));
                i++;
            }
            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.price_history));
            LineData dataValue = new LineData(dataSet);
            dataSet.setDrawFilled(true);
            dataSet.setDrawCircles(false);
            dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent));
            mChart.getXAxis().setValueFormatter(new TimeAxisValueFormatter(mChart, mFirstTimestamp));
            mChart.getAxisLeft().setValueFormatter(new PriceAxisValueFormatter());
            mChart.setData(dataValue);
            mChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

class TimeAxisValueFormatter implements IAxisValueFormatter{
    private long mFirstTimestamp;

    private BarLineChartBase<?> chart;

    public TimeAxisValueFormatter(BarLineChartBase<?> chart, long firstTimestamp) {
        this.chart = chart;
        mFirstTimestamp = firstTimestamp;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        long longValue = (long) value;

        if (chart.getVisibleXRange() > 10000000) {
            try{
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.setTimeInMillis(longValue+mFirstTimestamp);
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("MMM yy", Locale.getDefault());
                Date currentTimeZone = calendar.getTime();
                return sdf.format(currentTimeZone);
            }catch (Exception e) {
            }
            return "";
        } else {
            try{
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.setTimeInMillis(longValue+mFirstTimestamp);
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                Date currentTimeZone = calendar.getTime();
                return sdf.format(currentTimeZone);
            }catch (Exception e) {
            }
            return "";
        }
    }
}

class PriceAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;

    public PriceAxisValueFormatter() {

        // format values to 1 decimal digit
        mFormat = new DecimalFormat("###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mFormat.format(value) + " $";
    }


}
