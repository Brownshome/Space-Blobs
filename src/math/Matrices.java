/* Copyright (C) 2013 James L. Royalty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

/**
 * Utility methods that replace OpenGL and GLU matrix functions there were
 * deprecated in GL 3.0.
 *
 * @author James Royalty
 */
public final class Matrices {
	/**
	 * Creates a perspective projection matrix (frustum) using explicit values
	 * for all clipping planes. This method is analogous to the now deprecated
	 * {@code glFrustum} method.
	 *
	 * @param left
	 *            left vertical clipping plane
	 * @param right
	 *            right vertical clipping plane
	 * @param bottom
	 *            bottom horizontal clipping plane
	 * @param top
	 *            top horizontal clipping plane
	 * @param nearVal
	 *            distance to the near depth clipping plane (must be positive)
	 * @param farVal
	 *            distance to the far depth clipping plane (must be positive)
	 * @return
	 */
	public static final Mat4 frustum(final double left, final double right,
		final double bottom, final double top, final double nearVal,
		final double farVal) {
		final double m00 = 2f * nearVal / (right - left);
		final double m11 = 2f * nearVal / (top - bottom);
		final double m20 = (right + left) / (right - left);
		final double m21 = (top + bottom) / (top - bottom);
		final double m22 = -(farVal + nearVal) / (farVal - nearVal);
		final double m23 = -1f;
		final double m32 = -(2f * farVal * nearVal) / (farVal - nearVal);

		return new Mat4(m00, 0f, 0f, 0f, 0f, m11, 0f, 0f, m20, m21, m22, m23,
			0f, 0f, m32, 0f);
	}

	/**
	 * Defines a viewing transformation. This method is analogous to the now
	 * deprecated {@code gluLookAt} method.
	 *
	 * @param eye
	 *            position of the eye point
	 * @param center
	 *            position of the reference point
	 * @param up
	 *            direction of the up vector
	 * @return
	 */
	public static final Mat4 lookAt(final Vec3 eye, final Vec3 center,
		final Vec3 up) {
		final Vec3 f = center.subtract(eye).normalize();
		Vec3 u = up.normalize();
		final Vec3 s = f.cross(u).normalize();
		u = s.cross(f);

		return new Mat4(s.x, u.x, -f.x, 0f, s.y, u.y, -f.y, 0f, s.z, u.z, -f.z,
			0f, -s.dot(eye), -u.dot(eye), f.dot(eye), 1f);
	}

	/**
	 * Creates an orthographic projection matrix. This method is analogous to
	 * the now deprecated {@code glOrtho} method.
	 *
	 * @param left
	 *            left vertical clipping plane
	 * @param right
	 *            right vertical clipping plane
	 * @param bottom
	 *            bottom horizontal clipping plane
	 * @param top
	 *            top horizontal clipping plane
	 * @param zNear
	 *            distance to nearer depth clipping plane (negative if the plane
	 *            is to be behind the viewer)
	 * @param zFar
	 *            distance to farther depth clipping plane (negative if the
	 *            plane is to be behind the viewer)
	 * @return
	 */
	public static final Mat4 ortho(final double left, final double right,
		final double bottom, final double top, final double zNear,
		final double zFar) {
		final double m00 = 2f / (right - left);
		final double m11 = 2f / (top - bottom);
		final double m22 = -2f / (zFar - zNear);
		final double m30 = -(right + left) / (right - left);
		final double m31 = -(top + bottom) / (top - bottom);
		final double m32 = -(zFar + zNear) / (zFar - zNear);

		return new Mat4(m00, 0f, 0f, 0f, 0f, m11, 0f, 0f, 0f, 0f, m22, 0f, m30,
			m31, m32, 1f);
	}

	/**
	 * Creates a 2D orthographic projection matrix. This method is analogous to
	 * the now deprecated {@code gluOrtho2D} method.
	 *
	 * @param left
	 *            left vertical clipping plane
	 * @param right
	 *            right vertical clipping plane
	 * @param bottom
	 *            bottom horizontal clipping plane
	 * @param top
	 *            top horizontal clipping plane
	 * @return
	 */
	public static final Mat4 ortho2d(final double left, final double right,
		final double bottom, final double top) {
		final double m00 = 2f / (right - left);
		final double m11 = 2f / (top - bottom);
		final double m22 = -1f;
		final double m30 = -(right + left) / (right - left);
		final double m31 = -(top + bottom) / (top - bottom);

		return new Mat4(m00, 0f, 0f, 0f, 0f, m11, 0f, 0f, 0f, 0f, m22, 0f, m30,
			m31, 0f, 1f);
	}

