package com.cyberlight.perfect.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.appwidget.PlanWidgetProvider;
import com.cyberlight.perfect.data.DateConverter;
import com.cyberlight.perfect.data.PlanAndRecordRepository;
import com.cyberlight.perfect.data.PlanWithRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlanWidgetListViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "ListRemoteViewsFactory";
    private final List<PlanWithRecord> planWithRecords = new ArrayList<>();
    private final Context context;
    private final int appWidgetId;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        // 重新加载数据
        planWithRecords.clear();
        PlanAndRecordRepository repository = new PlanAndRecordRepository(context);
        List<PlanWithRecord> planWithRecords = repository.loadPlanWithRecordsByDateSync(LocalDate.now());
        this.planWithRecords.addAll(planWithRecords);
        Log.d(TAG, "appWidgetId " + appWidgetId + " onDataSetChanged");
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return planWithRecords.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.plan_widget_item);
        PlanWithRecord planWithRecord = planWithRecords.get(position);
        String countStr = planWithRecord.completionCount == planWithRecord.plan.targetNum
                ? "✓" : String.valueOf(planWithRecord.completionCount);
        rv.setTextViewText(R.id.plan_widget_item_count_tv,
                context.getString(R.string.plan_widget_item_count, countStr));
        rv.setTextViewText(R.id.plan_widget_item_content_tv, planWithRecord.plan.content);
        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in PlanWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(PlanWidgetProvider.EXTRA_PLAN_ID, planWithRecord.plan.planId);
        extras.putInt(PlanWidgetProvider.EXTRA_PLAN_TARGET, planWithRecord.plan.targetNum);
        DateConverter converter = new DateConverter();
        extras.putString(PlanWidgetProvider.EXTRA_DATE_STR, converter.localDateToString(planWithRecord.date));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(R.id.plan_widget_item_ll, fillInIntent);
        return rv;
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
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}