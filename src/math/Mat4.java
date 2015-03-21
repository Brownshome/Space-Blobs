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

/**
 * A 4x4 matrix.
 *
 * @author James Royalty
 */
public final class Mat4 extends AbstractMat {
	public static final Mat4 MAT4_IDENTITY = new Mat4(1);
	public static final Mat4 MAT4_ZERO = new Mat4();

	/*
	 * ::------------------------------------------------------------------------
	 * -:: COLUMN MAJOR LAYOUT: The first index indicates the COLUMN NUMBER. The
	 * second is the ROW NUMBER.
	 *
	 * | A E I M | | m00 m10 m20 m30 | | B F J N | = | m01 m11 m21 m31 | | C G K
	 * O | | m02 m12 m22 m32 | | D H L P | | m03 m13 m23 m33 |
	 */
	public final double m00, m10, m20, m30;
	public final double m01, m11, m21, m31;
	public final double m02, m12, m22, m32;
	public final double m03, m13, m23, m33;

	/**
	 * Creates a matrix with all elements equal to ZERO.
	 */
	public Mat4() {
		m00 = m10 = m20 = m30 = 0f;
		m01 = m11 = m21 = m31 = 0f;
		m02 = m12 = m22 = m32 = 0f;
		m03 = m13 = m23 = m33 = 0f;
	}

	/**
	 * Creates a matrix with the given value along the diagonal.
	 *
	 * @param d
	 */
	public Mat4(final double d) {
		m00 = m11 = m22 = m33 = d;
		m01 = m02 = m03 = 0f;
		m10 = m12 = m13 = 0f;
		m20 = m21 = m23 = 0f;
		m30 = m31 = m32 = 0f;
	}

	/**
	 * Creates a matrix using successive 4-tuples as <em>columns</em>.
	 *
	 * @param x00
	 *            first column, x
	 * @param x01
	 *            first column, y
	 * @param x02
	 *            first column, z
	 * @param x03
	 *            first column, w
	 * @param x10
	 *            second column, x
	 * @param x11
	 *            second column, y
	 * @param x12
	 *            second column, z
	 * @param x13
	 *            second column, w
	 * @param x20
	 *            third column, x
	 * @param x21
	 *            third column, y
	 * @param x22
	 *            third column, z
	 * @param x23
	 *            third column, w
	 * @param x30
	 *            fourth column, x
	 * @param x31
	 *            fourth column, y
	 * @param x32
	 *            fourth column, z
	 * @param x33
	 *            fourth column, w
	 */
	public Mat4(final double x00, final double x01, final double x02,
		final double x03, final double x10, final double x11, final double x12,
		final double x13, final double x20, final double x21, final double x22,
		final double x23, final double x30, final double x31, final double x32,
		final double x33) {
		// Col 1
		m00 = x00;
		m01 = x01;
		m02 = x02;
		m03 = x03;

		// Col 2
		m10 = x10;
		m11 = x11;
		m12 = x12;
		m13 = x13;

		// Col 3
		m20 = x20;
		m21 = x21;
		m22 = x22;
		m23 = x23;

		// Col 4
		m30 = x30;
		m31 = x31;
		m32 = x32;
		m33 = x33;
	}

	/**
	 * Creates a matrix using successive 4-tuples as <em>columns</em>.
	 *
	 * @param mat
	 *            array containing <em>at least</em> 16 elements. It's okay if
	 *            the given array is larger than 16 elements; those elements
	 *            will be ignored.
	 */
	public Mat4(final double[] mat) {
		assert mat.length >= 16 : "Invalid matrix array length";

		int i = 0;

		// Col 1
		m00 = mat[i++];
		m01 = mat[i++];
		m02 = mat[i++];
		m03 = mat[i++];

		// Col 2
		m10 = mat[i++];
		m11 = mat[i++];
		m12 = mat[i++];
		m13 = mat[i++];

		// Col 3
		m20 = mat[i++];
		m21 = mat[i++];
		m22 = mat[i++];
		m23 = mat[i++];

		// Col 4
		m30 = mat[i++];
		m31 = mat[i++];
		m32 = mat[i++];
		m33 = mat[i++];
	}

