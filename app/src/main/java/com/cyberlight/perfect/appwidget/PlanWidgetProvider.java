package com.cyberlight.perfect.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.data.DateConverter;
import com.cyberlight.perfect.data.PlanAndRecordRepository;
import com.cyberlight.perfect.data.PlanRecord;
import com.cyberlight.perfect.data.PlanWithRecord;
import com.cyberlight.perfect.service.PlanWidgetListViewService;
import com.google.common.util.concurrent.FutureCallback;

import java.time.LocalDate;


public class PlanWidgetProvider extends AppWidgetProvider {
    public static final String WIDGET_ITEM_CLICK_ACTION = "widget_item_click_action";
    public static final String EXTRA_PLAN_ID = "extra_plan_id";
    public static final String EXTRA_PLAN_TARGET = "extra_plan_target";
    public static final String EXTRA_DATE_STR = "extra_date_str";

    private static final String TAG = "PlanWidgetProvider";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WIDGET_ITEM_CLICK_ACTION)) {
            int planId = intent.getIntExtra(EXTRA_PLAN_ID, 0);
            String dateStr = intent.getStringExtra(EXTRA_DATE_STR);
            DateConverter converter = new DateConverter();
            LocalDate date = converter.stringToLocalDate(dateStr);
            // fixme:注意广播接收器生命周期不能直接开启多线程任务
            PlanAndRecordRepository repository = new PlanAndRecordRepository(context);
            repository.loadPlanWithRecordByDateAndPlanId(date, planId, new FutureCallback<PlanWithRecord>() {
                @Override
                public void onSuccess(PlanWithRecord result) {
                    Log.d(TAG, String.valueOf(result.completionCount));
                    int newCompletionCount = (result.completionCount + 1) % (result.plan.targetNum + 1);
                    PlanRecord planRecordToUpdate = new PlanRecord(result.date, newCompletionCount, result.plan.planId);
                    repository.insertPlanRecords(planRecordToUpdate);
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, context);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate appWidgetIds.length:" + appWidgetIds.length);
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, PlanWidgetListViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.plan_widget);

            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects to a RemoteViewsService through the specified intent.
            // This is how you populate the data.
            remoteViews.setRemoteAdapter(R.id.plan_widget_content_lv, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            remoteViews.setEmptyView(R.id.plan_widget_content_lv, R.id.plan_widget_no_content_tv);

            // This section makes it possible for items to have individualized behavior.
            // It does this by setting up a pending intent template. Individuals items of a collection
            // cannot set up their own pending intents. Instead, the collection as a whole sets
            // up a pending intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            Intent toastIntent = new Intent(context, PlanWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting WIDGET_ITEM_CLICK_ACTION.
            toastIntent.setAction(WIDGET_ITEM_CLICK_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.plan_widget_content_lv, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
