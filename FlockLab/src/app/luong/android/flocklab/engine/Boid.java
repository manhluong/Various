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

import app.luong.android.flocklab.*;

public class Boid
   {
   public static final int MAX_GROUP_NUMBER = FlockView.FLOCK_SIZE/10;

   /**
    * Position of the boid.
    */
   Vector2D _position;
   
   /**
    * Velocity of the boid.
    */
   Vector2D _velocity;

   /**
    * Acceleration of the boid.
    */
   Vector2D _acc;

   /**
    * Dimension.
    */
   float _r;
   
   float _maxForce;
   
   float _maxSpeed;
   
   /**
    * Range used in separate() to determine if a boid is far enough.
    */
   float _sepRange;
   
   /**
    * Range used in alignment() to determine if a boid is aligned enough.
    */
   float _alignRange;
   
   /**
    * Range used in cohesion() to determine if a boid is near enough.
    */
   float _cohRange;
   
   /**
    * Weight for separation.
    */
   float _sepFactor;
   
   /**
    * Weight for alignment.
    */
   float _alignFactor;
   
   /**
    * Weight for cohesion.
    */
   float _cohFactor;
   
   /**
    * Radius of the target in seek mode.
    */
   float _seekRadius;
   
   /**
    * Radius of the target in evade mode.
    */
   float _evadeRadius;
   
   float _areaX;
   
   float _areaY;
   
   float _areaWidth;
   
   float _areaHeight;

   /**
    * 
    * @param areaX
    * @param areaY
    * @param areaWidth
    * @param areaHeight
    * @param posX
    * @param posY
    * @param maxSpeed
    * @param maxForce
    * @param startVelX
    * @param startVelY
    * @param radius
    */
   public Boid(float areaX,
               float areaY,
               float areaWidth,
               float areaHeight,
               float posX,
               float posY,
               float maxSpeed,
               float maxForce,
               float startVelX,
               float startVelY,
               float radius)
      {
      _areaX = areaX;
      _areaY = areaY;
      _areaWidth = areaWidth;
      _areaHeight = areaHeight;
      _acc = new Vector2D(0,0);
      _velocity = new Vector2D(startVelX, startVelY);
      _position = new Vector2D(posX, posY);
      _r = radius;
      _maxSpeed = maxSpeed;
      _maxForce = maxForce;
      _sepFactor = Flock.DEF_SEP_FACTOR;
      _alignFactor = Flock.DEF_ALIGN_FACTOR;
      _cohFactor = Flock.DEF_COH_FACTOR;
      _sepRange = Flock.DEF_SEP_RANGE;
      _alignRange = Flock.DEF_ALIGN_RANGE;
      _cohRange = Flock.DEF_COH_RANGE;
      _seekRadius = Flock.DEF_SEEK_RADIUS;
      _evadeRadius = Flock.DEF_EVADE_RADIUS;
      }

   /**
    * Update this boid related to all the other boids.
    */
   public void updateBoid(Boid boids[],
                          FlockCalcBuffers buffers,
                          int elapsedTime,
                          Vector2D target,
                          int targetType)
      {
      //Calculate acceleration vector.
      flock(boids, buffers);
      //Apply acceleration vector.
      updatePosition(elapsedTime);
      //Apply target influence.
      if(targetType!=Flock.INVISIBLE_TARGET)
         {
         if(targetType==Flock.CHASE_TARGET)
            seek(target, _seekRadius, buffers);
         else if(targetType==Flock.EVADE_TARGET)
            evade(target, _evadeRadius, buffers);
         }
      //Toroidal wrapping.
      wrapBorders(_areaX, _areaY, _areaWidth, _areaHeight);
      }
   
   /**
    * Update the weights and ranges for separation, alignment and cohesion.<br>
    * Update the radiuses for seek and evade.
    */
   public void updateParameters(float sf, float af, float cf,
                                float sr, float ar, float cr,
                                float ser, float er)
      {
      //Weights.
      _sepFactor = sf;
      _alignFactor = af;
      _cohFactor = cf;
      //Ranges.
      _sepRange = sr;
      _alignRange = ar;
      _cohRange = cr;
      //Radiuses.
      _seekRadius = ser;
      _evadeRadius = er;
      }
   
   /**
    * Modify position according to acceleration (velocity).
    */
   private void updatePosition(int elapsedTime)
      {
      //Update position.
      _velocity.mult(elapsedTime);
      _velocity.limit(_maxSpeed);
      _position.add(_velocity);//x = x + v*t
      //Update velocity.
      _acc.mult(elapsedTime);
      _acc.div(2);
      _velocity.add(_acc);//v = v + (a*t)/2
      //Reset acceleration value for next time.
      _acc._x = 0;
      _acc._y = 0;
      }

   /**
    * Calculate new acceleration based on the three rules of
    * separation, alignment and cohesion.
    */
   public void flock(Boid boids[],
                     FlockCalcBuffers buffers)
      {
      //Separation.
      buffers._flockSumBuffer = separate(boids, buffers);//Final result stored in buffers._flockSumBuffer
      buffers._flockSepBuffer._x = buffers._flockSumBuffer._x;
      buffers._flockSepBuffer._y = buffers._flockSumBuffer._y;
      //Alignment.
      buffers._flockSumBuffer = alignment(boids, buffers._flockSumBuffer);
      buffers._flockAliBuffer._x = buffers._flockSumBuffer._x;
      buffers._flockAliBuffer._y = buffers._flockSumBuffer._y;
      //Cohesion.
      buffers._flockSumBuffer = cohesion(boids, buffers);//Final result stored in buffers._flockSteerFleeBuffer
      buffers._flockCohBuffer._x = buffers._flockSumBuffer._x;
      buffers._flockCohBuffer._y = buffers._flockSumBuffer._y;
      //Weight these forces.
      buffers._flockSepBuffer.mult(_sepFactor);
      buffers._flockAliBuffer.mult(_alignFactor);
      buffers._flockCohBuffer.mult(_cohFactor);
      //Add the vectors to acceleration.
      _acc.add(buffers._flockSepBuffer);
      _acc.add(buffers._flockAliBuffer);
      _acc.add(buffers._flockCohBuffer);
      }

   public void seek(Vector2D target, float cushion, FlockCalcBuffers buffers)
      {
      _acc.add(steer(_position, target, cushion, buffers, _maxSpeed, _maxForce, _velocity, _areaWidth, _areaHeight, false));
      }
   
   public void evade(Vector2D target, float cushion, FlockCalcBuffers buffers)
      {
      _acc.add(steer(target, _position, cushion, buffers, _maxSpeed, _maxForce, _velocity, _areaWidth, _areaHeight, true));
      }
   
   /**
    * Steer or flee towards / from a target.<br>
    * Pass a cushion of 0 to send the boid on the target.
    * @param buffers Use _flockSteerFleeBuffer and _flockVectCalcBuffer
    */
   public static Vector2D steer(Vector2D position,
                                Vector2D target,
                                float cushion,
                                FlockCalcBuffers buffers,
                                float maxSpeed,
                                float maxForce,
                                Vector2D velocity,
                                float width,
                                float height,
                                boolean flee)
      {
      //buffers._flockVectCalcBuffer contains the desired vector.
      buffers._flockVectCalcBuffer._x = 0;
      buffers._flockVectCalcBuffer._y = 0;
      Vector2D.sub(target, position, buffers._flockVectCalcBuffer);
      //Distance = magnitude.
      float distance = buffers._flockVectCalcBuffer.magnitude();
      //Steering is 0 vector if magnitude (distance) is 0.
      if ((!flee && (distance > cushion)) ||//Steer.
          (flee && (distance > 0) && (distance < cushion)))//Flee
         {
         //Steering = Desired - Speed.
         Vector2D.sub(buffers._flockVectCalcBuffer, velocity, buffers._flockSteerFleeBuffer);
         //Normalize, adjust magnitude and limit.
         buffers._flockSteerFleeBuffer.normalize();
         buffers._flockSteerFleeBuffer.mult(maxSpeed);
         buffers._flockSteerFleeBuffer.limit(maxForce);
         }
      else
         {
         buffers._flockSteerFleeBuffer._x = 0;
         buffers._flockSteerFleeBuffer._y = 0;
         }
      return buffers._flockSteerFleeBuffer;
      }

   public void wrapBorders(float startX,
                           float startY,
                           float width,
                           float height)
      {
      if (_position._x < startX-_r)
         _position._x = width+_r;
      if (_position._y < startY-_r)
         _position._y = height+_r;
      if (_position._x > width+_r)
         _position._x = startX-_r;
      if (_position._y > height+_r)
         _position._y = startY-_r;
      }
   
   /**
    * Separation.<br>
    * Each boid must avoid collision with other boids.
    */
   public Vector2D separate (Boid boids[],
                             FlockCalcBuffers buffers)
      {
      //Reset buffer.
      buffers._flockSumBuffer._x = 0;
      buffers._flockSumBuffer._y = 0;
      float count = 0;
         for (int i = 0 ; i < boids.length; i++)
            {
            //float distance = Vector2D.distance2(_position, boids[i]._position);
            float distance = Vector2D.distance2Wrap(_position, boids[i]._position, _areaWidth, _areaHeight);
            //Make sense only if there is a suitable distance.
            if ((distance > 0) && (distance < (_sepRange*_sepRange)))
               {
               //Vector pointing away from the other boid.
               Vector2D.sub(_position, boids[i]._position, buffers._flockVectCalcBuffer);
               //buffers._flockVectCalcBuffer.normalize();
               //Use distance as weight.
               buffers._flockVectCalcBuffer.div(android.util.FloatMath.sqrt(distance));
               buffers._flockSumBuffer.add(buffers._flockVectCalcBuffer);
               count++;
               }
            if(count==MAX_GROUP_NUMBER)
               break;
            }
      //Average
      if (count > 0)
         {
         buffers._flockSumBuffer.div(count);
         //Normalize, adjust magnitude and limit.
         buffers._flockSumBuffer.normalize();
         buffers._flockSumBuffer.mult(_maxSpeed);
         buffers._flockSumBuffer.limit(_maxForce);
         }
      return buffers._flockSumBuffer;
      }

   /**
    * Alignment.<br>
    * It is the average speed of the nearby boids.
    */
   public Vector2D alignment(Boid boids[],
                             Vector2D flockBuffer)
      {
      //Reset buffer.
      flockBuffer._x = 0;
      flockBuffer._y = 0;
      float count = 0;
         for (int i = 0 ; i < boids.length; i++)
            {
            //float distance = Vector2D.distance2(_position, boids[i]._position);
            float distance = Vector2D.distance2Wrap(_position, boids[i]._position, _areaWidth, _areaHeight);
            if ((distance > 0) && (distance < (_alignRange*_alignRange)))
               {
               //Get the same velocity of the other boid.
               flockBuffer.add(boids[i]._velocity);
               count++;
               }
            if(count==MAX_GROUP_NUMBER)
               break;
            }
      if (count > 0)
         {
         flockBuffer.div(count);
         //Normalize, adjust magnitude and limit.
         flockBuffer.normalize();
         flockBuffer.mult(_maxSpeed);
         flockBuffer.limit(_maxForce);
         }
      return flockBuffer;
      }

   /**
    * Cohesion.<br>
    * All boids must remain nearby.<br>
    * Steer each boid to the center of the local flock.<br>
    * Center = avarage of all locations.
    */
   public Vector2D cohesion (Boid boids[],
                             FlockCalcBuffers buffers)
      {
      //Reset buffer.
      buffers._flockSumBuffer._x = 0;
      buffers._flockSumBuffer._y = 0;
      float count = 0;
         for (int i = 0 ; i < boids.length; i++)
            {
            //float distance = Vector2D.distance2(_position, boids[i]._position);
            float distance = Vector2D.distance2Wrap(_position, boids[i]._position, _areaWidth, _areaHeight);
            //Only the local flock.
            if ((distance > 0) && (distance < (_cohRange*_cohRange)))
               {
               //Sum all locations together.
               buffers._flockSumBuffer.add(boids[i]._position);
               count++;
               }
            if(count==MAX_GROUP_NUMBER)
               break;
            }
      if (count > 0)
         {
         //Average the locations.
         buffers._flockSumBuffer.div(count);
         return steer(_position,
                      buffers._flockSumBuffer,
                      0,
                      buffers,
                      _maxSpeed,
                      _maxForce,
                      _velocity,
                      _areaWidth,
                      _areaHeight,
                      false);// Steer towards the location
         }
      return buffers._flockSumBuffer;
      }
   
   public String toString()
      {
      return "Position: "+_position;
      }
   }
