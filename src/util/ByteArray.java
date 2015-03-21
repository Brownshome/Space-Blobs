package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.BufferUtils;

public class ByteArray {
	static final int GROW_AMOUNT = 17;
	static final ByteOrder ORDER = ByteOrder.nativeOrder();

	byte[] data = new byte[0];
	int index;

	public void put(int i) {
		if(index == data.length) {
			byte[] newData = new byte[data.length + GROW_AMOUNT];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}

		data[index] = (byte) i;
		index++;
	}

	public void set(int index, byte b) {
		data[index] = b;
	}

	public void clear() {
		index = 0;
		data = new byte[data.length];
	}

	public byte get(int i) {
		return data[i];
	}

	public byte[] get() {
		byte[] result = new byte[index];
		System.arraycopy(data, 0, result, 0, index);
		return data;
	}

	public void putInt(int x) {
		if(ORDER == ByteOrder.BIG_ENDIAN) {
			put((x & 0xFF000000) >> 24);
			put((x & 0xFF0000) >> 16);
			put((x & 0xFF00) >> 8);
			put((x & 0xFF) >> 0);
		} else {
			put((x & 0xFF) >> 0);
			put((x & 0xFF00) >> 8);
			put((x & 0xFF0000) >> 16);
			put((x & 0xFF000000) >> 24);
		}
	}

	public void putFloat(float z) {
		putInt(Float.floatToRawIntBits(z));
	}

	public int size() {
		return index;
	}

	public void putFloat(double z) {
		putFloat((float) z);
	}

	public ByteBuffer toByteBuffer() {
		ByteBuffer result = BufferUtils.createByteBuffer(size());
		result.put(data, 0, size());
		result.flip();
		return result;
	}
}
