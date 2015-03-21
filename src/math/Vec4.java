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

import static java.lang.Math.sqrt;

import java.nio.FloatBuffer;

/**
 * @author James Royalty
 */
public class Vec4 implements Vec {
	public static final Vec4 VEC4_ZERO = new Vec4();

	public final double x, y, z, w;

	public Vec4() {
		x = 0.0;
		y = 0.0;
		z = 0.0;
		w = 0.0;
	}

	public Vec4(final double x, final double y, final double z, final double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vec4(final Vec3 other, final double w) {
		x = other.x;
		y = other.y;
		z = other.z;
		this.w = w;
	}

	public Vec4(final Vec4 other) {
		x = other.x;
		y = other.y;
		z = other.z;
		w = other.w;
	}

	public Vec4 add(final Vec4 vec) {
		return new Vec4(x + vec.x, y + vec.y, z + vec.z, w + vec.w);
	}

	public double dot(final Vec4 vec) {
		return x * vec.x + y * vec.y + z * vec.z + w * vec.w;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Vec4)) return false;
		Vec4 other = (Vec4) obj;
		return x == other.x && y == other.y && z == other.z && w == other.w;
	}

	@Override
	public FloatBuffer getBuffer() {
		final FloatBuffer buffer = allocateFloatBuffer();
		final int startPos = buffer.position();

		buffer.put((float) x).put((float) y).put((float) z).put((float) x);

		buffer.position(startPos);

		return buffer;
	}

	@Override
	public int getDimensions() {
		return 4;
	}

	@Override
	public double lengthSquared() {
		return x * x + y * y + z * z + w * w;
	}

	public Vec4 getNegated() {
		return new Vec4(-x, -y, -z, -w);
	}

	public Vec4 getUnitVector() {
		final double sqLength = lengthSquared();
		final double invLength = 1.0 / sqrt(sqLength);

		return new Vec4(x * invLength, y * invLength, z * invLength, w * invLength);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits((float) w);
		result = prime * result + Float.floatToIntBits((float) x);
		result = prime * result + Float.floatToIntBits((float) y);
		result = prime * result + Float.floatToIntBits((float) z);
		return result;
	}

	public Vec4 multiply(final double scalar) {
		return new Vec4(x * scalar, y * scalar, z * scalar, w * scalar);
	}

	public Vec4 scale(final double scalar) {
		return multiply(scalar);
	}

	public Vec4 subtract(final Vec4 vec) {
		return new Vec4(x - vec.x, y - vec.y, z - vec.z, w - vec.w);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName())
			.append("{").append(x).append(", ").append(y).append(", ")
			.append(z).append(", ").append(w).append("}").toString();
	}
}
