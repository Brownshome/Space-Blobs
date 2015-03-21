package io.user;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import physics.common.Vec2;
import render.Renderer;

/** Keeps track of keybinds and other such things */
public class KeyBinds {
	static class Action {
		Runnable action;
		int keyFlags = KeyIO.KEY_PRESSED;
		int current;
		int id;
		
		Action(Runnable a, int key) {
			action = a;
			current = key;
			id = KeyIO.addAction(action, current, keyFlags);
		}
		
		Action(Runnable a, int keyFlag, int key) {
			action = a;
			current = key;
			keyFlags = keyFlag;
			id = KeyIO.addAction(action, current, keyFlags);
		}
		
		public void rebind() {
			KeyIO.removeAction(id);
			KeyIO.addAction(action, current, keyFlags);
		}
	}
	
	static Map<String, Action> stringAssoc = new HashMap<>();
	
	public static void rebind(String name, int key) {
		Action a = stringAssoc.get(name);
		a.current = key;
		a.rebind();
	}
	
	public static void rebind(String name, int key, int flags) {
		Action a = stringAssoc.get(name);
		a.keyFlags = flags;
		a.current = key;
		a.rebind();
	}
	
	public static void add(Runnable r, int code, int flags, String name) {
		stringAssoc.put(name, new Action(r, flags, code));
	}

	/** In world space */
	public static Vec2 getMousePos() {
		double x = Mouse.getX();
		double y = Mouse.getY();
		x = 2 * x / Display.getWidth() - 1;
		y = 2 * y / Display.getHeight() - 1;
		
		return new Vec2(x / Renderer.zoom.x + Renderer.position.x, y / Renderer.zoom.y + Renderer.position.y);
	}
}
