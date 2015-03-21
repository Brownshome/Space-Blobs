package math;

public enum Direction {
	UP(new Vec3i(0, 1, 0)),
	NORTH(new Vec3i(0, 0, -1)),
	EAST(new Vec3i(1, 0, 0)),
	SOUTH(new Vec3i(0, 0, 1)),
	WEST(new Vec3i(-1, 0, 0)),
	DOWN(new Vec3i(0, -1, 0));

	static final Direction[] OPPOSITE = new Direction[] {
		DOWN,
		SOUTH,
		WEST,
		NORTH,
		EAST,
		UP,
	};

	private Vec3i xyz;

	Direction(Vec3i xyz) {
		this.xyz = xyz;
	}

	public Vec3i vec() {
		return xyz;
	}

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}
}
