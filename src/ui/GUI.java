package ui;

import main.Game;
import physics.common.Vec2;
import render.TextRenderer;

public class GUI {
	public static final GUI INSTANCE = new GUI();
	public static boolean debug = false;

	static {
		setDebug(true);
	}

	public static void setDebug(boolean isON) {
		if(debug != isON)
			if(debug)
				TextRenderer.clearStaticTextList();
			else {
				TextRenderer.text("X:", true, new Vec2(-0.98f, 0.95f), 0.03f);
				TextRenderer.text("Y:", true, new Vec2(-0.98f, 0.90f), 0.03f);
				TextRenderer.text("FPS:", true, new Vec2(-0.98f, 0.85f), 0.03f);
			}

		debug = isON;
	}

	public void tick() {
		if(debug) {
			TextRenderer.text(String.format("%.2f", 0.0), false, new Vec2(-1f + 0.08f, 0.95f), 0.03f);
			TextRenderer.text(String.format("%.2f", 0.0), false, new Vec2(-1f + 0.08f, 0.90f), 0.03f);
			TextRenderer.text(String.valueOf((int) Game.frameClock.getTPS()), false, new Vec2(-1f + 0.14f, 0.85f), 0.03f);
		}
	}
}
