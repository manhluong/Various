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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class MaLuBuTestWidgetActivity extends Activity
   {
   /**
    * Widget ID that Android give us after showing the Configuration Activity.
    */
   private int widgetID;
   
   private int seconds;
   
   private int color;
   
   @Override
   public void onCreate(Bundle savedInstanceState)
      {
      super.onCreate(savedInstanceState);
      //Try to retrieve the ID of the impending widget.
      widgetID = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                         AppWidgetManager.INVALID_APPWIDGET_ID);
      //No valid ID, so bail out.
      if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID)
          finish();
      //If the user press BACK, do not add any widget.
      setResult(RESULT_CANCELED);
      
      setContentView(R.layout.main);
      //Start with 1 second.
      seconds = 1;
      final TextView mainResult = (TextView)findViewById(R.id.main_result);
      color = getViewColor(mainResult, Color.WHITE);
      final SeekBar seekBar = (SeekBar)findViewById(R.id.conf_seek);
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
         {
         public void onStopTrackingTouch(SeekBar seekBar)
            {}
            
         public void onStartTrackingTouch(SeekBar seekBar)
            {}
            
         public void onProgressChanged(SeekBar seekBar,
                                       int progress,
                                       boolean fromUser)
            {
            //From 1 sec to 60 sec = (from 0 sec to 59 sec) + 1 sec.
            seconds = progress+1;
            mainResult.setText(seconds + " seconds");
            }
         });
      }
   
   /**
    * Quick and dirty solution to get the background color.
    * @param view
    * @param defColor
    * @return
    */
   private int getViewColor(View view, int defColor)
      {
      Drawable back = view.getBackground();
      if(back instanceof PaintDrawable)
         return ((PaintDrawable)back).getPaint().getColor();
      else if(back instanceof ColorDrawable)
         return ((ColorDrawable)back).getColor();
      else
         return defColor;
      }
   
   public void mainColor(View source)
      {
      color = getViewColor(source, Color.WHITE);
      final TextView mainResult = (TextView)findViewById(R.id.main_result);
      mainResult.setBackgroundColor(color);
      mainResult.invalidate();
      }
   
   public void mainOk(View source)
      {
      Log.d("Bug check", "MaLuBuTestWidgetActivity.mainOk()");
      
      Log.d("mainOk", "called");
      //Configuration...
      //Store the color.
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      SharedPreferences.Editor prefsEdit = prefs.edit();
      prefsEdit.putInt(MaLuBuTestWidgetProvider.EXTRA_COLOR_VALUE+"_"+widgetID,
                       color);
      Log.d("mainOk", "tag: " +
                      MaLuBuTestWidgetProvider.EXTRA_COLOR_VALUE+"_"+widgetID +
                      " color: " + color);
      prefsEdit.commit();
      
      //Call onUpdate for the first time.
      Log.d("Ok Button", "First onUpdate broadcast sending...");
      final Context context = MaLuBuTestWidgetActivity.this;
      Intent firstUpdate = new Intent(context, MaLuBuTestWidgetProvider.class);
      firstUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      //Put the ID of our widget to identify it later.
      firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      context.sendBroadcast(firstUpdate);
      Log.d("Ok Button", "First onUpdate broadcast sent");
      
      //Create and launch the AlarmManager.
      //N.B.:
      //Use a different action than the first update to have more reliable results.
      //Use explicit intents to have more reliable results.
      Uri.Builder build = new Uri.Builder();
      build.appendPath(""+widgetID);
      Uri uri = build.build();
      Intent intentUpdate = new Intent(context, MaLuBuTestWidgetProvider.class);
      intentUpdate.setAction(MaLuBuTestWidgetProvider.UPDATE_ONE);//Set an action anyway to filter it in onReceive()
      intentUpdate.setData(uri);//One Alarm per instance.
      //We will need the exact instance to identify the intent.
      MaLuBuTestWidgetProvider.addUri(widgetID, uri);
      intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(MaLuBuTestWidgetActivity.this,
                                                                    0,
                                                                    intentUpdate,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);
      //If you want one global AlarmManager for all instances, put this alarmManger as
      //static and create it only the first time.
      //Then pass in the Intent all the ids and do not put the Uri.
      AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                System.currentTimeMillis()+(seconds*1000),
                                (seconds*1000),
                                pendingIntentAlarm);
      Log.d("Ok Button", "Created Alarm. Action = " + MaLuBuTestWidgetProvider.UPDATE_ONE +
                         " URI = " + build.build().toString() +
                         " Seconds = " + seconds);
      
      //Return the original widget ID, found in onCreate().
      Intent resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      setResult(RESULT_OK, resultValue);
      finish();
      }
   }