	/**
	 * Creates a perspective projection matrix using field-of-view and aspect
	 * ratio to determine the left, right, top, bottom planes. This method is
	 * analogous to the now deprecated {@code gluPerspective} method.
	 *
	 * @param fovy
	 *            field of view angle, in degrees, in the {@code y} direction
	 * @param aspect
	 *            aspect ratio that determines the field of view in the x
	 *            direction. The aspect ratio is the ratio of {@code x} (width)
	 *            to {@code y} (height).
	 * @param zNear
	 *            near plane distance from the viewer to the near clipping plane
	 *            (always positive)
	 * @param zFar
	 *            far plane distance from the viewer to the far clipping plane
	 *            (always positive)
	 * @return
	 */
	public static final Mat4 perspective(final double fovy, final double aspect, final double zNear, final double zFar) {
		final double halfFovyRadians = toRadians(fovy / 2.0f);
		final double range = tan(halfFovyRadians) * zNear;
		final double left = -range * aspect;
		final double right = range * aspect;
		final double bottom = -range;
		final double top = range;

		return new Mat4(
			2f * zNear / (right - left), 	0f, 							0f, 									0f,
			0f, 							2f * zNear / (top - bottom), 	0f, 									0f,
			0f, 							0f, 							-(zFar + zNear) / (zFar - zNear), 		-1f,
			0f, 							0f, 							-(2f * zFar * zNear) / (zFar - zNear), 	0f
			);
	}

	/**
	 * Creates a rotation matrix for the given angle (in rad) around the given
	 * axis. <br>
	 * <br>
	 * Changed to return a 3*3 matrix (J. Brown)
	 *
	 * @param phi
	 *            The angle (in rad).
	 * @param axis
	 *            The axis to rotate around. Must be a unit-axis.
	 * @return This matrix, rotated around the given axis.
	 */
	public static Mat3 rotate3D(final double phi, final Vec3 axis) {
		double rcos = cos(phi);
		double rsin = sin(phi);
		double x = axis.x;
		double y = axis.y;
		double z = axis.z;
		Vec3 v1 = new Vec3(rcos + x * x * (1 - rcos), z
			* rsin + y * x * (1 - rcos), -y * rsin + z * x
			* (1 - rcos));
		Vec3 v2 = new Vec3(-z * rsin + x * y * (1 - rcos),
			rcos + y * y * (1 - rcos), x * rsin + z * y
			* (1 - rcos));
		Vec3 v3 = new Vec3(y * rsin + x * z * (1 - rcos), -x
			* rsin + y * z * (1 - rcos), rcos + z * z
			* (1 - rcos));
		return new Mat3(v1, v2, v3);
	}

	/** A method for getting a matrix converting points to be representated in different co-ordinate systems
	 *
	 * @param x The normalized vector for the new basis (x)
	 * @param y The normalized vector for the new basis (y)
	 * @param z The normalized vector for the new basis (z)
	 * @return A 3x3 Matrix that will transform a point from the normal system to it's representation in the new system
	 */
	public static Mat3 transformBasis(Vec3 x, Vec3 y, Vec3 z) {
		return new Mat3(x,y,z).transpose(); //using the transpose opporation as a replacement to invert.
	}

	/** Returns a matrix that rotates a point phi radians around (0, 0) counter-clockwise
	 *
	 * @param phi The angle in radians
	 * @return The 2x2 transformation matrix
	 */
	public static Mat2 rotate2D(final double phi) {
		double cos = cos(phi);
		double sin = sin(phi);

		return new Mat2(cos, -sin, sin, cos);
	}

	/** transforms the point using the other method of the same name
	 *
	 * @param x The normalized vector for the new basis (x)
	 * @param y The normalized vector for the new basis (y)
	 * @param z The normalized vector for the new basis (z)
	 * @param vec The vector to be transformed
	 * @return The result of this transformation
	 */
	public static Vec3 transformBasis(Vec3 x, Vec3 y, Vec3 z, Vec3 vec) {
		return transformBasis(x, y, z).multiply(vec); //OPTI break down into base opperations to avoid creating a matrix
	}
}
