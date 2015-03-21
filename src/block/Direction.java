package block;

import physics.common.Vec2;

public enum Direction {
	UP(new Vec2(0, 1)),
	LEFT(new Vec2(-1, 0)),
	RIGHT(new Vec2(1, 0)),
	DOWN(new Vec2(0, -1));
	
	public final Vec2 offset;
	
	Direction(Vec2 offset) {
		this.offset = offset;
	}
}
