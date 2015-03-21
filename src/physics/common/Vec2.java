/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package physics.common;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

import java.io.Serializable;

import com.sun.javafx.geom.Point2D;

/**
 * A 2D column vector
 */
public class Vec2 implements Serializable {
	private static final long serialVersionUID = 1L;

	public double x, y;

	public Vec2() {
		x = 0.0;
		y = 0.0;
	}

	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2(Point2D p) {
		x = p.x;
		y = p.y;
	}

	public Vec2(Vec2 toCopy) {
		this(toCopy.x, toCopy.y);
	}

	public Vec2(double d) {
		x = y = d;
	}

	/** Zero out this vector. */
	public final void setZero() {
		x = 0.0;
		y = 0.0;
	}

	/** Set the vector component-wise. */
	public final Vec2 set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/** Set this vector to another vector. */
	public final Vec2 set(Vec2 v) {
		this.x = v.x;
		this.y = v.y;
		return this;
	}

	/** Return the sum of this vector and another; does not alter either one. */
	public final Vec2 add(Vec2 v) {
		return new Vec2(x + v.x, y + v.y);
	}

	/** Return the difference of this vector and another; does not alter either one. */
	public final Vec2 sub(Vec2 v) {
		return new Vec2(x - v.x, y - v.y);
	}

	/** Return this vector multiplied by a scalar; does not alter this vector. */
	public final Vec2 mul(double a) {
		return new Vec2(x * a, y * a);
	}

	/**
	 * @param vec
	 * @return the angle between this and the given vector, in <em>radians</em>.
	 */
	public double angleInRadians(final Vec2 vec) {
		final double dot = dot(vec);
		final double lenSq = sqrt(lengthSquared() * vec.lengthSquared());
		return acos(dot / lenSq);
	}

	public double dot(final Vec2 vec) {
		return x * vec.x + y * vec.y;
	}
	
	/** Return the negation of this vector; does not alter this vector. */
	public final Vec2 negate() {
		return new Vec2(-x, -y);
	}

	/** Flip the vector and return it - alters this vector. */
	public final Vec2 negateLocal() {
		x = -x;
		y = -y;
		return this;
	}

