package block;

import io.user.Console;
import physics.common.Vec2;
import physics.dynamics.FixtureDef;
import physics.collision.shapes.PolygonShape;

public class CornerBlock extends DataBlock {
	static Vec2[] getShape(double scale, int x, int y, int rot, int xoffset, int yoffset) {
		Vec2 o = new Vec2((x + xoffset) * scale, (y + yoffset) * scale);
		scale *= 0.5;

		switch(rot) {
			case 0:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y - scale)};
			case 1:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y + scale)};
			case 2:
				return new Vec2[] {new Vec2(o.x + scale, o.y + scale), new Vec2(o.x - scale, o.y + scale), new Vec2(o.x + scale, o.y - scale)};
			case 3:
				return new Vec2[] {new Vec2(o.x - scale, o.y - scale), new Vec2(o.x + scale, o.y - scale), new Vec2(o.x + scale, o.y + scale)};
		}
		
		Console.error("Out of range rot value: " + rot, "PHYSICS MESH");
		return null;
	}

	public CornerBlock() {
		texture = new int[][] {{BlockGroupRenderer.HULL_CORNER, 0, 0, 0}};
	}

	@Override
	public FixtureDef[] getPhysics(int x, int y, BlockGroup parent) {
		FixtureDef[] fd = super.getPhysics(x, y, parent);
		PolygonShape ps = new PolygonShape();
		ps.set(getShape(parent.scale, x, y, toData(parent.id(x, y)), parent.xoffset, parent.yoffset), 3);
		fd[0].shape = ps;
		return fd;
	}

	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) {
		texture[0][3] = getData(x, y, parent);
		return texture;
	}

	@Override
	public int getDefaultData(Vec2 point, int x, int y, BlockGroup parent) {
		return point.x < 0 ? (point.y < 0 ? 0 : 1) : (point.y > 0 ? 2 : 3);
	}
	
	@Override
	public boolean canBePlaced(Direction dir, int b, int x, int y, BlockGroup parent) {
		int rot = getData(x, y, parent);
		int d = dir.ordinal();
		
		switch(rot) {
			case 0:
				return d == 2 || d == 3;
			case 1:
				return d == 3 || d == 0;
			case 2:
				return d == 0 || d == 1;
			case 3:
				return d == 2 || d == 1;
		}
		
		throw new RuntimeException("How did this happen?");
	}
}