	/**
	 * Creates a matrix using successive 4-tuples as <em>columns</em>. The
	 * semantics are the same as the float array constructor.
	 *
	 * @param buffer
	 */
	public Mat4(final FloatBuffer buffer) {
		assert buffer.capacity() >= 16 : "Invalid matrix buffer length";

		final int startPos = buffer.position();

		// Col 1
		m00 = buffer.get();
		m01 = buffer.get();
		m02 = buffer.get();
		m03 = buffer.get();

		// Col 2
		m10 = buffer.get();
		m11 = buffer.get();
		m12 = buffer.get();
		m13 = buffer.get();

		// Col 3
		m20 = buffer.get();
		m21 = buffer.get();
		m22 = buffer.get();
		m23 = buffer.get();

		// Col 4
		m30 = buffer.get();
		m31 = buffer.get();
		m32 = buffer.get();
		m33 = buffer.get();

		buffer.position(startPos);
	}

	/**
	 * Creates a matrix that is a copy of the given matrix.
	 *
	 * @param mat
	 *            matrix to copy
	 */
	public Mat4(final Mat4 mat) {
		m00 = mat.m00;
		m01 = mat.m01;
		m02 = mat.m02;
		m03 = mat.m03;

		m10 = mat.m10;
		m11 = mat.m11;
		m12 = mat.m12;
		m13 = mat.m13;

		m20 = mat.m20;
		m21 = mat.m21;
		m22 = mat.m22;
		m23 = mat.m23;

		m30 = mat.m30;
		m31 = mat.m31;
		m32 = mat.m32;
		m33 = mat.m33;
	}

	/**
	 * Create a matrix using the given 3-elements vectors as <em>columns</em>.
	 * The fourth element of each given vector will be set to zero.
	 *
	 * @param col0
	 *            vector for the first column
	 * @param col1
	 *            vector for the second column
	 * @param col2
	 *            vector for the third column
	 * @param col3
	 *            vector for the fourth column
	 */
	public Mat4(final Vec3 col0, final Vec3 col1, final Vec3 col2,
		final Vec3 col3) {
		m00 = col0.x;
		m10 = col1.x;
		m20 = col2.x;
		m30 = col3.x;
		m01 = col0.y;
		m11 = col1.y;
		m21 = col2.y;
		m31 = col3.y;
		m02 = col0.z;
		m12 = col1.z;
		m22 = col2.z;
		m32 = col3.z;
		m03 = 0f;
		m13 = 0f;
		m23 = 0f;
		m33 = 0f;
	}

	/**
	 * Create a matrix using the given 4-elements vectors as <em>columns</em>.
	 *
	 * @param col0
	 *            vector for the first column
	 * @param col1
	 *            vector for the second column
	 * @param col2
	 *            vector for the third column
	 * @param col3
	 *            vector for the fourth column
	 */
	public Mat4(final Vec4 col0, final Vec4 col1, final Vec4 col2,
		final Vec4 col3) {
		m00 = col0.x;
		m10 = col1.x;
		m20 = col2.x;
		m30 = col3.x;
		m01 = col0.y;
		m11 = col1.y;
		m21 = col2.y;
		m31 = col3.y;
		m02 = col0.z;
		m12 = col1.z;
		m22 = col2.z;
		m32 = col3.z;
		m03 = col0.w;
		m13 = col1.w;
		m23 = col2.w;
		m33 = col3.w;
	}

	/**
	 * Add two matrices together and return the result
	 *
	 * @param other
	 */
	public Mat4 add(final Mat4 other) {
		return new Mat4(m00 + other.m00, m01 + other.m01, m02 + other.m02, m03
			+ other.m03, m10 + other.m10, m11 + other.m11, m12 + other.m12,
			m13 + other.m13, m20 + other.m20, m21 + other.m21, m22
			+ other.m22, m23 + other.m23, m30 + other.m30, m31
			+ other.m31, m32 + other.m32, m33 + other.m33);
	}