	/** Add another vector to this one and returns result - alters this vector. */
	public final Vec2 addLocal(Vec2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/** Adds values to this vector and returns result - alters this vector. */
	public final Vec2 addLocal(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vec2 scaleAbout(double scalar, Vec2 about) {
		return sub(about).mul(scalar).sub(about);
	}
	
	/** Subtract another vector from this one and return result - alters this vector. */
	public final Vec2 subLocal(Vec2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	/** Multiply this vector by a number and return result - alters this vector. */
	public final Vec2 mulLocal(double a) {
		x *= a;
		y *= a;
		return this;
	}

	/** Get the skew vector such that dot(skew_vec, other) == cross(vec, other) */
	public final Vec2 skew() {
		return new Vec2(-y, x);
	}

	/** Get the skew vector such that dot(skew_vec, other) == cross(vec, other) */
	public final void skew(Vec2 out) {
		out.x = -y;
		out.y = x;
	}

	/** Return the length of this vector. */
	public final double length() {
		return Math.sqrt(x * x + y * y);
	}

	/** Return the squared length of this vector. */
	public final double lengthSquared() {
		return (x * x + y * y);
	}

	public Vec2 mul(double a, double b) {
		return new Vec2(a * x, b * y);
	}
	
	public Vec2 add(double x2, double y2) {
		return new Vec2(x + x2, y + y2);
	}

	public Vec2 mul(Vec2 v) {
		return new Vec2(v.x * x, v.y * y);
	}
	
	/** Normalize this vector and return the length before normalization. Alters this vector. */
	public final double normalize() {
		double length = length();
		if (length < Settings.EPSILON) {
			return 0f;
		}

		double invLength = 1.0f / length;
		x *= invLength;
		y *= invLength;
		return length;
	}

	/** True if the vector represents a pair of valid, non-infinite doubleing point numbers. */
	public final boolean isValid() {
		return !Double.isNaN(x) && !Double.isInfinite(x) && !Double.isNaN(y) && !Double.isInfinite(y);
	}

	/** Return a new vector that has positive components. */
	public final Vec2 abs() {
		return new Vec2(Math.abs(x), Math.abs(y));
	}

	public final void absLocal() {
		x = Math.abs(x);
		y = Math.abs(y);
	}

	/** Return a copy of this vector. */
	@Override
	public final Vec2 clone() {
		return new Vec2(x, y);
	}

	@Override
	public final String toString() {
		return "(" + String.format("%.2f", x) + "," + String.format("%.2f", y) + ")";
	}

	/*
	 * Static
	 */

	public final static Vec2 abs(Vec2 a) {
		return new Vec2(Math.abs(a.x), Math.abs(a.y));
	}

	public final static void absToOut(Vec2 a, Vec2 out) {
		out.x = Math.abs(a.x);
		out.y = Math.abs(a.y);
	}

	public final static double dot(Vec2 a, Vec2 b) {
		return a.x * b.x + a.y * b.y;
	}

	public final static double cross(Vec2 a, Vec2 b) {
		return a.x * b.y - a.y * b.x;
	}

	public final static Vec2 cross(Vec2 a, double s) {
		return new Vec2(s * a.y, -s * a.x);
	}

	public final static void crossToOut(Vec2 a, double s, Vec2 out) {
		final double tempy = -s * a.x;
		out.x = s * a.y;
		out.y = tempy;
	}

	public final static void crossToOutUnsafe(Vec2 a, double s, Vec2 out) {
		assert (out != a);
		out.x = s * a.y;
		out.y = -s * a.x;
	}

	public final static Vec2 cross(double s, Vec2 a) {
		return new Vec2(-s * a.y, s * a.x);
	}

	public final static void crossToOut(double s, Vec2 a, Vec2 out) {
		final double tempY = s * a.x;
		out.x = -s * a.y;
		out.y = tempY;
	}

	public final static void crossToOutUnsafe(double s, Vec2 a, Vec2 out) {
		assert (out != a);
		out.x = -s * a.y;
		out.y = s * a.x;
	}

	public final static void negateToOut(Vec2 a, Vec2 out) {
		out.x = -a.x;
		out.y = -a.y;
	}

	public final static Vec2 min(Vec2 a, Vec2 b) {
		return new Vec2(a.x < b.x ? a.x : b.x, a.y < b.y ? a.y : b.y);
	}

	public final static Vec2 max(Vec2 a, Vec2 b) {
		return new Vec2(a.x > b.x ? a.x : b.x, a.y > b.y ? a.y : b.y);
	}

	public final static void minToOut(Vec2 a, Vec2 b, Vec2 out) {
		out.x = a.x < b.x ? a.x : b.x;
		out.y = a.y < b.y ? a.y : b.y;
	}

	public final static void maxToOut(Vec2 a, Vec2 b, Vec2 out) {
		out.x = a.x > b.x ? a.x : b.x;
		out.y = a.y > b.y ? a.y : b.y;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long xl = Double.doubleToLongBits(x);
		long yl = Double.doubleToLongBits(y);

		result += prime * xl & 0xffffffff;
		result += prime * yl;
		result += prime * (xl >> 32);
		result += prime * (yl >> 32);

		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) { // automatically generated by Eclipse
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vec2 other = (Vec2) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) return false;
		return true;
	}

	public final static double distanceSquared(Vec2 v1, Vec2 v2) {
		double dx = (v1.x - v2.x);
		double dy = (v1.y - v2.y);
		return dx * dx + dy * dy;
	}

	public final static double distance(Vec2 v1, Vec2 v2) {
		return Math.sqrt(Vec2.distanceSquared(v1, v2));
	}

	public Vec2 lerp(Vec2 v, double t) {
		return new Vec2(x * (1 - t) + v.x * t, y * (1 - t) + v.y * t);
	}

	public Vec2 getUnitVector() {
		Vec2 v = clone();
		v.normalize();
		return v;
	}
}
