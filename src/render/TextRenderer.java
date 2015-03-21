package render;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import physics.common.Vec2;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextRenderer {
	static final int GRID_SIZE = 16;

	static final int BYTES_PER_LETTER = 16;
	static final int OFFSET = 32;

	static Vec2 normScalar;

	static int VAO;
	static int VBO;
	static int PROGRAM;
	static int TEXTURE;

	static ArrayList<int[]> buffer = new ArrayList<>();
	static ArrayList<int[]> texts = new ArrayList<>();
	static int length = 0;

	public static int text(String text, boolean constant, Vec2 position, double size) {
		if(constant)
			texts.add(TextRenderer.translate(text, position, new Vec2(size)));
		else
			addText(translate(text, position, new Vec2(size)));

		return texts.size() - 1;
	}


	public static void clearStaticTextList() {
		texts.clear();
	}
	
	public static void initialize(String fontName) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		normScalar = new Vec2(1.0 / Display.getWidth(), 1.0 / Display.getHeight());

		//create the buffers
		VAO = Renderer.getVertexArrayID();
		GL30.glBindVertexArray(VAO);

		VBO = Renderer.getBufferID();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, BYTES_PER_LETTER, 0); 		//position
		GL30.glVertexAttribIPointer(1, 1, GL11.GL_UNSIGNED_INT, BYTES_PER_LETTER, 8);	//character index
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, BYTES_PER_LETTER, 12); 	//size

		//create the program
		int vertex = Renderer.createShader("text vertex", GL20.GL_VERTEX_SHADER);
		int fragment = Renderer.createShader("text fragment", GL20.GL_FRAGMENT_SHADER);
		int geometry = Renderer.createShader("text geometry", GL32.GL_GEOMETRY_SHADER);

		PROGRAM = Renderer.createProgram(vertex, fragment, geometry);

		//load the texture file
		TEXTURE = Renderer.getTextureID();
		Texture fontSheet = new Texture("fonts/" + fontName, Format.RGBA);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE);
		//TODO explore using GL42.glTexStorage2D
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, fontSheet.width, fontSheet.width, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fontSheet.data);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		GL20.glUseProgram(PROGRAM);
		GL20.glUniform1i(0, GRID_SIZE);
	}

	/**
	 * returns an array holding the data, this can be added at later frames for minimal cost */
	public static int[] translate(String text, Vec2 position, Vec2 size) {
		int[] data = new int[4 * text.length()];

		float x = (float) position.x;
		float y = (float) position.y;

		for(int i = 0; i < text.length(); i++) {
			data[i * 4 + 0] = Float.floatToRawIntBits(x);
			data[i * 4 + 1] = Float.floatToRawIntBits(y);
			data[i * 4 + 2] = text.codePointAt(i) - OFFSET;
			data[i * 4 + 3] = Float.floatToRawIntBits((float) size.x);
			x += size.x;
		}
		
		return data;
	}

	public static void addText(int[] data) {
		buffer.add(data);
		length += data.length;
	}

	public static void render() {
		for(int[] text : texts)
			length += text.length;
		
		buffer.addAll(texts);
		
		if(buffer.size() == 0)
			return;

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, length * Integer.BYTES, GL15.GL_STREAM_DRAW);
		IntBuffer b = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, length * Integer.BYTES, GL30.GL_MAP_INVALIDATE_BUFFER_BIT | GL30.GL_MAP_WRITE_BIT, null).asIntBuffer();
		for(int[] data : buffer)
			b.put(data);
		GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);

		GL20.glUseProgram(PROGRAM);
		GL30.glBindVertexArray(VAO);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, length);

		length = 0;
		buffer.clear();
	}
}