	/**Wraps a 3x3 matrix in a 4x4 for multiplication
	 *
	 * @param other The 3x3 matrix
	 */
	public Mat4(final Mat3 m) {
		m00 = m.m00;
		m01 = m.m01;
		m02 = m.m02;

		m10 = m.m10;
		m11 = m.m11;
		m12 = m.m12;

		m20 = m.m20;
		m21 = m.m21;
		m22 = m.m22;

		m33 = 1.0f;
		m31 = m30 = m03 = m13 = m23 = m32 = 0.0f;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Mat4)) return false;
		Mat4 other = (Mat4) obj;
		return m00 == other.m00 && m01 == other.m01 && m02 == other.m02 && m03 == other.m03 &&
			m10 == other.m10 && m11 == other.m11 && m12 == other.m12 && m13 == other.m13 &&
			m20 == other.m20 && m21 == other.m21 && m22 == other.m22 && m23 == other.m23 &&
			m30 == other.m30 && m31 == other.m31 && m32 == other.m32 && m33 == other.m33;
	}

	public FloatBuffer getBuffer() {
		final FloatBuffer buffer = allocateFloatBuffer();
		final int startPos = buffer.position();

		// Col1
		buffer.put((float) m00).put((float) m01).put((float) m02).put((float) m03);

		// Col 2
		buffer.put((float) m10).put((float) m11).put((float) m12).put((float) m13);

		// Col 3
		buffer.put((float) m20).put((float) m21).put((float) m22).put((float) m23);

		// Col 4
		buffer.put((float) m30).put((float) m31).put((float) m32).put((float) m33);

		buffer.position(startPos);

		return buffer;
	}

	public Vec4 getColumn(final int columnIndex) {
		assert columnIndex < 4 : "Invalid column index = " + columnIndex;

		switch (columnIndex) {
			case 0:
				return new Vec4(m00, m01, m02, m03);
			case 1:
				return new Vec4(m10, m11, m12, m13);
			case 2:
				return new Vec4(m20, m21, m22, m23);
			case 3:
				return new Vec4(m30, m31, m32, m33);
			default:
				throw new IllegalArgumentException("Invalid column index = "
					+ columnIndex);
		}
	}

	public Iterable<Vec4> getColumns() {
		List<Vec4> cols = new ArrayList<Vec4>(4);

		cols.add(new Vec4(m00, m01, m02, m03));
		cols.add(new Vec4(m10, m11, m12, m13));
		cols.add(new Vec4(m20, m21, m22, m23));
		cols.add(new Vec4(m30, m31, m32, m33));

		return cols;
	}

	@Override
	public int getNumColumns() {
		return 4;
	}

	@Override
	public int getNumRows() {
		return 4;
	}

	public Mat3 getRotation() {
		return new Mat3(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits((float) m00);
		result = prime * result + Float.floatToIntBits((float) m01);
		result = prime * result + Float.floatToIntBits((float) m02);
		result = prime * result + Float.floatToIntBits((float) m03);
		result = prime * result + Float.floatToIntBits((float) m10);
		result = prime * result + Float.floatToIntBits((float) m11);
		result = prime * result + Float.floatToIntBits((float) m12);
		result = prime * result + Float.floatToIntBits((float) m13);
		result = prime * result + Float.floatToIntBits((float) m20);
		result = prime * result + Float.floatToIntBits((float) m21);
		result = prime * result + Float.floatToIntBits((float) m22);
		result = prime * result + Float.floatToIntBits((float) m23);
		result = prime * result + Float.floatToIntBits((float) m30);
		result = prime * result + Float.floatToIntBits((float) m31);
		result = prime * result + Float.floatToIntBits((float) m32);
		result = prime * result + Float.floatToIntBits((float) m33);
		return result;
	}

	/**
	 * Multiply this matrix with another and return the result.
	 *
	 * @param right
	 */
	public Mat4 multiply(final Mat4 right) {
		double nm00 = m00 * right.m00 + m10 * right.m01 + m20 * right.m02 + m30
			* right.m03;
		double nm01 = m01 * right.m00 + m11 * right.m01 + m21 * right.m02 + m31
			* right.m03;
		double nm02 = m02 * right.m00 + m12 * right.m01 + m22 * right.m02 + m32
			* right.m03;
		double nm03 = m03 * right.m00 + m13 * right.m01 + m23 * right.m02 + m33
			* right.m03;
		double nm10 = m00 * right.m10 + m10 * right.m11 + m20 * right.m12 + m30
			* right.m13;
		double nm11 = m01 * right.m10 + m11 * right.m11 + m21 * right.m12 + m31
			* right.m13;
		double nm12 = m02 * right.m10 + m12 * right.m11 + m22 * right.m12 + m32
			* right.m13;
		double nm13 = m03 * right.m10 + m13 * right.m11 + m23 * right.m12 + m33
			* right.m13;
		double nm20 = m00 * right.m20 + m10 * right.m21 + m20 * right.m22 + m30
			* right.m23;
		double nm21 = m01 * right.m20 + m11 * right.m21 + m21 * right.m22 + m31
			* right.m23;
		double nm22 = m02 * right.m20 + m12 * right.m21 + m22 * right.m22 + m32
			* right.m23;
		double nm23 = m03 * right.m20 + m13 * right.m21 + m23 * right.m22 + m33
			* right.m23;
		double nm30 = m00 * right.m30 + m10 * right.m31 + m20 * right.m32 + m30
			* right.m33;
		double nm31 = m01 * right.m30 + m11 * right.m31 + m21 * right.m32 + m31
			* right.m33;
		double nm32 = m02 * right.m30 + m12 * right.m31 + m22 * right.m32 + m32
			* right.m33;
		double nm33 = m03 * right.m30 + m13 * right.m31 + m23 * right.m32 + m33
			* right.m33;

		return new Mat4(nm00, nm01, nm02, nm03, nm10, nm11, nm12, nm13, nm20,
			nm21, nm22, nm23, nm30, nm31, nm32, nm33);
	}

	/**
	 * This is the equivalent of <strong>this * vector</strong> (if we had
	 * operator overloading). If you want <strong>vector * this</strong> then
	 * see {@link Vec4#multiply(Mat4)}.
	 *
	 * @param vec
	 * @return
	 */
	public Vec4 multiply(final Vec4 vec) {
		return new Vec4(m00 * vec.x + m10 * vec.y + m20 * vec.z + m30 * vec.w,
			m01 * vec.x + m11 * vec.y + m21 * vec.z + m31 * vec.w, m02
			* vec.x + m12 * vec.y + m22 * vec.z + m32 * vec.w, m03
			* vec.x + m13 * vec.y + m23 * vec.z + m33 * vec.w);
	}

	/**
	 * Subtract other matrix from this one and return the result ( this - right
	 * )
	 *
	 * @param right
	 */
	public Mat4 subtract(final Mat4 right) {
		return new Mat4(m00 - right.m00, m01 - right.m01, m02 - right.m02, m03
			- right.m03, m10 - right.m10, m11 - right.m11, m12 - right.m12,
			m13 - right.m13, m20 - right.m20, m21 - right.m21, m22
			- right.m22, m23 - right.m23, m30 - right.m30, m31
			- right.m31, m32 - right.m32, m33 - right.m33);
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName())
		.append("{")
		.append("\n ")
		.append(String.format("%8.5f %8.5f %8.5f %8.5f", m00, m10, m20, m30))
		.append("\n ")
		.append(String.format("%8.5f %8.5f %8.5f %8.5f", m01, m11, m21, m31))
		.append("\n ")
		.append(String.format("%8.5f %8.5f %8.5f %8.5f", m02, m12, m22, m32))
		.append("\n ")
		.append(String.format("%8.5f %8.5f %8.5f %8.5f", m03, m13, m23, m33))
		.append("\n}")
		.toString();
	}

	public Mat4 transpose() {
		return new Mat4(m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33);
	}

	public static Mat4 getScalingMatrix(Vec3 scale) {
		return new Mat4(scale.x, 0, 0, 0, 0, scale.y, 0, 0, 0, 0, scale.z, 0, 0, 0, 0, 1);
	}

	public static Mat4 translate(Vec3 t) {
		return new Mat4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, t.x, t.y, t.z, 1);
	}

	public static Mat4 getModelMatrix(Mat3 r, Vec3 t) {
		return new Mat4(r.m00, r.m01, r.m02, 0, r.m10, r.m11, r.m12, 0, r.m20, r.m21, r.m22, 0, t.x, t.y, t.z, 1);
	}

	public Vec3 translationVec() {
		return new Vec3(m30, m31, m32);
	}

	/** equivilant to new Vec3(multiply(vec.toPoint())) */
	public Vec3 multiply(Vec3 vec) {
		return new Vec3(m00 * vec.x + m10 * vec.y + m20 * vec.z + m30,
			m01 * vec.x + m11 * vec.y + m21 * vec.z + m31, m02
			* vec.x + m12 * vec.y + m22 * vec.z + m32);
	}

	/** equivilant to new Vec3(multiply(vec.toDirection())) */
	public Vec3 multiplyAsDirection(Vec3 vec) {
		return new Vec3(m00 * vec.x + m10 * vec.y + m20 * vec.z,
			m01 * vec.x + m11 * vec.y + m21 * vec.z,
			m02 * vec.x + m12 * vec.y + m22 * vec.z);
	}
}
