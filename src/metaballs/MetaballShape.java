package metaballs;

import io.user.Console;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import math.Box;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;

import render.Renderer;

public class MetaballShape {
	static final int RES_X = 512;
	static final int RES_Y = 512;

	static final double GRID_SIZE = 0.4;
	static final int DATA = 3; //be sure to update this in glsl

	//metaballs are rendererd by rendering instanced to a texture and then filtering the texture so
	//that is forms a well defined edge.

	static int GPUPass1; //program
	static int GPUPass2; //program

	int GPUPass1OutFramebuffer;
	int GPUPass1OutTexture;
	int GPUVAO; //VAO
	int GPUVBO;

	static {
		GPUPass1 = Renderer.createProgram(Renderer.createShader("meta pass1 frag", GL20.GL_FRAGMENT_SHADER), Renderer.createShader("meta pass1 vert", GL20.GL_VERTEX_SHADER));
		GPUPass2 = Renderer.createProgram(Renderer.createShader("meta pass2 frag", GL20.GL_FRAGMENT_SHADER), Renderer.createShader("meta pass2 vert", GL20.GL_VERTEX_SHADER));
	}

	public void setupRender(int number) {
		GPUVAO = Renderer.getVertexArrayID();
		GPUVBO = Renderer.getBufferID();

		GL30.glBindVertexArray(GPUVAO);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GPUVBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, number * (DATA * 4 + 16) + 48, GL15.GL_STATIC_DRAW);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);

		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);	//xy per vertex

		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 28, 48);	//x0, x1, y0, y1
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 28, 64);	//0.5 of meta data

		GL33.glVertexAttribDivisor(1, 1);
		GL33.glVertexAttribDivisor(2, 1);

		FloatBuffer buffer = BufferUtils.createFloatBuffer(12);

		float[] perVertex = new float[] {
			0, 0,
			1, 1,
			1, 0,

			0, 0,
			0, 1,
			1, 1,
		};

		buffer.put(perVertex);
		buffer.flip();
		
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
		
		// CREATE FRAMEBUFFER

		GPUPass1OutFramebuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, GPUPass1OutFramebuffer);
		GPUPass1OutTexture = Renderer.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GPUPass1OutTexture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, RES_X, RES_Y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GPUPass1OutTexture, 0);
		GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);

		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			Console.error("framebuffer failed to form.", "RENDER");

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void render(Metaball... metaballs) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(metaballs.length * (DATA + 4));
		
		for(Metaball m : metaballs) {
			Box b = m.boundingBox();
			buffer.put((float) b.x0).put((float) b.x1).put((float) b.y0).put((float) b.y1);
			buffer.put(m.getData());
		}
		
		buffer.flip();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GPUVBO);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 48, buffer);

		//1st pass
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, GPUPass1OutFramebuffer);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		GL11.glViewport(0, 0, RES_X, RES_Y);

		GL30.glBindVertexArray(GPUVAO);
		GL20.glUseProgram(GPUPass1);

		GL20.glUniform2f(0, (float) Renderer.position.x, (float) Renderer.position.y);
		GL20.glUniform2f(1, (float) Renderer.zoom.x, (float) Renderer.zoom.y);

		GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, 6, metaballs.length);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());

		//2nd pass
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GPUPass1OutTexture);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL20.glUseProgram(GPUPass2);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}