package block;

import java.util.ArrayList;

import main.Game;
import physics.callbacks.QueryCallback;
import physics.collision.AABB;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.BodyType;
import physics.dynamics.Fixture;

/** detects all fixtures intersecting a point */

public class PointTest implements QueryCallback {
	final Vec2 point;
	ArrayList<Fixture> fixtures = new ArrayList<>();

	public PointTest(Vec2 point) {
		this.point = point;
		AABB broardTest = new AABB();
		broardTest.lowerBound.set(point.x - .001f, point.y - .001f);
		broardTest.upperBound.set(point.x + .001f, point.y + .001f);
		Game.getWorld().queryAABB(this, broardTest);
	}

	@Override
	public boolean reportFixture(Fixture argFixture) {
		Body body = argFixture.getBody();
		if (body.getType() == BodyType.DYNAMIC && argFixture.testPoint(point))
			fixtures.add(argFixture);

		return true;
	}

	public Fixture[] getFixtures() {
		return fixtures.toArray(new Fixture[0]);
	}
}
