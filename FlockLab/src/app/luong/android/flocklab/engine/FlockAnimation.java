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

package app.luong.android.flocklab.engine;

import android.graphics.*;
import android.view.*;

public class FlockAnimation extends Thread
   {
   /**
    * -1 to ignore.
    */
   private static final long FINAL_FPS = 30;
   
   /**
    * I simply use a counter to keep track of how long the fps number is displayed.
    */
   private static final int MAX_FPS_COUNT = 100;
   
   /**
    * To stop computation / drawing immediately.
    */
   private boolean _run;

   /**
    * To calculate elapsed time.
    */
   private long _lastMoment;      

   private int _frameCounted;
   private int _frameElapsed;
   private long _sleepTime;
   
   /**
    * This value is lightly synchronized: it is acceptable that the user
    * reset this number just before the game loop decrease it.
    */
   private int _fpsCount;
   
   /**
    * Actual FPS.
    */
   private int _fps;

   /**
    * Handle to the surface.
    */
   private SurfaceHolder _surfHolder;

   /**
    * How we paint everything.
    */
   private Paint _paint;
   
   /**
    * All the boids, target included.
    */
   private Flock flock;
   
   /**
    * Target.<br>
    * This copy is needed so we update it and notify the game loop to copy it to the flock.
    */
   private Vector2D _targetBuf;
   
   /**
    * One of the three type.<br>
    * This copy is needed so we update it and notify the game loop to copy it to the flock.
    */
   private int _targetTypeBuf;
   
   /**
    * Object that stores all buffers.
    */
   private FlockCalcBuffers _flockBuffers;
   
   /**
    * Accessed, synchronized, by the game loop to know if there are new settings.
    */
   private boolean _newSettings;
   
   /**
    * These values for settings are protected by synchronized _newSettings access method.<br>
    */
   private float _sepFactBuf, _alignFactBuf, _cohFactBuf,
                 _sepRangeBuf, _alignRangeBuf, _cohRangeBuf,
                 _evadeRadiusBuf, _seekRadiusBuf,
                 _areaWidth, _areaHeight, _areaX, _areaY;

   public FlockAnimation(SurfaceHolder surfaceHolder)
      {
      _newSettings = false;
      _frameCounted = 0;
      _frameElapsed = 0;
      _fpsCount = MAX_FPS_COUNT;
      _sleepTime = 0;
      _fps = 0;
      _surfHolder = surfaceHolder;
      _paint = new Paint();
      //Target.
      _targetBuf = new Vector2D(0, 0);
      _targetTypeBuf = Flock.INVISIBLE_TARGET;
      //Buffers.
      _flockBuffers = new FlockCalcBuffers();
      //For text style, do it only once, here.
      _paint.setTextSize(33);
      _paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
      }
   
   /**
    * Called when the SurfaceView is created and ready.
    * @param size Flock total size.
    */
   public void initFlock(int size)
      {
      Canvas c = _surfHolder.lockCanvas(null);
      flock = new Flock(0, 0, c.getWidth(), c.getHeight(), size);
      _areaWidth = c.getWidth();
      _areaHeight = c.getHeight();
      _areaX = 0;
      _areaY = 0;
      _surfHolder.unlockCanvasAndPost(c);
      }

   /**
    * Main Loop is here.<br>
    */
   @Override
   public void run()
      {
      while (isRunning())
         {
         Canvas c = null;
         try
            {
            c = _surfHolder.lockCanvas(null);
            synchronized (_surfHolder)
               {
               if(c!=null)
                  updateAll(c);
               }
            }
         finally
            {
            if (c != null)
               _surfHolder.unlockCanvasAndPost(c);
            }
         }
      }
   
   private void updateAll(Canvas canvas)
      {
      //Fill the canvas with color.
      canvas.drawColor(Color.WHITE);
      //FPS
      long now = System.currentTimeMillis();
      //Calculate elapsed time.
      int time = (int) (now - _lastMoment);
      if (_lastMoment != 0)
         {
         _frameElapsed += time;
         _frameCounted++;
         //Calculate fps each 10 frames.
         if (_frameCounted == 10)
            {
            //Update fps
            _fps = (int)(10000 / _frameElapsed);
            //Reset support variables.
            _frameElapsed = 0;
            _frameCounted = 0;
            }
         }
      if(checkFpsCount())//Draw it only if necessary.
         {
         _paint.setARGB(255,255,0,0);
         canvas.drawText("FPS: " + (_fps+1), //+1 because it is from 0 to MAX-1.
                         10,
                         33,
                         _paint);
         decreaseFpsCount();
         }
      //END FPS
      //FLOCK
      _paint.setARGB(255,0,0,0);
      if(isNewSettings())//Check if there are new settings
         {
         //Update flock with new factors.
         flock.updateFlock(canvas,
                           _paint,
                           _flockBuffers,
                           time,
                           _sepFactBuf, _alignFactBuf, _cohFactBuf,
                           _sepRangeBuf, _alignRangeBuf, _cohRangeBuf,
                           _targetBuf, _targetTypeBuf,
                           _seekRadiusBuf, _evadeRadiusBuf);
         //Updated, next cycle do it normal.
         resetNewSettings();
         }
      else//No new settings.
         {
         flock.updateFlock(canvas,
                           _paint,
                           _flockBuffers,
                           time);
         }
      //Update the canvas.
      canvas.restore();
      //END FLOCK
      //FPS CAP.
      if(FINAL_FPS > 0)
         {
         _sleepTime = (1000L / (FINAL_FPS)) - (System.currentTimeMillis() - now);
         try
            {
            if (_sleepTime > 0)
               sleep(_sleepTime);
            }
         catch (Exception e)
            {}
         _sleepTime = 0;
         }
      _lastMoment = now;
      }

   /**
    * Protect the variable access.
    */
   public synchronized boolean isRunning()
      {
      return _run;
      }     
     
   /**
    * Used to stop the thread.<br>
    * It is useful to bail out from the run() immediately.
    */
   public synchronized void setRunning(boolean b)
      {
      _run = b;
      }
   
   /**
    * Used by the game loop to now if there are new settings.
    * @return If there are new settings.
    */
   protected synchronized boolean isNewSettings()
      {
      return _newSettings;
      }
   
   /**
    * Used by the game loop after it updated settings values.
    */
   protected synchronized void resetNewSettings()
      {
      _newSettings = false;
      }
   
   /**
    * Called to pass the new settings to the game loop.
    * @param sepFact
    * @param alignFact
    * @param cohFact
    * @param sepRange
    * @param alignRange
    * @param cohRange
    * @param evadeRadius
    * @param seekRadius
    */
   public synchronized void setNewSettings(float sepFact, float alignFact, float cohFact,
                                           float sepRange, float alignRange, float cohRange,
                                           float seekRadius, float evadeRadius)
      {
      _sepFactBuf = sepFact;
      _alignFactBuf = alignFact;
      _cohFactBuf = cohFact;
      _sepRangeBuf = sepRange;
      _alignRangeBuf = alignRange;
      _cohRangeBuf = cohRange;
      _seekRadiusBuf = seekRadius;
      _evadeRadiusBuf = evadeRadius;
      trimTargetPosition();
      _newSettings = true;
      }
   
   /**
    * Since we update the target only when the settings are not visible,
    * then we can have a separate method.
    * @param x New x position.
    * @param y New y position.
    * @param switchType true = change target type.
    */
   public synchronized void setNewTarget(float x, float y, boolean switchType) 
      {
      _targetBuf._x = x;
      _targetBuf._y = y;
      if(switchType)
         {
         _targetTypeBuf++;
         if(_targetTypeBuf > Flock.INVISIBLE_TARGET)
            _targetTypeBuf = Flock.CHASE_TARGET;
         }
      trimTargetPosition();
      //android.util.Log.d("TARGET: ", ""+_targetTypeBuf);
      _newSettings = true;
      }
   
   protected synchronized void decreaseFpsCount()
      {
      if(_fpsCount>0)
         _fpsCount--;
      }
   
   protected synchronized boolean checkFpsCount()
      {
      return _fpsCount > 0;
      }
   
   public synchronized void resetFpsCount()
      {
      _fpsCount = MAX_FPS_COUNT;
      }
   
   /**
    * We live in a toroidal world.<br>
    * While distances are calculated correctly, for chase / evade areas, I just contain everything, for a simpler solution.<br>
    * :-P
    */
   protected void trimTargetPosition()
      {
      if(_targetTypeBuf == Flock.CHASE_TARGET)
         {
         if(_targetBuf._x+_seekRadiusBuf > _areaWidth)
            _targetBuf._x -= (_targetBuf._x+_seekRadiusBuf) - _areaWidth;
         if(_targetBuf._y+_seekRadiusBuf > _areaHeight)
            _targetBuf._y -= (_targetBuf._y+_seekRadiusBuf) - _areaHeight;
         if(_targetBuf._x-_seekRadiusBuf < _areaX)
            _targetBuf._x += -((_targetBuf._x-_seekRadiusBuf) - _areaX);
         if(_targetBuf._y-_seekRadiusBuf < _areaY)
            _targetBuf._y += -((_targetBuf._y-_seekRadiusBuf) - _areaY);
         }
      if(_targetTypeBuf == Flock.EVADE_TARGET)
         {
         if(_targetBuf._x+_evadeRadiusBuf > _areaWidth)
            _targetBuf._x -= (_targetBuf._x+_evadeRadiusBuf) - _areaWidth;
         if(_targetBuf._y+_evadeRadiusBuf > _areaHeight)
            _targetBuf._y -= (_targetBuf._y+_evadeRadiusBuf) - _areaHeight;
         if(_targetBuf._x-_evadeRadiusBuf < _areaX)
            _targetBuf._x += -((_targetBuf._x-_evadeRadiusBuf) - _areaX);
         if(_targetBuf._y-_evadeRadiusBuf < _areaY)
            _targetBuf._y += -((_targetBuf._y-_evadeRadiusBuf) - _areaY);
         }
      }
   }
