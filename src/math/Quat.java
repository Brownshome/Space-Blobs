package math;

import static java.lang.Math.sqrt;

/* Shamlessly addapted from the lwjgl_util package
 *
 */


public class Quat {
	public final static Quat IDENTITY = new Quat();

	public final double x, y, z, w;

	/**
	 * The quaternion will be initialized to the identity.
	 */
	public Quat() {
		x = y = z = 0;
		w = 1;
	}

	public Quat(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quat(Vec3 v, double w) {
		x = v.x;
		y = v.y;
		z = v.z;
		this.w = w;
	}

	/**
	 * Calculate the conjugate of this quaternion.
	 */

	public Quat conj() {
		return new Quat(-x, -y, -z, w);
	}

	/**
	 * Sets the value of this quaternion to the quaternion product of
	 * quaternions left and right (this = left * right). Note that this is safe
	 * for aliasing (e.g. this can be left or right).
	 *
	 * @param left
	 *            the first quaternion
	 * @param right
	 *            the second quaternion
	 */
	public Quat mul(Quat right) {
		return new Quat(
			x * right.w + w * right.x + y * right.z - z * right.y,
			y * right.w + w * right.y + z * right.x - x * right.z,
			z * right.w + w * right.z + x * right.y - y * right.x,
			w * right.w - x * right.x - y * right.y - z * right.z);
	}

	/**
	 *
	 * Multiplies this quaternion by the inverse of quaternion right and returns
	 * the value. (this * right^-1).
	 *
	 * @param right
	 *            the right quaternion
	 */
	public Quat mulInverse(Quat right) {
		return new Quat(x * right.w - w * right.x - y * right.z + z * right.y,
			y * right.w - w * right.y - z * right.x + x * right.z,
			z * right.w - w * right.z - x	* right.y + y * right.x,
			w * right.w + x * right.x + y * right.y + z * right.z);
	}

	public Quat power(double a) {
		double angle = Math.acos(w) * a;
		double axisMult = Math.sin(angle) / Math.sqrt(x * x + y * y + z * z);

		return new Quat(x * axisMult, y * axisMult, z * axisMult, Math.cos(angle));
	}

	/** The results are packed into a Vec4 with w = angle */

	public Vec4 toAxisAngle() {
		double angle = 2 * Math.acos(w);
		double dem = 1.0 - w * w;

		if(dem == 0.0)
			return new Vec4(1, 0, 0, 0);

		dem = 1.0 / dem;

		return new Vec4(x * dem, y * dem, z * dem, angle);
	}

	/**
	 * returns a quaternion equivalent to the rotation of the
	 * Axis-Angle argument.
	 *
	 * v must be normalized
	 */
	public static Quat fromAxisAngle(Vec3 v, double w) {
		double x = v.x;
		double y = v.y;
		double z = v.z;

		double s = Math.sin(0.5 * w);
		x *= s;
		y *= s;
		z *= s;
		w = Math.cos(0.5 * w);

		return new Quat(x, y, z, w);
	}

	/**
	 * returns a Quat representing the rotation represented by the matrix
	 */
	public static Quat fromMatrix(Mat4 m) {
		return fromMatrix(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
	}

	/**
	 * returns a Quat representing the rotation represented by the matrix
	 */
	public static Quat fromMatrix(Mat3 m) {
		return fromMatrix(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
	}

	/**
	 * returns a Quat representing the rotation represented by the matrix
	 */
	public static Quat fromMatrix(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {

		double s, x, y, z, w;
		double tr = m00 + m11 + m22;
		if (tr >= 0.0) {
			s = Math.sqrt(tr + 1.0);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else {
			double max = Math.max(Math.max(m00, m11), m22);
			if (max == m00) {
				s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m01 + m10) * s;
				z = (m20 + m02) * s;
				w = (m21 - m12) * s;
			} else if (max == m11) {
				s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m12 + m21) * s;
				x = (m01 + m10) * s;
				w = (m02 - m20) * s;
			} else {
				s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m20 + m02) * s;
				y = (m12 + m21) * s;
				w = (m10 - m01) * s;
			}
		}
		return new Quat(x, y, z, w);
	}

	public Quat normalize() {
		final double sqLength = x * x + y * y + z * z + w * w;
		final double invLength = 1.0 / sqrt(sqLength);

		return new Quat(x * invLength, y * invLength, z * invLength, w * invLength);
	}

	public Mat3 matrix() {
		double fTx  = 2.0 * x;
		double fTy  = 2.0 * y;
		double fTz  = 2.0 * z;
		double fTwx = fTx * w;
		double fTwy = fTy * w;
		double fTwz = fTz * w;
		double fTxx = fTx * x;
		double fTxy = fTy * x;
		double fTxz = fTz * x;
		double fTyy = fTy * y;
		double fTyz = fTz * y;
		double fTzz = fTz * z;

		return new Mat3(1.0f-(fTyy+fTzz), fTxy-fTwz, fTxz+fTwy,
			fTxy+fTwz, 1.0f-(fTxx+fTzz), fTyz-fTwx,
			fTxz-fTwy, fTyz+fTwx, 1.0f-(fTxx+fTyy));
	}

	public Quat scale(double d) {
		return new Quat();
	}

	public Quat add(Quat q) {
		return new Quat(x + q.x, y + q.y, z + q.z, w + q.w);
	}
}