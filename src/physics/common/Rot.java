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

import static java.lang.Math.sqrt;

import java.io.Serializable;

/**
 * Represents a rotation
 * 
 * @author Daniel
 */
public class Rot implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public double s, c; // sin and cos

	public Rot(double s, double c) {
		this.s = s;
		this.c = c;
	}
	
	public Rot() {
		setIdentity();
	}

	public Rot(double angle) {
		set(angle);
	}

	public double getSin() {
		return s;
	}

	public Rot inverse() {
		return new Rot(-s, c);
	}

	@Override
	public String toString() {
		return "Rot(s:" + s + ", c:" + c + ")";
	}

	public double getCos() {
		return c;
	}

	public Rot set(double angle) {
		s = Math.sin(angle);
		c = Math.cos(angle);
		return this;
	}

	public Rot set(Rot other) {
		s = other.s;
		c = other.c;
		return this;
	}

	public Rot setIdentity() {
		s = 0;
		c = 1;
		return this;
	}

	public double getAngle() {
		return Math.atan2(s, c);
	}

	public void getXAxis(Vec2 xAxis) {
		xAxis.set(c, s);
	}

	public void getYAxis(Vec2 yAxis) {
		yAxis.set(-s, c);
	}

	public void normalize() {
		final double sqLength = s * s + c * c;
		final double invLength = 1.0 / sqrt(sqLength);
		s *= invLength;
		c *= invLength;
	}
	
	public void addLocal(Rot a) {
		s += a.s;
		c += a.c;
	}
	
	public Rot perp() {
		double t = s;
		s = -c;
		c = t;
		return this;
	}
	
	@Override
	public Rot clone() {
		Rot copy = new Rot();
		copy.s = s;
		copy.c = c;
		return copy;
	}

	public static final void mul(Rot q, Rot r, Rot out) {
		double tempc = q.c * r.c - q.s * r.s;
		out.s = q.s * r.c + q.c * r.s;
		out.c = tempc;
	}

	public static final void mulUnsafe(Rot q, Rot r, Rot out) {
		assert (r != out);
		assert (q != out);
		// [qc -qs] * [rc -rs] = [qc*rc-qs*rs -qc*rs-qs*rc]
			// [qs qc] [rs rc] [qs*rc+qc*rs -qs*rs+qc*rc]
		// s = qs * rc + qc * rs
		// c = qc * rc - qs * rs
		out.s = q.s * r.c + q.c * r.s;
		out.c = q.c * r.c - q.s * r.s;
	}

	public static final void mulTrans(Rot q, Rot r, Rot out) {
		final double tempc = q.c * r.c + q.s * r.s;
		out.s = q.c * r.s - q.s * r.c;
		out.c = tempc;
	}

	public static final void mulTransUnsafe(Rot q, Rot r, Rot out) {
		// [ qc qs] * [rc -rs] = [qc*rc+qs*rs -qc*rs+qs*rc]
		// [-qs qc] [rs rc] [-qs*rc+qc*rs qs*rs+qc*rc]
		// s = qc * rs - qs * rc
		// c = qc * rc + qs * rs
		out.s = q.c * r.s - q.s * r.c;
		out.c = q.c * r.c + q.s * r.s;
	}

	public static final void mulToOut(Rot q, Vec2 v, Vec2 out) {
		double tempy = q.s * v.x + q.c * v.y;
		out.x = q.c * v.x - q.s * v.y;
		out.y = tempy;
	}

	public static final void mulToOutUnsafe(Rot q, Vec2 v, Vec2 out) {
		out.x = q.c * v.x - q.s * v.y;
		out.y = q.s * v.x + q.c * v.y;
	}

	public static final void mulTrans(Rot q, Vec2 v, Vec2 out) {
		final double tempy = -q.s * v.x + q.c * v.y;
		out.x = q.c * v.x + q.s * v.y;
		out.y = tempy;
	}

	public static final void mulTransUnsafe(Rot q, Vec2 v, Vec2 out) {
		out.x = q.c * v.x + q.s * v.y;
		out.y = -q.s * v.x + q.c * v.y;
	}
}
