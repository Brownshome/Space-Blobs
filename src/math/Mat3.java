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

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import physics.common.Vec2;

/**
 * A 3x3 matrix.
 *
 * @author James Royalty
 */
public final class Mat3 extends AbstractMat {
	public static final Mat3 MAT3_IDENTITY = new Mat3(1.0f);
	public static final Mat3 MAT3_ZERO = new Mat3();

	/*
	 * ::------------------------------------------------------------------------
	 * -:: COLUMN MAJOR LAYOUT: The first index indicates the COLUMN NUMBER. The
	 * second is the ROW NUMBER.
	 *
	 * | A D G | | m00 m10 m20 | | B E H | = | m01 m11 m21 | | C F I | | m02 m12
	 * m22 |
	 */
	final double m00, m10, m20;
	final double m01, m11, m21;
	final double m02, m12, m22;

	/**
	 * Creates a matrix with all elements equal to ZERO.
	 */
	public Mat3() {
		m00 = m10 = m20 = 0f;
		m01 = m11 = m21 = 0f;
		m02 = m12 = m22 = 0f;
	}

	/**
	 * Creates a matrix with the given value along the diagonal.
	 *
	 * @param diagonalValue
	 */
	public Mat3(final double diagonalValue) {
		m00 = m11 = m22 = diagonalValue;
		m10 = m20 = 0f;
		m01 = m21 = 0f;
		m02 = m12 = 0f;
	}

	/**
	 * Creates a matrix using successive triples as <em>columns</em>. For
	 * example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(1f, 2f, 3f, // first column
	 * 		4f, 5f, 6f, // second
	 * 		7f, 8f, 9f // third
	 * );
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 4 7 |
	 * | 2 5 8 |
	 * | 3 6 9 |
	 * </pre>
	 *
	 * @param x00
	 *            first column, x
	 * @param x01
	 *            first column, y
	 * @param x02
	 *            first column, z
	 * @param x10
	 *            second column, x
	 * @param x11
	 *            second column, y
	 * @param x12
	 *            second column, z
	 * @param x20
	 *            third column, x
	 * @param x21
	 *            third column, y
	 * @param x22
	 *            third column, z
	 */
	public Mat3(final double x00, final double x01, final double x02,
		final double x10, final double x11, final double x12, final double x20,
		final double x21, final double x22) {
		// Col 1
		m00 = x00;
		m01 = x01;
		m02 = x02;

		// Col 2
		m10 = x10;
		m11 = x11;
		m12 = x12;

		// Col 3
		m20 = x20;
		m21 = x21;
		m22 = x22;
	}

	/**
	 * Creates a matrix using successive triples as <em>columns</em>. For
	 * example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(new double[] { 1f, 2f, 3f, // first column
	 * 		4f, 5f, 6f, // second
	 * 		7f, 8f, 9f // third
	 * 		});
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 4 7 |
	 * | 2 5 8 |
	 * | 3 6 9 |
	 * </pre>
	 *
	 * @param mat
	 *            array containing <em>at least</em> 9 elements. It's okay if
	 *            the given array is larger than 9 elements; those elements will
	 *            be ignored.
	 */
	public Mat3(final double[] mat) {
		assert mat.length >= 9 : "Invalid matrix array length";

		int i = 0;

		// Col 1
		m00 = mat[i++];
		m01 = mat[i++];
		m02 = mat[i++];

		// Col 2
		m10 = mat[i++];
		m11 = mat[i++];
		m12 = mat[i++];

		// Col 3
		m20 = mat[i++];
		m21 = mat[i++];
		m22 = mat[i++];
	}

	/**
	 * Creates a matrix using successive triples as <em>columns</em>. The
	 * semantics are the same as the double array constructor.
	 *
	 * @param buffer
	 */
	public Mat3(final FloatBuffer buffer) {
		assert buffer.capacity() >= 9 : "Invalid matrix buffer length";

		final int startPos = buffer.position();

		m00 = buffer.get();
		m01 = buffer.get();
		m02 = buffer.get();

		m10 = buffer.get();
		m11 = buffer.get();
		m12 = buffer.get();

		m20 = buffer.get();
		m21 = buffer.get();
		m22 = buffer.get();

		buffer.position(startPos);
	}

	/**
	 * Creates a matrix that is a copy of the given matrix.
	 *
	 * @param mat
	 *            matrix to copy
	 */
	public Mat3(final Mat3 mat) {
		m00 = mat.m00;
		m01 = mat.m01;
		m02 = mat.m02;

		m10 = mat.m10;
		m11 = mat.m11;
		m12 = mat.m12;

		m20 = mat.m20;
		m21 = mat.m21;
		m22 = mat.m22;
	}

	/**
	 * Create a matrix using the given vectors as <em>columns</em>. For example,
	 *
	 * <pre>
	 * Mat3 m1 = new Mat3(new Vec3(1f, 2f, 3f), // first column
	 * 		new Vec3(4f, 5f, 6f), // second
	 * 		new Vec3(7f, 8f, 9f) // third
	 * );
	 * </pre>
	 *
	 * will create the following 3x3 matrix:
	 *
	 * <pre>
	 * | 1 4 7 |
	 * | 2 5 8 |
	 * | 3 6 9 |
	 * </pre>
	 *
	 * @param col0
	 *            vector for the first column
	 * @param col1
	 *            vector for the second column
	 * @param col2
	 *            vector for the third column
	 */
	public Mat3(final Vec3 col0, final Vec3 col1, final Vec3 col2) {
		m00 = col0.x;
		m10 = col1.x;
		m20 = col2.x;
		m01 = col0.y;
		m11 = col1.y;
		m21 = col2.y;
		m02 = col0.z;
		m12 = col1.z;
		m22 = col2.z;
	}

