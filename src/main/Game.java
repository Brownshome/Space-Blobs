package main;

import io.user.Console;
import io.user.KeyIO;
import io.user.click.ClickMode;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import physics.collision.shapes.PolygonShape;
import physics.common.Settings;
import physics.common.Vec2;
import physics.dynamics.BodyDef;
import physics.dynamics.BodyType;
import physics.dynamics.World;
import physics.link.ContactResponse;
import render.LineRenderer;
import render.PhysRenderer;
import render.Renderer;
import render.TextRenderer;
import time.Clock;
import ui.GUI;
import block.Block;
import block.BlockGroup;
import block.BlockGroupRenderer;

public class Game {
	public static final int CLOCK_WARM_UP = 2000;
	public static volatile boolean exitFlag = false;
	public static boolean drawPhys = false;
	public static Clock frameClock = new Clock();
	public static World world = new World(new Vec2());
	
	public static void main(String[] args) {
		innit();
		
		try {
			mainLoop();
		} catch (Exception e) {
			Console.error(e, "UNKNOWN");
		} finally {
			cleanup();
		}
	}

	static void mainLoop() {
		int[] grid = new int[] {
			1
		};
		
		BodyDef bd = new BodyDef();
		bd.setType(BodyType.STATIC);
		bd.setPosition(new Vec2(0, -1.5));
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1.0, 1.0);
		
		BlockGroup bg1 = new BlockGroup(grid, 1, 1, 0.0, new Vec2(), 0.2, new Vec2());
		
		world.setDebugDraw(PhysRenderer.INSTANCE);
		world.setContactListener(ContactResponse.INSTANCE);
		
		BlockGroupRenderer bgr1 = new BlockGroupRenderer(bg1);
		bgr1.setupRender();
		
		GUI.setDebug(true);
		TextRenderer.initialize("kristen");
		
		while(!Display.isCloseRequested() && !exitFlag) {
			Display.update();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			world.step(Settings.DELTA, 8, 8);
			
			frameClock.tick();
			ClickMode.tick();
			KeyIO.tick();
			GUI.INSTANCE.tick();
			
			bg1.tick();
			bgr1.render();
			
			TextRenderer.render();
			LineRenderer.render();
			
			if(drawPhys)
				world.drawDebugData();

			Renderer.checkGL();
		}
	}
	
	private static void innit() {
		Renderer.initialize();
		ClickMode.initialize();
		KeyIO.addAction(() -> exitFlag = true, Keyboard.KEY_ESCAPE, KeyIO.KEY_PRESSED);
		KeyIO.addAction(() -> GUI.setDebug(!GUI.debug), Keyboard.KEY_F3, KeyIO.KEY_PRESSED);
		KeyIO.addAction(() -> drawPhys = !drawPhys, Keyboard.KEY_F4, KeyIO.KEY_PRESSED);
	}

	public static void cleanup() {
		//TODO technically the context should clean up after me, but...

		Display.destroy();
	}

	public static World getWorld() {
		return world;
	}
}
