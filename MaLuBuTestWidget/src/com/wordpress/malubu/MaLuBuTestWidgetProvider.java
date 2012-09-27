/*
 * Copyright 2012 Manh Luong   Bui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordpress.malubu;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class MaLuBuTestWidgetProvider extends AppWidgetProvider
   {
   public static final String EXTRA_COLOR_VALUE = "com.malubu.wordpress.EXTRA_COLOR_VALUE";
   
   public static final String UPDATE_ONE = "com.malubu.wordpress.UPDATE_ONE_WIDGET";
   
   /**
    * We need the exact Uri instance to identify the Intent.
    */
   private static HashMap<Integer, Uri> uris = new HashMap<Integer, Uri>();
   
   @Override
   public void onReceive(Context context,
                         Intent intent)
      {
      Log.d("Bug check", "MaLuBuTestWidgetProvider.onReceive()");
      
      String action = intent.getAction();
      Log.d("onReceive", "action: " + action);
      if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
         action.equals(UPDATE_ONE))
         {
         //Check if there is a single widget ID.
         int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                           AppWidgetManager.INVALID_APPWIDGET_ID);
         //Call our onUpdate() passing a one element array, with the retrieved ID.
         if(widgetID != AppWidgetManager.INVALID_APPWIDGET_ID)
            this.onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetID});
         }
      else
         {
         //Don't call the super implementation of onReceive().
         //super.onReceive(context, intent);
         //Manage the 'surplus' ID.
         //int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
         //Dispatch all other widget statuses.
         if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action))
            {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID))
               {
               final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
               this.onDeleted(context, new int[] { appWidgetId });
               }
            }
         else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action))
            {
            this.onEnabled(context);
            }
         else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action))
            {
            this.onDisabled(context);
            }
         }
      }
   
   @Override
   public void onUpdate(Context context,
                        AppWidgetManager appWidgetManager,
                        int[] appWidgetIds)
      {
      ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MaLuBuTestWidgetProvider.class.getName());
      int[] allIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
      Log.d("Bug check", "MaLuBuTestWidgetProvider.onUpdate() - number of ids: " + allIds.length);
      
      Log.d("onUpdate", "called, number of instances " + appWidgetIds.length);
      for (int widgetId : appWidgetIds)
         {
         updateAppWidget(context,
                         appWidgetManager,
                         widgetId);
         }
      }
   
   /**
    * Each time an instance is removed, we cancel the associated AlarmManager.
    */
   @Override
   public void onDeleted(Context context, int[] appWidgetIds)
      {
      super.onDeleted(context, appWidgetIds);
      for (int appWidgetId : appWidgetIds)
         {
         cancelAlarmManager(context, appWidgetId);
         }
     }
   
   protected void cancelAlarmManager(Context context, int widgetID)
      {
      AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      Intent intentUpdate = new Intent(context, MaLuBuTestWidgetProvider.class);
      //AlarmManager are identified with Intent's Action and Uri.
      intentUpdate.setAction(MaLuBuTestWidgetProvider.UPDATE_ONE);
      //Don't put the uri to cancel all the AlarmManager with action UPDATE_ONE.
      intentUpdate.setData(uris.get(widgetID));
      intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(context,
                                                                    0,
                                                                    intentUpdate,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
      alarm.cancel(pendingIntentAlarm);
      Log.d("cancelAlarmManager", "Cancelled Alarm. Action = " +
                                  MaLuBuTestWidgetProvider.UPDATE_ONE +
                                  " URI = " + uris.get(widgetID));
      uris.remove(widgetID);
      }
   
   public static void addUri(int id, Uri uri)
      {
      uris.put(Integer.valueOf(id), uri);
      }
   
   private void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId)
      {
      //Inflate layout.
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                                R.layout.widget);
      //Update UI.
      remoteViews.setTextViewText(R.id.label, getTimeStamp());
      //Retrieve color.
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      int actColor = prefs.getInt(EXTRA_COLOR_VALUE+"_"+appWidgetId, Color.WHITE);
      Log.d("updateAppWidget", "retrieve: " + EXTRA_COLOR_VALUE+"_"+appWidgetId + " color: " + actColor);
      //Apply color.
      remoteViews.setInt(R.id.label, "setBackgroundColor", actColor);
      //Create the intent.
      Intent labelIntent = new Intent(context, MaLuBuTestWidgetProvider.class);
      labelIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
      //Put the ID of our widget to identify it later.
      labelIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
      PendingIntent labelPendingIntent = PendingIntent.getBroadcast(context,
                                                                    appWidgetId,
                                                                    labelIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
      remoteViews.setOnClickPendingIntent(R.id.label, labelPendingIntent);
      Log.d("updateAppWidget", "Updated ID: " + appWidgetId);
      //Call the Manager to ensure the changes take effect.
      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
      }
   
   /**
    * Utility method to ensure that when we want an Intent that fire ACTION_APPWIDGET_UPDATE, the extras are correct.<br>
    * The default implementation of onReceive() will discard it if we don't add the ids of all the instances.
    * @param context
    * @return
    *//*
   protected Intent get_ACTION_APPWIDGET_UPDATE_Intent(Context context)
      {
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MaLuBuTestWidgetProvider.class.getName());
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
      Intent intent = new Intent(context, MaLuBuTestWidgetProvider.class);
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
      return intent;
      }*/
   
   private String getTimeStamp()
      {
      String res="";
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      Date now = calendar.getTime();
      res += now.getHours()+":"+now.getMinutes()+":"+now.getSeconds();
      return res;
      }

   }
