package math;

import static java.lang.Math.sqrt;
import physics.common.Vec2;

//represents a complex number
public class Doublet {
	public final static Doublet IDENTITY = new Doublet();

	public final double x, y;

	/**
	 * The doublet will be initialized to the identity.
	 */
	public Doublet() {
		x = 1;
		y = 0;
	}

	public Doublet(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Calculate the conjugate of this doublet.
	 */

	public Doublet conj() {
		return new Doublet(x, -y);
	}
	
	public Doublet lerp(Doublet vec, double amount) {
		final double diff = 1.0 - amount;
		return new Doublet(diff * x + amount * vec.x, diff * y + amount * vec.y);
	}
	
	public Doublet scale(double s) {
		return new Doublet(x * s, s * y);
	}

	/**
	 * Sets the value of this doublet to the doublet product of
	 * doublets left and right (this = left * right). Note that this is safe
	 * for aliasing (e.g. this can be left or right).
	 *
	 * @param left
	 *            the first doublet
	 * @param right
	 *            the second doublet
	 */
	public Doublet mul(Doublet right) {
		return new Doublet(
			x * right.x - y * right.y,
			x * right.y + y * right.x
		);
	}

	public Vec2 rotate(Vec2 v) {
		return new Vec2(x * v.x - y * v.y, x * v.y + y * v.x);
	}
	
	public Doublet normalize() {
		final double sqLength = x * x + y * y;
		final double invLength = 1.0 / sqrt(sqLength);

		return new Doublet(x * invLength, y * invLength);
	}

	public Doublet add(Doublet q) {
		return new Doublet(x + q.x, y + q.y);
	}

	public Doublet perp() {
		return new Doublet(y, -x);
	}

	public Vec2 unrotate(Vec2 v) {
		return new Vec2(x * v.x + y * v.y, x * v.y - y * v.x);
	}
}
