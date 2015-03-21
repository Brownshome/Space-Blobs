package util;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;

public class IntArray {
	static final int GROW_AMOUNT = 17;

	public int[] data = new int[0];
	public int index;

	public void put(int i) {
		if(index == data.length) {
			int[] newData = new int[data.length + GROW_AMOUNT];
			System.arraycopy(data, 0, newData, 0, data.length);
			data = newData;
		}

		data[index] = i;
		index++;
	}

	public int size() {
		return index;
	}
	
	public void set(int index, byte b) {
		data[index] = b;
	}

	public void clear() {
		index = 0;
		data = new int[data.length];
	}

	public int get(int i) {
		return data[i];
	}

	public int[] get() {
		int[] result = new int[index];
		System.arraycopy(data, 0, result, 0, index);
		return result;
	}

	public IntBuffer toIntBuffer() {
		IntBuffer result = BufferUtils.createIntBuffer(index);
		result.put(data, 0, index);
		result.flip();
		return result;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(get());
	}
}
