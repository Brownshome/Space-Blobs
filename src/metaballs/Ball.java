package metaballs;

import physics.common.Vec2;
import math.Box;

public class Ball extends Metaball {
	public Vec2 p;
	public double scale;
	
	/** position is in world space */
	public Ball(Vec2 position, double scale) {
		p = position;
		this.scale = scale;
	}

	@Override
	float[] getData() {
		return new float[] {(float) p.x, (float) p.y, (float) scale};
	}

	static final double CONST = Math.sqrt(2);
	
	@Override
	public Box boundingBox() {
		return new Box(p.x - scale * CONST, p.x + scale * CONST, p.y - scale * CONST, p.y + scale * CONST);
	}
}
