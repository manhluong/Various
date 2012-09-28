/**
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

package app.luong.android.flocklab;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;

import app.luong.android.flocklab.engine.Flock;

/**
 * Every time the activity is paused and resumed the animation restart anew.
 */
public class FlockLab extends Activity
   {
   public static final int DIALOG_ABOUT = 0;
   
   private static final String SEP_KEY_WEIGHT = "FlockLab.Separation.Weight";
   
   private static final String ALIGN_KEY_WEIGHT = "FlockLab.Alignment.Weight";
   
   private static final String COH_KEY_WEIGHT = "FlockLab.Cohesion.Weight";
   
   private static final String SEP_KEY_RANGE = "FlockLab.Separation.Range";
   
   private static final String ALIGN_KEY_RANGE = "FlockLab.Alignment.Range";
   
   private static final String COH_KEY_RANGE = "FlockLab.Cohesion.Range";
   
   private static final String SEEK_KEY_RANGE = "FlockLab.Seek.Range";
   
   private static final String EVADE_KEY_RANGE = "FlockLab.Evade.Range";
   
   private static final String BOID_NUM_KEY = "FlockLab.Boid.Num";
   
   private static final String HIDE_KEY = "FlockLab.HideSettings";
   
   private static final String PREFS = "FlockLabPrefs";
   
   private static FlockView _flockView;
   
   private static SeekBar _seekSepWeight;
   
   private static SeekBar _seekAlignWeight;
   
   private static SeekBar _seekCohWeight;
   
   private static SeekBar _seekSepRange;
   
   private static SeekBar _seekAlignRange;
   
   private static SeekBar _seekCohRange;
   
   private static SeekBar _seekSeekRadius;
   
   private static SeekBar _seekEvadeRadius;
   
   private static SeekBar _seekBoidNum;
   
   /**
    * Used in onSettingsHide() to determine if Views are shown or not.<br>
    * onSettingsHide() change this value accordingly to show/hide at <b>next</b> call.<br>
    * True = <u>next time</u> visible.<br>
    * False = <u>next time</u> invisible.
    */
   private static boolean _hideSettings;

   private static enum SettingsState
      {
      WEIGHTS,
      RANGES,
      RADIUSES,
      BOIDSNUM
      };
      
   /**
    * Determine which settings to show or hide.
    */
   private static SettingsState _showWhichSettings;
   
   @Override
   public void onCreate(Bundle savedInstanceState)
      {
      super.onCreate(savedInstanceState);
      
      //Fullscreen.
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                           WindowManager.LayoutParams.FLAG_FULLSCREEN);

      
      setContentView(R.layout.activity_flock_lab);
      
      final ViewGroup settingsGroup = (ViewGroup)findViewById(R.id.settings_view);
      _flockView = (FlockView)findViewById(R.id.animation_view);
      _flockView.setOnTouchListener(new OnTouchListener()
         {
         @Override
         public boolean onTouch(View view, MotionEvent motion)
            {
            if(motion.getActionMasked() == MotionEvent.ACTION_DOWN ||
               motion.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
               {
               //Inside the settings area, is too easy to accidentally hide everything.
               
               //As we are ignoring the settings area, let the tap works only for hiding.
               //Otherwise the user could show the settings area tapping only part of the FlockView which
               //is a too weird behavior.
               
               if(motion.getY()>settingsGroup.getHeight() && !_hideSettings)//Next time invisible.
                  {
                  //Reset progress bars.
                  setWeightSeeks(_seekSepWeight.getSecondaryProgress(),
                                 _seekAlignWeight.getSecondaryProgress(),
                                 _seekCohWeight.getSecondaryProgress());
                  setRangeSeeks(_seekSepRange.getSecondaryProgress(),
                                _seekAlignRange.getSecondaryProgress(),
                                _seekCohRange.getSecondaryProgress());
                  setRadiusSeeks(_seekSeekRadius.getSecondaryProgress(),
                                _seekEvadeRadius.getSecondaryProgress());
                  onSettingsHide(_flockView);
                  return true;
                  }
               
               //If the touch is not for hiding the settings,
               //then we want to add / switch / remove the target.
               if(_hideSettings)
                  {
                  updateTarget(motion.getX(), motion.getY(), true);
                  return true;
                  }
               
               return false;
               }
            else if(motion.getActionMasked() == MotionEvent.ACTION_MOVE)
               {
               if(_hideSettings)
                  {
                  updateTarget(motion.getX(), motion.getY(), false);
                  return true;
                  }
               return false;
               }
            else
               return false;
            }
         });
      
      //WEIGHTS
      //Separation SeekBar factor.
      _seekSepWeight = (SeekBar)findViewById(R.id.seek_sep_weight);
      ((TextView)findViewById(R.id.title_sep_weight))
         .setText(getText(R.string.seek_sep_title_weight) + " " + Flock.DEF_SEP_FACTOR);
      _seekSepWeight.setProgress((int)(Flock.DEF_SEP_FACTOR*Flock.PROGRESS_FACTOR)-1);
      _seekSepWeight.setSecondaryProgress(_seekSepWeight.getProgress());
      _seekSepWeight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_sep_weight))
                  .setText(getText(R.string.seek_sep_title_weight) +
                        " " + (progress+1)/Flock.PROGRESS_FACTOR);
               }
         });
      //Alignment SeekBar factor.
      _seekAlignWeight = (SeekBar)findViewById(R.id.seek_align_weight);
      ((TextView)findViewById(R.id.title_align_weight))
         .setText(getText(R.string.seek_align_title_weight) + " " + Flock.DEF_ALIGN_FACTOR);
      _seekAlignWeight.setProgress((int)(Flock.DEF_ALIGN_FACTOR*Flock.PROGRESS_FACTOR)-1);
      _seekAlignWeight.setSecondaryProgress(_seekAlignWeight.getProgress());
      _seekAlignWeight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_align_weight))
                  .setText(getText(R.string.seek_align_title_weight) +
                        " " + (progress+1)/Flock.PROGRESS_FACTOR);
               }
         });
      //Cohesion SeekBar factor.
      _seekCohWeight = (SeekBar)findViewById(R.id.seek_coh_weight);
      ((TextView)findViewById(R.id.title_coh_weight))
         .setText(getText(R.string.seek_coh_title_weight) + " " + Flock.DEF_COH_FACTOR);
      _seekCohWeight.setProgress((int)(Flock.DEF_COH_FACTOR*Flock.PROGRESS_FACTOR)-1);
      _seekCohWeight.setSecondaryProgress(_seekCohWeight.getProgress());
      _seekCohWeight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_coh_weight))
                  .setText(getText(R.string.seek_coh_title_weight) +
                        " " + (progress+1)/Flock.PROGRESS_FACTOR);
               }
         });
      //END WEIGHTS ************************************************************
      
      //RANGES
      //Separation SeekBar range.
      _seekSepRange = (SeekBar)findViewById(R.id.seek_sep_range);
      ((TextView)findViewById(R.id.title_sep_range))
         .setText(getText(R.string.seek_sep_title_range) + " " + Flock.DEF_SEP_RANGE);
      _seekSepRange.setProgress((int)Flock.DEF_SEP_RANGE-1);
      _seekSepRange.setSecondaryProgress(_seekSepRange.getProgress());
      _seekSepRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_sep_range))
                  .setText(getText(R.string.seek_sep_title_range) +
                        " " + (progress+1));
               }
         });
      //Alignment SeekBar factor.
      _seekAlignRange = (SeekBar)findViewById(R.id.seek_align_range);
      ((TextView)findViewById(R.id.title_align_range))
         .setText(getText(R.string.seek_align_title_range) + " " + Flock.DEF_ALIGN_RANGE);
      _seekAlignRange.setProgress((int)(Flock.DEF_ALIGN_RANGE)-1);
      _seekAlignRange.setSecondaryProgress(_seekAlignRange.getProgress());
      _seekAlignRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_align_range))
                  .setText(getText(R.string.seek_align_title_range) +
                        " " + (progress+1));
               }
         });
      //Cohesion SeekBar factor.
      _seekCohRange = (SeekBar)findViewById(R.id.seek_coh_range);
      ((TextView)findViewById(R.id.title_coh_range))
         .setText(getText(R.string.seek_coh_title_range) + " " + Flock.DEF_COH_RANGE);
      _seekCohRange.setProgress((int)(Flock.DEF_COH_RANGE)-1);
      _seekCohRange.setSecondaryProgress(_seekCohRange.getProgress());
      _seekCohRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_coh_range))
                  .setText(getText(R.string.seek_coh_title_range) +
                        " " + (progress+1));
               }
         });
      //END RANGES ************************************************************
      
      //RADIUSES
      //Seek radius SeekBar.
      _seekSeekRadius = (SeekBar)findViewById(R.id.seek_seek_radius);
      ((TextView)findViewById(R.id.title_seek_radius))
         .setText(getText(R.string.seek_seek_title_radius) + " " + (int)Flock.DEF_SEEK_RADIUS);
      _seekSeekRadius.setProgress((int)(Flock.DEF_SEEK_RADIUS)-1);
      _seekSeekRadius.setSecondaryProgress(_seekSeekRadius.getProgress());
      _seekSeekRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_seek_radius))
                  .setText(getText(R.string.seek_seek_title_radius) +
                        " " + (progress+1));
               }
         });
      //Evade radius SeekBar.
      _seekEvadeRadius = (SeekBar)findViewById(R.id.seek_evade_radius);
      ((TextView)findViewById(R.id.title_evade_radius))
         .setText(getText(R.string.seek_evade_title_radius) + " " + (int)Flock.DEF_EVADE_RADIUS);
      _seekEvadeRadius.setProgress((int)(Flock.DEF_EVADE_RADIUS)-1);
      _seekEvadeRadius.setSecondaryProgress(_seekEvadeRadius.getProgress());
      _seekEvadeRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_evade_radius))
                  .setText(getText(R.string.seek_evade_title_radius) +
                        " " + (progress+1));
               }
         });
      //END RADIUSES ************************************************************
      
      //BOID NUMBER
      SharedPreferences settings = getSharedPreferences(PREFS, 0);
      int oldBoidNum = settings.getInt(BOID_NUM_KEY, -1);
      if(oldBoidNum<0)
         oldBoidNum = (int)(Flock.DEF_BOID_NUM)-Flock.FLOCK_SIZE_MIN;
      _seekBoidNum = (SeekBar)findViewById(R.id.seek_boid_num);
      ((TextView)findViewById(R.id.title_boid_num))
         .setText(getText(R.string.seek_title_boid_num) + " " + (int)(oldBoidNum+Flock.FLOCK_SIZE_MIN));
      _seekBoidNum.setProgress(oldBoidNum);
      _seekBoidNum.setSecondaryProgress(_seekBoidNum.getProgress());
      _seekBoidNum.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
               {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser)
               {
               ((TextView)findViewById(R.id.title_boid_num))
                  .setText(getText(R.string.seek_title_boid_num) +
                        " " + (progress+Flock.FLOCK_SIZE_MIN));
               }
         });
      //END BOID NUMBER
      
      //Start with settings hided.
      _hideSettings = true;
      //Start with weights. Anyway it is setted in onOptionsItemSelected.
      _showWhichSettings = SettingsState.WEIGHTS;
      
      //Call it once, to set saved preferences.
      _flockView.setNewFlockSettings((_seekSepWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     (_seekAlignWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     (_seekCohWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     _seekSepRange.getProgress()+1,
                                     _seekAlignRange.getProgress()+1,
                                     _seekCohRange.getProgress()+1,
                                     _seekSeekRadius.getProgress()+1,
                                     _seekEvadeRadius.getProgress()+1,
                                     _seekBoidNum.getProgress()+Flock.FLOCK_SIZE_MIN);
      }
   
   @Override
   protected Dialog onCreateDialog(int id)
      {
      switch(id)
         {
         case DIALOG_ABOUT:
            Dialog about = new Dialog(this);
            about.setContentView(R.layout.about_dialog);
            about.setTitle(R.string.menu_settings_about);
            ((Button)about.findViewById(R.id.github_btn)).setOnClickListener(new View.OnClickListener()
               {
                  @Override
                  public void onClick(View v)
                     {
                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))));
                     }
               });
            ((Button)about.findViewById(R.id.blog_btn)).setOnClickListener(new View.OnClickListener()
               {
                  @Override
                  public void onClick(View v)
                     {
                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.blog_url))));
                     }
               });
            return about;
         default:
            return null;
         }
      }
   
   @Override
   protected void onResume()
      {
      Log.d("FlockLab", "onResume");
      //Previously onPause() was called, hence SurfaceView.surfaceDestroyed() was called.
      super.onResume();
      //Activity is put back to foreground, so we init a new thread.
      _flockView.initAnimation(_seekSepWeight.getProgress()/Flock.PROGRESS_FACTOR,
                               _seekAlignWeight.getProgress()/Flock.PROGRESS_FACTOR,
                               _seekCohWeight.getProgress()/Flock.PROGRESS_FACTOR,
                               _seekSepRange.getProgress()+1,
                               _seekAlignRange.getProgress()+1,
                               _seekCohRange.getProgress()+1,
                               _seekSeekRadius.getProgress()+1,
                               _seekEvadeRadius.getProgress()+1,
                               _seekBoidNum.getProgress()+Flock.FLOCK_SIZE_MIN);
      //Thread launched in SurfaceView.surfaceCreated().
      }
   
   @Override
   protected void onPause()
      {
      super.onPause();
      writePreferences();
      //SurfaceView.surfaceDestroyed() is called, destroying the thread.
      }
   
   @Override
   protected void onStop()
      {
      super.onStop();
      //Do it one last time.
      writePreferences();
      }
   
   public void writePreferences()
      {
      SharedPreferences settings = getSharedPreferences(PREFS, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt(BOID_NUM_KEY, _seekBoidNum.getProgress());
      editor.commit();
      }
   
   @Override
   protected void onSaveInstanceState(Bundle outBundle)
      {
      super.onSaveInstanceState(outBundle);
      outBundle.putInt(SEP_KEY_WEIGHT, _seekSepWeight.getProgress());
      outBundle.putInt(ALIGN_KEY_WEIGHT, _seekAlignWeight.getProgress());
      outBundle.putInt(COH_KEY_WEIGHT, _seekCohWeight.getProgress());
      outBundle.putInt(SEP_KEY_RANGE, _seekSepRange.getProgress());
      outBundle.putInt(ALIGN_KEY_RANGE, _seekAlignRange.getProgress());
      outBundle.putInt(COH_KEY_RANGE, _seekCohRange.getProgress());
      outBundle.putInt(SEEK_KEY_RANGE, _seekSeekRadius.getProgress());
      outBundle.putInt(EVADE_KEY_RANGE, _seekEvadeRadius.getProgress());
      outBundle.putInt(BOID_NUM_KEY, _seekBoidNum.getProgress());
      outBundle.putBoolean(HIDE_KEY, _hideSettings);
      }
   
   @Override
   protected void onRestoreInstanceState(Bundle inBundle)
      {
      Log.d("FlockLab", "onRestoreInstanceState");
      super.onRestoreInstanceState(inBundle);
      setWeightSeeks(inBundle.getInt(SEP_KEY_WEIGHT),
                     inBundle.getInt(ALIGN_KEY_WEIGHT),
                     inBundle.getInt(COH_KEY_WEIGHT));
      setRangeSeeks(inBundle.getInt(SEP_KEY_RANGE),
                    inBundle.getInt(ALIGN_KEY_RANGE),
                    inBundle.getInt(COH_KEY_RANGE));
      setRadiusSeeks(inBundle.getInt(SEEK_KEY_RANGE),
                     inBundle.getInt(EVADE_KEY_RANGE));
      setBoidNumSeek(inBundle.getInt(BOID_NUM_KEY));
      setSecondaryWeightSeeks(_seekSepWeight.getProgress(),
                              _seekAlignWeight.getProgress(),
                              _seekCohWeight.getProgress());
      setSecondaryRangeSeeks(_seekSepRange.getProgress(),
                             _seekAlignRange.getProgress(),
                             _seekCohRange.getProgress());
      setSecondaryRadiusSeeks(_seekSeekRadius.getProgress(),
                              _seekEvadeRadius.getProgress());
      setSecondaryBoidNumSeek(_seekBoidNum.getProgress());
      //Since last time onSettingsHide() was called _hideSettings was updated, now we need the opposite.
      _hideSettings = !inBundle.getBoolean(HIDE_KEY);
      onSettingsHide(null);
      }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
      {
      getMenuInflater().inflate(R.menu.actionbar_menu, menu);
      return true;
      }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
      {
      switch (item.getItemId())
         {
         case R.id.menu_settings_weight:
            _hideSettings = false;
            onSettingsHide(null);//First, hide what is shown on screen.
            _showWhichSettings = SettingsState.WEIGHTS;
            _hideSettings = true;
            onSettingsHide(null);//Then show what is supposed to be shown.
            return true;
         case R.id.menu_settings_range:
            _hideSettings = false;
            onSettingsHide(null);//First, hide what is shown on screen.
            _showWhichSettings = SettingsState.RANGES;
            _hideSettings = true;
            onSettingsHide(null);//Then show what is supposed to be shown.
            return true;
         case R.id.menu_settings_radius:
            _hideSettings = false;
            onSettingsHide(null);//First, hide what is shown on screen.
            _showWhichSettings = SettingsState.RADIUSES;
            _hideSettings = true;
            onSettingsHide(null);//Then show what is supposed to be shown.
            return true;
         case R.id.menu_settings_boid_num:
            _hideSettings = false;
            onSettingsHide(null);//First, hide what is shown on screen.
            _showWhichSettings = SettingsState.BOIDSNUM;
            _hideSettings = true;
            onSettingsHide(null);//Then show what is supposed to be shown.
            return true;
         case R.id.menu_settings_fps:
            _flockView.showFpsCount();
            return true;
         case R.id.menu_settings_about:
            showDialog(DIALOG_ABOUT);
            return true;
         default:
            return super.onOptionsItemSelected(item);
         }
      }
   
   public void onSettingsReset(View src)
      {
      //-1 because there are also the various onProgressChanged() +1.
      if(_showWhichSettings == SettingsState.WEIGHTS)
         setWeightSeeks((int)(Flock.DEF_SEP_FACTOR*Flock.PROGRESS_FACTOR)-1,
                        (int)(Flock.DEF_ALIGN_FACTOR*Flock.PROGRESS_FACTOR)-1,
                        (int)(Flock.DEF_COH_FACTOR*Flock.PROGRESS_FACTOR)-1);
      else if(_showWhichSettings == SettingsState.RANGES)
         setRangeSeeks((int)Flock.DEF_SEP_RANGE-1,
                       (int)Flock.DEF_ALIGN_RANGE-1,
                       (int)Flock.DEF_COH_RANGE-1);
      else if(_showWhichSettings == SettingsState.RADIUSES)
         setRadiusSeeks((int)Flock.DEF_SEEK_RADIUS-1,
                        (int)Flock.DEF_EVADE_RADIUS-1);
      else if(_showWhichSettings == SettingsState.BOIDSNUM)
         setBoidNumSeek(Flock.DEF_BOID_NUM-Flock.FLOCK_SIZE_MIN);
      }
   
   public void onSettingsDone(View src)
      {
      if(_showWhichSettings == SettingsState.WEIGHTS)
         setSecondaryWeightSeeks(_seekSepWeight.getProgress(),
                                 _seekAlignWeight.getProgress(),
                                 _seekCohWeight.getProgress());
      else if(_showWhichSettings == SettingsState.RANGES)
         setSecondaryRangeSeeks(_seekSepRange.getProgress(),
                                _seekAlignRange.getProgress(),
                                _seekCohRange.getProgress());
      else if(_showWhichSettings == SettingsState.RADIUSES)
         setSecondaryRadiusSeeks(_seekSeekRadius.getProgress(),
                                 _seekEvadeRadius.getProgress());
      else if(_showWhichSettings == SettingsState.BOIDSNUM)
         setSecondaryBoidNumSeek(_seekBoidNum.getProgress());
      _flockView.setNewFlockSettings((_seekSepWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     (_seekAlignWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     (_seekCohWeight.getProgress()+1)/Flock.PROGRESS_FACTOR,
                                     _seekSepRange.getProgress()+1,
                                     _seekAlignRange.getProgress()+1,
                                     _seekCohRange.getProgress()+1,
                                     _seekSeekRadius.getProgress()+1,
                                     _seekEvadeRadius.getProgress()+1,
                                     _seekBoidNum.getProgress()+Flock.FLOCK_SIZE_MIN);
      onSettingsHide(_flockView);
      }
   
   /**
    * Show or hide all settings Views without any visibility checking.
    * @param src
    */
   public void onSettingsHide(View src)
      {
      //Hide / show the Layout is not reliable.
      if(!_hideSettings)
         {
         if(_showWhichSettings == SettingsState.WEIGHTS)
            {
            findViewById(R.id.title_sep_weight).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_sep_weight).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_align_weight).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_align_weight).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_coh_weight).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_coh_weight).setVisibility(View.INVISIBLE);
            setWeightSeeks(_seekSepWeight.getSecondaryProgress(),
                           _seekAlignWeight.getSecondaryProgress(),
                           _seekCohWeight.getSecondaryProgress());
            }
         else if(_showWhichSettings == SettingsState.RANGES)
            {
            findViewById(R.id.title_sep_range).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_sep_range).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_align_range).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_align_range).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_coh_range).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_coh_range).setVisibility(View.INVISIBLE);
            setRangeSeeks(_seekSepRange.getSecondaryProgress(),
                          _seekAlignRange.getSecondaryProgress(),
                          _seekCohRange.getSecondaryProgress());
            }
         else if(_showWhichSettings == SettingsState.RADIUSES)
            {
            findViewById(R.id.title_seek_radius).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_seek_radius).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_evade_radius).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_evade_radius).setVisibility(View.INVISIBLE);
            setRadiusSeeks(_seekSeekRadius.getSecondaryProgress(),
                           _seekEvadeRadius.getSecondaryProgress());
            }
         else if(_showWhichSettings == SettingsState.BOIDSNUM)
            {
            findViewById(R.id.title_boid_num).setVisibility(View.INVISIBLE);
            findViewById(R.id.seek_boid_num).setVisibility(View.INVISIBLE);
            setBoidNumSeek(_seekBoidNum.getSecondaryProgress());
            }
         //BUTTONS
         findViewById(R.id.reset_settings_btn).setVisibility(View.INVISIBLE);
         findViewById(R.id.cancel_settings_btn).setVisibility(View.INVISIBLE);
         findViewById(R.id.done_settings_btn).setVisibility(View.INVISIBLE);
         //Next time, show them.
         _hideSettings = true;
         }
      else
         {
         if(_showWhichSettings == SettingsState.WEIGHTS)
            {
            findViewById(R.id.title_sep_weight).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_sep_weight).setVisibility(View.VISIBLE);
            findViewById(R.id.title_align_weight).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_align_weight).setVisibility(View.VISIBLE);
            findViewById(R.id.title_coh_weight).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_coh_weight).setVisibility(View.VISIBLE);
            setSecondaryWeightSeeks(_seekSepWeight.getProgress(),
                                    _seekAlignWeight.getProgress(),
                                    _seekCohWeight.getProgress());
            }
         else if(_showWhichSettings == SettingsState.RANGES)
            {
            findViewById(R.id.title_sep_range).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_sep_range).setVisibility(View.VISIBLE);
            findViewById(R.id.title_align_range).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_align_range).setVisibility(View.VISIBLE);
            findViewById(R.id.title_coh_range).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_coh_range).setVisibility(View.VISIBLE);
            setSecondaryRangeSeeks(_seekSepRange.getProgress(),
                                   _seekAlignRange.getProgress(),
                                   _seekCohRange.getProgress());
            }
         else if(_showWhichSettings == SettingsState.RADIUSES)
            {
            findViewById(R.id.title_seek_radius).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_seek_radius).setVisibility(View.VISIBLE);
            findViewById(R.id.title_evade_radius).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_evade_radius).setVisibility(View.VISIBLE);
            setSecondaryRadiusSeeks(_seekSeekRadius.getProgress(),
                                    _seekEvadeRadius.getProgress());
            }
         else if(_showWhichSettings == SettingsState.BOIDSNUM)
            {
            findViewById(R.id.title_boid_num).setVisibility(View.VISIBLE);
            findViewById(R.id.seek_boid_num).setVisibility(View.VISIBLE);
            setSecondaryBoidNumSeek(_seekBoidNum.getProgress());
            }
         //BUTTONS
         findViewById(R.id.reset_settings_btn).setVisibility(View.VISIBLE);
         findViewById(R.id.cancel_settings_btn).setVisibility(View.VISIBLE);
         findViewById(R.id.done_settings_btn).setVisibility(View.VISIBLE);
         //Next time, hide them.
         _hideSettings = false;
         }
      }
   
   void updateTarget(float x, float y, boolean switchType)
      {
      _flockView.updateTarget(x, y, switchType);
      }
   
   private void setWeightSeeks(int sep, int align, int coh)
      {
      _seekSepWeight.setProgress(sep);
      _seekAlignWeight.setProgress(align);
      _seekCohWeight.setProgress(coh);
      }
   
   private void setRangeSeeks(int sep, int align, int coh)
      {
      _seekSepRange.setProgress(sep);
      _seekAlignRange.setProgress(align);
      _seekCohRange.setProgress(coh);
      }
   
   private void setRadiusSeeks(int seek, int evade)
      {
      _seekSeekRadius.setProgress(seek);
      _seekEvadeRadius.setProgress(evade);
      }
   
   private void setBoidNumSeek(int boids)
      {
      _seekBoidNum.setProgress(boids);
      }
   
   private void setSecondaryWeightSeeks(int sep, int align, int coh)
      {
      _seekSepWeight.setSecondaryProgress(sep);
      _seekAlignWeight.setSecondaryProgress(align);
      _seekCohWeight.setSecondaryProgress(coh);
      }
   
   private void setSecondaryRangeSeeks(int sep, int align, int coh)
      {
      _seekSepRange.setSecondaryProgress(sep);
      _seekAlignRange.setSecondaryProgress(align);
      _seekCohRange.setSecondaryProgress(coh);
      }
   
   private void setSecondaryRadiusSeeks(int seek, int evade)
      {
      _seekSeekRadius.setSecondaryProgress(seek);
      _seekEvadeRadius.setSecondaryProgress(evade);
      }
   
   private void setSecondaryBoidNumSeek(int boids)
      {
      _seekBoidNum.setSecondaryProgress(boids);
      }
   }
