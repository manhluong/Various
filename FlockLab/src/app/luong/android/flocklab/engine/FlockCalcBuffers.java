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

/**
 * Collection of Vector2D instances passed to flock calculations to avoid "new" calls.<br>
 * Used to avoid to pass lots of Vector2D parameters.
 */
public class FlockCalcBuffers
   {
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    */
   Vector2D _flockSumBuffer;
   
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    * Used to store vector math calculations.
    */
   Vector2D _flockVectCalcBuffer;
   
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    * Used in steer() and flee().
    */
   Vector2D _flockSteerFleeBuffer;
   
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    */
   Vector2D _flockSepBuffer;
   
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    */
   Vector2D _flockAliBuffer;
   
   /**
    * Buffer used by internal flock algorithm, in the Boid class.<br>
    */
   Vector2D _flockCohBuffer;
   
   public FlockCalcBuffers()
      {
      _flockSumBuffer = new Vector2D(0, 0);
      _flockVectCalcBuffer = new Vector2D(0, 0);
      _flockSteerFleeBuffer = new Vector2D(0, 0);
      _flockSepBuffer = new Vector2D(0, 0);
      _flockAliBuffer = new Vector2D(0, 0);
      _flockCohBuffer = new Vector2D(0, 0);
      }
   }
