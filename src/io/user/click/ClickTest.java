package io.user.click;

import io.user.KeyBinds;
import main.Game;
import physics.callbacks.QueryCallback;
import physics.collision.AABB;
import physics.common.Vec2;
import physics.dynamics.Fixture;

/** detects the first fixture intersecting a point that is clickable */

public class ClickTest implements QueryCallback {
	public Vec2 point;
	public Fixture fixture;

	public ClickTest() {
		this.point = KeyBinds.getMousePos();
		AABB broardTest = new AABB();
		broardTest.lowerBound.set(point.x - .001f, point.y - .001f);
		broardTest.upperBound.set(point.x + .001f, point.y + .001f);
		Game.getWorld().queryAABB(this, broardTest);
	}

	@Override
	public boolean reportFixture(Fixture argFixture) {
		if (argFixture.getUserData() instanceof Clickable && argFixture.testPoint(point)) {
			fixture = argFixture;
			return false;
		}

		return true;
	}
}
