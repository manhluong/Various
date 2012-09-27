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

import java.util.Random;

import android.graphics.*;

/**
 * Encapsulate both pure Java version and RenderScript version.<br>
 * So clients will switch between versions more easily.<br>
 * <br>
 * The RenderScript part has its own boids.
 */
public class Flock
   {
   public static final float DEF_SEP_FACTOR = 0.6f;
   
   public static final float DEF_ALIGN_FACTOR = 0.3f;
   
   public static final float DEF_COH_FACTOR = 0.4f;
   
   public static final float DEF_SEP_RANGE = 10.0f;
   
   public static final float DEF_ALIGN_RANGE = 40.0f;
   
   public static final float DEF_COH_RANGE = 40.0f;
   
   public static final float DEF_SEEK_RADIUS = 50.0f;
   
   public static final float DEF_EVADE_RADIUS = 50.0f;
   
   public static final int TARGET_RADIUS = 10;
   
   public static final int CHASE_TARGET = 0;
   
   public static final int EVADE_TARGET = 1;
   
   public static final int INVISIBLE_TARGET = 2;
   
   /**
    * This mean that the factors has a resolution of 1/100, allowing 1000 values to be chosen.
    */
   public static final float PROGRESS_FACTOR = 100.0f;
   
   /**
    *  An array with all the boids.<br>
    *  No optimized data structure yet.
    */
   private static Boid _boids[];
   
   /**
    * One target for everyone.<br>
    * We need to memorize it here, to draw it in each cycle.
    */
   private static Vector2D _target;
   
   /**
    * One of the three type.<br>
    * We need to memorize it here, to draw it in each cycle.
    */
   private static int _targetType;
   
   /**
    * We need to memorize it here, to draw it in each cycle.
    */
   private static float _targetSeekRadius;
   
   /**
    * We need to memorize it here, to draw it in each cycle.
    */
   private static float _targetEvadeRadius;
   
   /**
    * Used to generate random float values to init the boids.
    */
   private Random _randomGen;
   
   private float _areaX;
   
   private float _areaY;
   
   private float _widthBounds;
   
   private float _heightBounds;

   /**
    * @param areaX Origin of the area. 
    * @param areaY Origin of the area.
    * @param width Width of the area in which this flock will fly.
    * @param height Height of the area in which this flock will fly.
    */
   public Flock(float areaX, float areaY, float width, float height, int size)
      {
      _areaX = areaX;
      _areaY = areaY;
      _widthBounds = width;
      _heightBounds = height;
      _randomGen = new Random();
      //Flock data.
      _boids = new Boid[size];
      //Populate with random values.
      for(int i=0; i<_boids.length; i++)
         {
         _boids[i] = new Boid(_areaX,//Start x area.
                              _areaY,//Start y area.
                              _widthBounds,//Width area.
                              _heightBounds,//Height area.
                              _randomGen.nextFloat()*_widthBounds,//Start x position.
                              _randomGen.nextFloat()*_heightBounds,//Start y position.
                              3.0f,//Maximum speed.
                              0.1f,//Maximum force.
                              (_randomGen.nextFloat()*6) - 3,//Initial x speed of [-3, +3]
                              (_randomGen.nextFloat()*6) - 3,//Initial y speed of [-3, +3]
                              (_randomGen.nextFloat()*3) + 1//Random size of [+1, +4]
                              );
         }
      _target = new Vector2D(0, 0);
      _targetType = INVISIBLE_TARGET;
      }
   
   /**
    * Without updating flocking factors.
    * @param canvas
    * @param paint
    * @param buffers
    */
   public void updateFlock(Canvas canvas,
                           Paint paint,
                           FlockCalcBuffers buffers,
                           int elapsedTime)
      {
      for (int i = 0; i < _boids.length; i++)
         {
         //Update boid.
         _boids[i].updateBoid(_boids,//All boids.
                              buffers,//Used buffers.
                              elapsedTime,
                              _target,
                              _targetType
                              );
         //Draw the boid.
         canvas.drawCircle(_boids[i]._position._x,
                           _boids[i]._position._y,
                           _boids[i]._r,
                           paint
                           );
         
         }
      drawTarget(paint, canvas);
      }
   
   /**
    * Also update flocking factors.<br>
    * Need a separated method to use only one flag for synchronization.<br>
    * Synchronizing all three factors at the same time, guarantee that the boids
    * are all updated at the same cycle.
    * @param canvas
    * @param paint
    * @param buffers
    * @param sf
    * @param af
    * @param cf
    */
   public void updateFlock(Canvas canvas,
                           Paint paint,
                           FlockCalcBuffers buffers,
                           int elapsedTime,
                           float sf, float af, float cf,
                           float sr, float ar, float cr,
                           Vector2D target, int targetType,
                           float ser, float er)
      {
      //Update target.
      _target._x = target._x;
      _target._y = target._y;
      _targetType = targetType;
      _targetSeekRadius = ser;
      _targetEvadeRadius = er;
      for (int i = 0; i < _boids.length; i++)
         {
         //Update weights, ranges and radiuses.
         _boids[i].updateParameters(sf, af, cf,
                                    sr, ar, cr,
                                    ser, er);
         //Update boids.
         _boids[i].updateBoid(_boids,//All boids.
                              buffers,//Used buffers.
                              elapsedTime,
                              _target,
                              _targetType
                              );
         //Draw the boid.
         canvas.drawCircle(_boids[i]._position._x,
                           _boids[i]._position._y,
                           _boids[i]._r,
                           paint
                           );
         }
      drawTarget(paint, canvas);
      }
   
   protected void drawTarget(Paint paint, Canvas canvas)
      {
      //Draw the target if visible.
      if(_targetType!=INVISIBLE_TARGET)
         {
         int oldPaint = paint.getColor();
         if(_targetType==CHASE_TARGET)
            {
            paint.setARGB(255, 0, 255, 0);
            canvas.drawCircle(_target._x,
                              _target._y,
                              TARGET_RADIUS,
                              paint
                              );
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(_target._x,
                              _target._y,
                              _targetSeekRadius,
                              paint
                              );
            }
         else if(_targetType==EVADE_TARGET)
            {
            paint.setARGB(255, 255, 0, 0);
            canvas.drawCircle(_target._x,
                              _target._y,
                              TARGET_RADIUS,
                              paint
                              );
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(_target._x,
                              _target._y,
                              _targetEvadeRadius,
                              paint
                              );
            }
         paint.setStyle(Paint.Style.FILL);
         paint.setColor(oldPaint);
         }
      }
   }
