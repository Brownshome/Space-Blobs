package math;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

public final class Vec3i {
	public static final Vec3i VEC3I_ZERO = new Vec3i();

	public final int x, y, z;

	public Vec3i() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Vec3i(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3i(final Vec3i vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}

	public Vec3i(int i) {
		this(i, i, i);
	}

	public Vec3i add(final Vec3i vec) {
		return new Vec3i(x + vec.x, y + vec.y, z + vec.z);
	}

	/**
	 * @param vec
	 * @return the angle between this and the given vector, in <em>radians</em>.
	 */
	public double angleInRadians(final Vec3i vec) {
		final double dot = dot(vec);
		final double lenSq = sqrt(getLengthSquared()
			* vec.getLengthSquared());
		return acos(dot / lenSq);
	}

	public Vec3i cross(final Vec3i vec) {
		return new Vec3i(y * vec.z - vec.y * z, z * vec.x - vec.z * x, x * vec.y
			- vec.x * y);
	}

	public int dot(final Vec3i vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	public Vec3i multiply(Vec3i s) {
		return new Vec3i(x*s.x, y*s.y, z*s.z);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Vec3i)) return false;

		final Vec3i other = (Vec3i) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;

		return true;
	}

	/**
	 * Get the coordinates of this Vec3I as a double array.
	 *
	 * @return new int[]{x, y, z};
	 */
	public int[] getArray() {
		return new int[] { x, y, z };
	}

	public static int getDimensions() {
		return 3;
	}

	public int getLengthSquared() {
		return x * x + y * y + z * z;
	}

	public Vec3i getNegated() {
		return new Vec3i(-x, -y, -z);
	}

	public Vec3 getUnitVector() {
		final int sqLength = getLengthSquared();
		final double invLength = 1.0 / sqrt(sqLength);

		return new Vec3(x * invLength, y * invLength, z * invLength);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	public Vec3i multiply(final int scalar) {
		return new Vec3i(x * scalar, y * scalar, z * scalar);
	}

	public Vec3i divide(final int s) {
		return new Vec3i(x/s,y/s,z/s);
	}

	public Vec3i subtract(final Vec3i vec) {
		return new Vec3i(x - vec.x, y - vec.y, z - vec.z);
	}

	@Override
	public String toString() {
		return "Vec3I{"+x+" "+y+" "+z+"}";
	}


	public Vec3 divide(Vec3i s) {
		return new Vec3((double)x/(double)s.x, (double)y/(double)s.y, (double)z/(double)s.z);
	}

	public Vec3 multiply(double s) {
		return new Vec3(x * s, y * s, z * s);
	}

	public Vec3 toVec3() {
		return new Vec3(x, y, z);
	}

	public Vec3i add(int x2, int y2, int z2) {
		return new Vec3i(x + x2, y + y2, z + z2);
	}

	public Vec3i mod(int d) {
		return new Vec3i(x % d, y % d, z % d);
	}
}
