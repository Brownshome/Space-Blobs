package math;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import physics.common.Vec2;

/**
 * A 2x2 matrix.
 *
 * @author J. Brown (copied from Mat3)
 */
public final class Mat2 extends AbstractMat {
	public static final Mat2 MAT2_IDENTITY = new Mat2(1);
	public static final Mat2 MAT2_ZERO = new Mat2();

	/*
	 * ::------------------------------------------------------------------------
	 * -:: COLUMN MAJOR LAYOUT: The first index indicates the COLUMN NUMBER. The
	 * second is the ROW NUMBER.
	 *
	 * | m00 m10 |
	 * | m01 m11 |
	 */

	public final double m00, m10;
	public final double m01, m11;

	/**
	 * Creates a matrix with all elements equal to ZERO.
	 */
	public Mat2() {
		m00 = m10 = 0f;
		m01 = m11 = 0f;
	}

	/**
	 * Creates a matrix with the given value along the diagonal.
	 *
	 * @param diagonalValue
	 */
	public Mat2(final double diagonalValue) {
		m00 = m11 = diagonalValue;
		m10 = 0f;
		m01 = 0f;
	}

	/**
	 * Creates a matrix using successive doubles as <em>columns</em>. For
	 * example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(1f, 2f, 3f, // first column
	 * 					  4f, 5f, 6f, // second
	 * 					  7f, 8f, 9f // third
	 * );
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 3 |
	 * | 2 4 |
	 * </pre>
	 *
	 * @param x00
	 *            first column, x
	 * @param x01
	 *            first column, y
	 * @param x10
	 *            second column, x
	 * @param x11
	 *            second column, y
	 */
	public Mat2(final double x00, final double x01, final double x10, final double x11) {
		// Col 1
		m00 = x00;
		m01 = x01;

		// Col 2
		m10 = x10;
		m11 = x11;
	}

	/**
	 * Creates a matrix using successive triples as <em>columns</em>. For
	 * example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(new float[] { 1f, 2f, // first column
	 * 	3f, 4f // second
	 * 		});
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 3 |
	 * | 2 4 |
	 * </pre>
	 *
	 * @param mat
	 *            array containing <em>at least</em> 4 elements. It's okay if
	 *            the given array is larger than 4 elements; those elements will
	 *            be ignored.
	 */
	public Mat2(final double[] mat) {
		assert mat.length >= 4 : "Invalid matrix array length";

		int i = 0;

		// Col 1
		m00 = mat[i++];
		m01 = mat[i++];

		// Col 2
		m10 = mat[i++];
		m11 = mat[i++];
	}

	/**
	 * Creates a matrix using successive doubles as <em>columns</em>. The
	 * semantics are the same as the float array constructor.
	 *
	 * @param buffer
	 */
	public Mat2(final FloatBuffer buffer) {
		assert buffer.capacity() >= 4 : "Invalid matrix buffer length";

		final int startPos = buffer.position();

		m00 = buffer.get();
		m01 = buffer.get();

		m10 = buffer.get();
		m11 = buffer.get();

		buffer.position(startPos);
	}

	/**
	 * Creates a matrix that is a copy of the given matrix.
	 *
	 * @param mat
	 *            matrix to copy
	 */
	public Mat2(final Mat2 mat) {
		m00 = mat.m00;
		m01 = mat.m01;

		m10 = mat.m10;
		m11 = mat.m11;
	}

	/**
	 * Create a matrix using the given vectors as <em>columns</em>. For example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(new Vec2(1f, 2f), // first column
	 * 		new Vec2(3f, 4f), // second
	 * );
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 3 |
	 * | 2 4 |
	 * </pre>
	 *
	 * @param col0
	 *            vector for the first column
	 * @param col1
	 *            vector for the second column
	 */
	public Mat2(final Vec2 col0, final Vec2 col1) {
		m00 = col0.x;
		m10 = col1.x;
		m01 = col0.y;
		m11 = col1.y;
	}

	public double determinant() {
		return m00 * m11 - m01 * m10;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Mat2)) return false;
		Mat2 other = (Mat2) obj;
		return m00 == other.m00 && m10 == other.m10 && m01 == other.m01 && m11 == other.m11;
	}

	public FloatBuffer getBuffer() {
		final FloatBuffer buffer = allocateFloatBuffer();
		final int startPos = buffer.position();

		// Col 1
		buffer.put((float) m00).put((float) m01);

		// Col 2
		buffer.put((float) m10).put((float) m11);

		buffer.position(startPos);

		return buffer;
	}

	public Vec2 getColumn(final int columnIndex) {

		switch (columnIndex) {
			case 0:
				return new Vec2(m00, m01);
			case 1:
				return new Vec2(m10, m11);
			default:
				throw new IllegalArgumentException("Invalid column index = "
					+ columnIndex);
		}
	}

	public Iterable<Vec2> getColumns() {
		List<Vec2> cols = new ArrayList<Vec2>(2);

		cols.add(new Vec2(m00, m01));
		cols.add(new Vec2(m10, m11));

		return cols;
	}

	@Override
	public int getNumColumns() {
		return 2;
	}

	@Override
	public int getNumRows() {
		return 2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits((float) m00);
		result = prime * result + Float.floatToIntBits((float) m01);
		result = prime * result + Float.floatToIntBits((float) m10);
		result = prime * result + Float.floatToIntBits((float) m11);
		return result;
	}

	public Mat2 multiply(final double a) {
		return new Mat2(m00 * a, m01 * a,
			m10 * a, m11 * a);
	}

	/**
	 *
	 * @param mat the matrice to multiply
	 * @return this * mat
	 */
	public Mat2 multiply(final Mat2 mat) {
		return new Mat2(
			m00 * mat.m00 + m10 * mat.m01, // m00
			m01 * mat.m00 + m11 * mat.m01, // m01
			m00 * mat.m10 + m10 * mat.m11, // m10
			m01 * mat.m10 + m11 * mat.m11 // m11
			);
	}

	/**
	 * This is the equivalent of <strong>this * vector</strong> (if we had
	 * operator overloading). If you want <strong>vector * this</strong> then
	 * see {@link Vec2#multiply(Mat2)}.
	 *
	 * @param vec
	 * @return
	 */
	public Vec2 multiply(final Vec2 vec) {
		return new Vec2(m00 * vec.x + m10 * vec.y, m01 * vec.x + m11 * vec.y);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName())
			.append("{").append("\n ")
			.append(String.format("%8.5f %8.5f %8.5f", m00, m10))
			.append("\n ")
			.append(String.format("%8.5f %8.5f %8.5f", m01, m11))
			.append("\n}").toString();
	}

	public Mat2 transpose() {
		return new Mat2(m00, m10, m01, m11);
	}
}
