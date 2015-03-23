package block;

import physics.common.Vec2;

public enum Direction {
	UP(new Vec2(0, 1)),
	RIGHT(new Vec2(1, 0)),
	DOWN(new Vec2(0, -1)),
	LEFT(new Vec2(-1, 0));
	
	public final Vec2 offset;
	
	static final Direction[] OPPOSITE = new Direction[] {
		DOWN,
		LEFT,
		UP,
		RIGHT
	};
	
	Direction(Vec2 offset) {
		this.offset = offset;
	}
	
	Direction opposite() {
		return OPPOSITE[ordinal()];
	}
}
