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

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.nio.FloatBuffer;

/**
 * @author James Royalty
 */
public final class Vec3 implements Vec {
	public static final Vec3 VEC3_ZERO = new Vec3();
	public static final Vec3 VEC3_UP = new Vec3(0, 1, 0);
	public static final Vec3 VEC3_FORWARD = new Vec3(0, 0, 1); //actually backward (don't tell anyone)

	public final double x, y, z;

	public Vec3() {
		x = 0f;
		y = 0f;
		z = 0f;
	}

	public Vec3(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(final Vec3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vec3(Vec4 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vec3(double i) {
		x = y = z = i;
	}

	public Vec3(Color colour) {
		x = colour.getRed() / 255.0;
		y = colour.getGreen() / 255.0;
		z = colour.getBlue() / 255.0;
	}

	public Vec3 add(final Vec3 vec) {
		return new Vec3(x + vec.x, y + vec.y, z + vec.z);
	}

	/**
	 * @param vec
	 * @return the angle between this and the given vector, in <em>radians</em>.
	 */
	public double angleInRadians(final Vec3 vec) {
		final double dot = dot(vec);
		final double lenSq = sqrt(lengthSquared() * vec.lengthSquared());
		return acos(dot / lenSq);
	}

	public Vec3 cross(final Vec3 vec) {
		return new Vec3(y * vec.z - vec.y * z, z * vec.x - vec.z * x, x * vec.y - vec.x * y);
	}

	public double dot(final Vec3 vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Vec3)) return false;

		final Vec3 other = (Vec3) obj;
		return x == other.x && y == other.y && z == other.z;
	}

	/**
	 * Get the coordinates of this Vec3 as a double array.
	 *
	 * @return new double[]{x, y, z};
	 */
	public float[] getArray() {
		return new float[] {(float) x, (float) y, (float) z};
	}

	@Override
	public FloatBuffer getBuffer() {
		final FloatBuffer buffer = allocateFloatBuffer();
		final int startPos = buffer.position();

		buffer.put((float) x).put((float) y).put((float) z);

		buffer.position(startPos);

		return buffer;
	}

	@Override
	public int getDimensions() {
		return 3;
	}

	@Override
	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	public Vec3 getNegated() {
		return new Vec3(-x, -y, -z);
	}

	public Vec3 normalize() {
		final double sqLength = lengthSquared();
		final double invLength = 1.0 / sqrt(sqLength);

		return new Vec3(x * invLength, y * invLength, z * invLength);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits((float) x);
		result = prime * result + Float.floatToIntBits((float) y);
		result = prime * result + Float.floatToIntBits((float) z);
		return result;
	}

	public Vec3 lerp(final Vec3 vec, final double amount) {
		final double diff = 1f - amount;
		return new Vec3(diff * x + amount * vec.x, diff * y + amount * vec.y,
			diff * z + amount * vec.z);
	}

	public Vec3 divide(final double s) {
		double recp = 1.0f / s;
		return scale(recp);
	}

	public Vec3 multiply(final Mat3 mat) {
		return new Vec3(mat.m00 * x + mat.m01 * y + mat.m02 * z, mat.m10 * x
			+ mat.m11 * y + mat.m12 * z, mat.m20 * x + mat.m21 * y
			+ mat.m22 * z);
	}

	public Vec3 scale(final double scalar) {
		return new Vec3(scalar * x, scalar * y, scalar * z);
	}

	public Vec3 scale(Vec3 v) {
		return new Vec3(v.x * x, v.y * y, v.z * z);
	}

	public Vec3 scaleAbout(final double scalar, final Vec3 about) {
		return subtract(about).scale(scalar).add(about);
	}

	public Vec3 scaleAbout(final Vec3 scalar, final Vec3 about) {
		return subtract(about).cross(scalar).add(about);
	}

	public Vec3 subtract(final Vec3 vec) {
		return new Vec3(x - vec.x, y - vec.y, z - vec.z);
	}

	public Vec4 toDirection() {
		return new Vec4(x, y, z, 0.0);
	}

	public Vec4 toPoint() {
		return new Vec4(x, y, z, 1.0);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName())
			.append("{")
			.append(String.format("%8.5f %8.5f %8.5f", x, y, z))
			.append("}").toString();
	}

	public Vec3i getVec3I() {
		return new Vec3i((int)x,(int)y,(int)z);
	}

	public boolean isMultipleOf(Vec3 vec) {
		return divide(vec).isDiagonal();
	}

	private boolean isDiagonal() {
		return x == y && y == z; //use errors if possible
	}

	public Vec3 divide(Vec3 vec) {
		return new Vec3(x / vec.x, y / vec.y, z / vec.z);
	}

	/**Adds f to every component of the vector
	 *
	 * @param f
	 * @return
	 */
	public Vec3 add(double f) {
		return new Vec3(x + f, y + f, z + f);
	}

	/**Returns true if all components are smaller than or equal to vec*/
	public boolean isSmallerThan(Vec3 vec) {
		return x <= vec.x && y <= vec.y && z <= vec.z;
	}

	/**Returns true if all components are greater than or equal to vec*/
	public boolean isGreaterThan(Vec3 vec) {
		return x >= vec.x && y >= vec.y && z >= vec.z;
	}

	public double[] fillArray(double[] coords, int i) {
		coords[i] = x;
		coords[i + 1] = y;
		coords[i + 2] = z;

		return coords;
	}

	public Vec3 add(double x2, double y2, double z2) {
		return new Vec3(x + x2, y + y2, z + z2);
	}

	public Vec3 negate() {
		return new Vec3(-x, -y, -z);
	}

	public Mat3 hat() {
		return new Mat3(0,z,-y,  -z,0,x,  y,-x,0);
	}

	static final double ELIPSON = 1e-10;
	public boolean equalWithElipson(Vec3 v) {
		return Math.abs(v.x - x) < ELIPSON && Math.abs(v.y - y) < ELIPSON && Math.abs(v.z - z) < ELIPSON;
	}
}
