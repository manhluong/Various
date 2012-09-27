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
 * Simple 2D Vector class.<br>
 * Avoid unnecessary encapsulation.
 */
public class Vector2D
   {
   
   public float _x;

   public float _y;

   public Vector2D()
      {
      _x = 0;
      _y = 0;
      }
   
   public Vector2D(float x, float y)
      {
      _x = x;
      _y = y;
      }

   public float magnitude()
      {
      return android.util.FloatMath.sqrt(_x*_x + _y*_y);
      }

   public void add(Vector2D v)
      {
      _x += v._x;
      _y += v._y;
      }

   public void sub(Vector2D v)
      {
      _x -= v._x;
      _y -= v._y;
      }

   public void mult(float n)
      {
      _x *= n;
      _y *= n;
      }

   public void div(float n)
      {
      _x /= n;
      _y /= n;
      }

   /**
    * Check that magnitude() is > 0 first.
    */
   public void normalize()
      {
      float m = magnitude();
      div(m);
      }

   public void limit(float max)
      {
      if (magnitude() > max)
         {
         normalize();
         mult(max);
         }
      }

   public static void add(Vector2D v1, Vector2D v2, Vector2D res)
      {
      res._x = v1._x + v2._x;
      res._y = v1._y + v2._y;
      }

   public static void sub(Vector2D v1, Vector2D v2, Vector2D res)
      {
      res._x = v1._x - v2._x;
      res._y = v1._y - v2._y;
      }

   public static void div(Vector2D v1, float n, Vector2D res)
      {
      res._x = v1._x / n;
      res._y = v1._y / n;
      }

   public static void mult(Vector2D v1, float n, Vector2D res)
      {
      res._x = v1._x * n;
      res._y = v1._y * n;
      }
   
   /**
    * Avoid square roots because is processor intensive.<br>
    * Take account of toroidal wrapping.<br>
    * Math in the comments of:
    * <a href=http://www.wildbunny.co.uk/blog/2012/04/13/implementing-a-wrap-around-world/>Implementing a wrap-around world</a>
    * @param v1
    * @param v2
    * @param width
    * @param height
    * @return
    */
   public static float distance2Wrap(Vector2D v1, Vector2D v2, float width, float height)
      {
      float dx = Math.abs(v1._x - v2._x);
      float dy = Math.abs(v1._y - v2._y);
      //If the delta surpass half of the size, then
      //the shortest delta is the the one that goes the other way around.
      if(dx > (width / 2))
         dx += width;
      if(dy > (height / 2))
         dy += height;
      return (dx*dx + dy*dy);
      }

   public String toString()
      {
      return "["+_x+"; "+_y+"]";
      }
   }