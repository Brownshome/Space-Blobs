package block;

import io.user.Console;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL42;

import render.Renderer;
import render.Texture;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

/** Handles the visual representation of a blockgroup */

/* Blocks are rendererd instanced as a triangle strip with the
 * texture indexs packed into a 2D array texture, the following data
 * is required:
 * 
 * Uniform width
 * 
 * Texture
 * 
 * Uniform size
 * Uniform rotation
 * Uniform position
 */

public class BlockGroupRenderer {
	static final int TEXTURE_SIZE = 64;
	static final int MIPMAP_LEVELS = 32 - Integer.numberOfLeadingZeros(TEXTURE_SIZE);
	static final int BLOCK_DATA_SIZE = 7;

	static final float[] PER_VERTEX = new float[] {
		//pos
		0, 0,
		1, 1,
		1, 0,

		0, 0,
		0, 1,
		1, 1,
	};

	static int VBO;
	int VAO;
	static int PROGRAM;
	static int TEXTURE;
	static int HEAT;
	static ArrayList<String> TEXTURE_NAMES = new ArrayList<>();

	public final static int SELECTED_TEXTURE;
	public final static int BASIC_HULL;
	public final static int HEAT_GRATE_ON;
	public final static int HEAT_GRATE_OFF;
	public final static int HULL_CORNER;
	public final static int EMPTY_GRATE;
	public final static int BLUE_HULL;

	static boolean created = false;

	static {
		VBO = Renderer.getBufferID();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.createFloatBuffer(PER_VERTEX.length).put(PER_VERTEX).flip(), GL15.GL_STATIC_DRAW);

		PROGRAM = Renderer.createProgram(Renderer.createShader("block fragment", GL20.GL_FRAGMENT_SHADER), Renderer.createShader("block vertex", GL20.GL_VERTEX_SHADER));

		HEAT = Renderer.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_1D, HEAT);
		Texture texture = new Texture("heatcolour", Format.BGRA);
		GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL11.GL_RGBA, 256, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);

		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		//TODO add mod texture loading
		SELECTED_TEXTURE = allocateTexture("block.selected");
		BASIC_HULL = allocateTexture("block.hull.basic.square");
		HEAT_GRATE_ON = allocateTexture("block.heatgrateon");   
		HEAT_GRATE_OFF = allocateTexture("block.heatgrateoff");
		HULL_CORNER = allocateTexture("block.hull.basic.corner");
		EMPTY_GRATE = allocateTexture("block.grate");
		BLUE_HULL = allocateTexture("block.hull.blue.square");
		createTexture();
	}

	int GPUData;
	BlockGroup parent;

	public BlockGroupRenderer(BlockGroup parent) {
		this.parent = parent;
		parent.renderer = this;
	}

	public void setupRender() {
		VAO = Renderer.getVertexArrayID();
		GPUData = Renderer.getBufferID();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GPUData);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, parent.number * BLOCK_DATA_SIZE * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

		GL30.glBindVertexArray(VAO);

		GL20.glEnableVertexAttribArray(0);	//position

		GL20.glEnableVertexAttribArray(1);	//texture indexs
		GL20.glEnableVertexAttribArray(2);	//position in grid
		GL20.glEnableVertexAttribArray(3);  //texture lerp
		GL20.glEnableVertexAttribArray(4);  //heat level
		GL20.glEnableVertexAttribArray(5);  //texture rotation

		GL30.glVertexAttribIPointer(1, 2, GL11.GL_INT, 28, 8);
		GL33.glVertexAttribDivisor(1, 1);

		GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, 28, 16);
		GL33.glVertexAttribDivisor(3, 1);

		GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, 28, 20);
		GL33.glVertexAttribDivisor(4, 1);

		GL30.glVertexAttribIPointer(2, 2, GL11.GL_INT, 28, 0);
		GL33.glVertexAttribDivisor(2, 1);

		GL30.glVertexAttribIPointer(5, 1, GL11.GL_INT, 28, 24); //R: n * PI / 2 clockwise
		GL33.glVertexAttribDivisor(5, 1);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
		
		parent.number--; //for the selection box, a more elegant system is needed
	}

	public void render() {
		if(!created)
			Console.error("Texture array not created.", "RENDER");

		Renderer.checkGL();
		
		//if(parent.updateRender) {
		int[] array = parent.getRenderData();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GPUData);
		
		Renderer.checkGL();
		
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, parent.getUpdateOffset(), (IntBuffer) BufferUtils.createIntBuffer(array.length).put(array).flip());
		//}

		Renderer.checkGL();

		GL30.glBindVertexArray(VAO);
		GL20.glUseProgram(PROGRAM);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		GL20.glUniform1f(0, (float) parent.scale);
		GL20.glUniform2f(1, (float) parent.m_xf.q.c, (float) parent.m_xf.q.s);
		GL20.glUniform2f(2, (float) parent.getPosition().x, (float) parent.getPosition().y);
		//GL20.glUniform2f(3, (float) parent.getLocalCenter().x, (float) parent.getLocalCenter().y);

		GL20.glUniform2f(4, (float) Renderer.position.x, (float) Renderer.position.y);
		GL20.glUniform2f(5, (float) Renderer.zoom.x, (float) Renderer.zoom.y);

		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_1D, HEAT);
		GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, 6, parent.number);
		Renderer.checkGL();
	}

	public void resizeBuffer() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GPUData);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, parent.number * BLOCK_DATA_SIZE * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
	}
	
	/** ID clashes are not checked, be sure or pay the price */
	public static int allocateTexture(String name) {
		if(created) 
			Console.error("Texture array already created", "RENDER");

		TEXTURE_NAMES.add(name);
		return TEXTURE_NAMES.size() - 1;
	}

	public static void createTexture() {
		if(created)
			Console.error("Texture array already created", "RENDER");

		created = true;

		TEXTURE = Renderer.getTextureID();
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, TEXTURE);
		GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, MIPMAP_LEVELS, GL11.GL_RGBA16, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_NAMES.size());

		Texture texture;
		for(int i = 0; i < TEXTURE_NAMES.size(); i++) {
			texture = new Texture(TEXTURE_NAMES.get(i), Format.BGRA);

			if(texture.width != TEXTURE_SIZE || texture.height != TEXTURE_SIZE)
				Console.error("Texture " + TEXTURE_NAMES.get(i) + " is the wrong size.", "TEXTURE LOAD");

			GL12.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, TEXTURE_SIZE, TEXTURE_SIZE, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, texture.data);
		}

		GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);

		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	}

	public static int getTexture(String name) {
		return TEXTURE_NAMES.indexOf(name);
	}
}
