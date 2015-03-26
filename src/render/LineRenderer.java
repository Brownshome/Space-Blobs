package render;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import physics.common.Color;
import physics.common.Vec2;

/** Only used for debug purposes, not optimised at all */
public class LineRenderer {
	static final int VAO = Renderer.getVertexArrayID();
	static final int VBO = Renderer.getBufferID();
	static final int PROGRAM = Renderer.createProgram(Renderer.createShader("line vertex", GL20.GL_VERTEX_SHADER), Renderer.createShader("line fragment", GL20.GL_FRAGMENT_SHADER));
	
	static ArrayList<Vec2> points = new ArrayList<>();
	static ArrayList<Color> colours = new ArrayList<>();
	
	static {
		GL30.glBindVertexArray(VAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 20, 0);
		
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 20, 8);
	}
	
	public static void draw(Vec2 a, Vec2 b, Color c) {
		points.add(a.sub(Renderer.position).mul(Renderer.zoom));
		points.add(b.sub(Renderer.position).mul(Renderer.zoom));
		colours.add(c);
	}
	
	public static void render() {
		FloatBuffer b = BufferUtils.createFloatBuffer(colours.size() * 10);
		
		for(int i = 0; i < points.size() / 2; i++) {
			b.put((float) points.get(i * 2).x).put((float) points.get(i * 2).y)
			.put(colours.get(i).r).put(colours.get(i).g).put(colours.get(i).b)
			
			.put((float) points.get(i * 2 + 1).x).put((float) points.get(i * 2 + 1).y)
			.put(colours.get(i).r).put(colours.get(i).g).put(colours.get(i).b);
		}
		
		b.flip();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, b, GL15.GL_STATIC_DRAW);
		
		GL30.glBindVertexArray(VAO);
		GL20.glUseProgram(PROGRAM);
		
		GL11.glDrawArrays(GL11.GL_LINES, 0, points.size());
		
		points.clear();
		colours.clear();
	}
}