	public double determinant() {
		return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20)
			+ m02 * (m10 * m21 - m11 * m20);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Mat3)) return false;
		Mat3 other = (Mat3) obj;

		return m00 == other.m00 && m10 == other.m10 && m20 == other.m20 &&
			m01 == other.m01 && m11 == other.m11 && m21 == other.m21 &&
			m02 == other.m02 && m12 == other.m12 && m22 == other.m22;
	}

	public FloatBuffer getBuffer() {
		final FloatBuffer buffer = allocateFloatBuffer();
		final int startPos = buffer.position();

		// Col 1
		buffer.put((float) m00).put((float) m01).put((float) m02);

		// Col 2
		buffer.put((float) m10).put((float) m11).put((float) m12);

		// Col 3
		buffer.put((float) m20).put((float) m21).put((float) m22);

		buffer.position(startPos);

		return buffer;
	}

	public Vec3 getColumn(final int columnIndex) {
		assert columnIndex < 3 : "Invalid column index = " + columnIndex;

		switch (columnIndex) {
			case 0:
				return new Vec3(m00, m01, m02);
			case 1:
				return new Vec3(m10, m11, m12);
			case 2:
				return new Vec3(m20, m21, m22);
			default:
				throw new IllegalArgumentException("Invalid column index = "
					+ columnIndex);
		}
	}

	public Iterable<Vec3> getColumns() {
		List<Vec3> cols = new ArrayList<Vec3>(3);

		cols.add(new Vec3(m00, m01, m02));
		cols.add(new Vec3(m10, m11, m12));
		cols.add(new Vec3(m20, m21, m22));

		return cols;
	}

	@Override
	public int getNumColumns() {
		return 3;
	}

	@Override
	public int getNumRows() {
		return 3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits((float) m00);
		result = prime * result + Float.floatToIntBits((float) m01);
		result = prime * result + Float.floatToIntBits((float) m02);
		result = prime * result + Float.floatToIntBits((float) m10);
		result = prime * result + Float.floatToIntBits((float) m11);
		result = prime * result + Float.floatToIntBits((float) m12);
		result = prime * result + Float.floatToIntBits((float) m20);
		result = prime * result + Float.floatToIntBits((float) m21);
		result = prime * result + Float.floatToIntBits((float) m22);
		return result;
	}

	public Mat3 multiply(final double a) {
		return new Mat3(m00 * a, m01 * a, m02 * a, m10 * a, m11 * a, m12 * a,
			m20 * a, m21 * a, m22 * a);
	}

	public Mat3 multiply(final Mat3 mat) {
		return new Mat3(m00 * mat.m00 + m10 * mat.m01 + m20 * mat.m02, // m00
			m01 * mat.m00 + m11 * mat.m01 + m21 * mat.m02, // m01
			m02 * mat.m00 + m12 * mat.m01 + m22 * mat.m02, // m02

			m00 * mat.m10 + m10 * mat.m11 + m20 * mat.m12, // m10
			m01 * mat.m10 + m11 * mat.m11 + m21 * mat.m12, // m11
			m02 * mat.m10 + m12 * mat.m11 + m22 * mat.m12, // m12

			m00 * mat.m20 + m10 * mat.m21 + m20 * mat.m22, // m20
			m01 * mat.m20 + m11 * mat.m21 + m21 * mat.m22, // m21
			m02 * mat.m20 + m12 * mat.m21 + m22 * mat.m22 // m22
			);
	}

	/**
	 * This is the equivalent of <strong>this * vector</strong> (if we had
	 * operator overloading). If you want <strong>vector * this</strong> then
	 * see {@link Vec3#multiply(Mat3)}.
	 *
	 * @param vec
	 * @return
	 */
	public Vec3 multiply(final Vec3 vec) {
		return new Vec3(m00 * vec.x + m10 * vec.y + m20 * vec.z, m01 * vec.x
			+ m11 * vec.y + m21 * vec.z, m02 * vec.x + m12 * vec.y + m22
			* vec.z);
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getClass().getSimpleName())
			.append("{").append("\n ")
			.append(String.format("%8.5f %8.5f %8.5f", m00, m10, m20))
			.append("\n ")
			.append(String.format("%8.5f %8.5f %8.5f", m01, m11, m21))
			.append("\n ")
			.append(String.format("%8.5f %8.5f %8.5f", m02, m12, m22))
			.append("\n}").toString();
	}

	public Mat3 transpose() {
		return new Mat3(m00, m10, m20, m01, m11, m21, m02, m12, m22);
	}

	public Vec3 axisAngle() {
		double angle = Math.acos((m00 + m11 + m22 - 1) / 2);

		if(angle < 0.0001)
			return Vec3.VEC3_ZERO;

		double a = m21 - m12;
		double b = m02 - m20;
		double c = m10 - m01;
		double d = 1 / Math.sqrt(a * a + b * b + c * c);

		return new Vec3(a * d * angle, b * d * angle, c * d * angle);
	}

	public Mat3 add(Mat3 m) {
		return new Mat3(m00 + m.m00, m01 + m.m01, m02 + m.m02,
						m10 + m.m10, m11 + m.m11, m12 + m.m12,
						m20 + m.m20, m21 + m.m21, m22 + m.m22);
	}

	/** Converts the Vec2 to a Vec3 then multiplies it and then back again */
	public Vec2 multiply(Vec2 vec) {
		return new Vec2(m00 * vec.x + m10 * vec.y + m20, m01 * vec.x + m11 * vec.y + m21);
	}

	public static Mat3 translate(Vec2 v) {
		return new Mat3(1, 0, 0,
			0, 1, 0,
			v.x, v.y, 1);
	}
}
