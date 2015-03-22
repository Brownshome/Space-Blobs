package block;

import io.user.Console;
import physics.common.Vec2;
import physics.dynamics.FixtureDef;
import physics.collision.shapes.PolygonShape;

public class CornerBlock extends BasicBlock {
	static Vec2[] getShape(double scale, int x, int y, int rot) {
		Vec2 o = new Vec2(x * scale, y * scale);
		scale *= 0.5;

		switch(rot) {
			case 0:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y - scale)};
			case 1:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y + scale)};
			case 2:
				return new Vec2[] {new Vec2(o.x + scale, o.y + scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y - scale)};
			case 3:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x + scale, o.y - scale), new Vec2(o.x + scale, o.y - scale)};
		}
		
		Console.error("Out of range rot value: " + rot, "PHYSICS MESH");
		return null;
	}

	public CornerBlock() {
		super(1, BlockGroupRenderer.HULL_CORNER);
	}

	public FixtureDef getPhysics(int x, int y, BlockGroup parent) {
		FixtureDef fd = super.getPhysics(x, y, parent);
		((PolygonShape) fd.shape).set(getShape(parent.scale, x, y, toData(parent.id(x, y))), 3);
		return fd;
	}

	public int[][] getTextures(int x, int y, BlockGroup parent) {
		texture[0][3] = toData(parent.id(x, y));
		return texture;
	}

	@Override
	public int getID(Vec2 point, int x, int y, BlockGroup parent) {
		int rot = point.x < 0 ? (point.y < 0 ? 0 : 1) : (point.y > 0 ? 2 : 3);
		return Block.toDataBlockID(1, rot);
	}
}