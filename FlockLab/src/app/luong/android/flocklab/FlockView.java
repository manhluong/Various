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

import app.luong.android.flocklab.engine.*;
import android.content.*;
import android.util.*;
import android.view.*;


public class FlockView extends SurfaceView
                       implements SurfaceHolder.Callback 
   {
   
   /**
    * Thread that refresh the canvas regularly.<br>
    * Is is the engine and contains data for boids and target.
    */
   private FlockAnimation _animation;

   public FlockView(Context context, AttributeSet attrs)
      {
      super(context, attrs);
      initAnimation();
      }
   
   /**
    * I assume that the surface won't change its dimension, after it is
    * created.<br>
    * Do nothing.
    */
   @Override
   public void surfaceChanged(SurfaceHolder holder,
                              int format,
                              int width,
                              int height)
      { }

   /**
    * The Surface is created and ready to rumble.<br>
    * Start the animation thread.
    */
   @Override
   public void surfaceCreated(SurfaceHolder holder)
      {
      startAnimation();
      }
       
   /**
    * The Surface is destroyed and we will never touch it again.
    */
   @Override
   public void surfaceDestroyed(SurfaceHolder holder)
      {
      stopAnimationAndJoin();
      }
   
   private void initAnimation()
      {
      Log.d("FlockView", "initAnimation");
      //Retrieve the Holder of this Surface.
      SurfaceHolder holder = getHolder();
      //FlockView manage its life cycle.
      holder.addCallback(this);
      //Create the thread, passing the Holder so it can draw.
      //We will launch the thread in the right life cycle callback.
      _animation = new FlockAnimation(holder);
      }
   
   /**
    * Activity.onRestoreInstanceState() is called before Activity.onResume().
    * @param sep
    * @param align
    * @param coh
    */
   public void initAnimation(float sepFact, float alignFact, float cohFact,
                             float sepRange, float alignRange, float cohRange,
                             float evadeRadius, float seekRadius,
                             int boidNum)
      {
      initAnimation();
      _animation.setNewSettings(sepFact, alignFact, cohFact,
                                sepRange, alignRange, cohRange,
                                evadeRadius, seekRadius, boidNum);
      }
   
   protected void startAnimation()
      {
      Log.d("FlockView", "Start animation.");
      _animation.initFlock(Flock.FLOCK_SIZE);
      _animation.setRunning(true);
      _animation.start();
      }
   
   protected void stopAnimationAndJoin()
      {
      Log.d("FlockView", "Stop animation and join the thread.");
      //We need this variable to know when join() is done.
      //Used in case join() throws an exception.
      boolean again = true;
      //Stop computation and drawing.
      _animation.setRunning(false);
      //Try to join() and, eventually, retry.
      for(;again;)
         {
         try
            {
            //Make sure the thread is really ended.
            _animation.join();
            again = false;
            }
         catch (InterruptedException e)
            {
            Log.d("FlockView", "Thread join() failed. " + e.toString());
            }
         }
      //Exception or not, nullify the pointer for the GC.
      _animation = null;
      }
   
   public void setNewFlockSettings(float sepFact, float alignFact, float cohFact,
                                   float sepRange, float alignRange, float cohRange,
                                   float seekRadius, float evadeRadius,
                                   int boidNum)
      {
      _animation.setNewSettings(sepFact, alignFact, cohFact,
                                sepRange, alignRange, cohRange,
                                seekRadius, evadeRadius,
                                boidNum);
      }
   
   /**
    * Tell the game loop to display the fps count as soon as possible.
    */
   public void showFpsCount()
      {
      _animation.resetFpsCount();
      }
   
   public void updateTarget(float x, float y, boolean switchType)
      {
      _animation.setNewTarget(x, y, switchType);
      }
   }
