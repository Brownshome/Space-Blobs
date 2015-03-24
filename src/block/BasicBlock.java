package block;

import physics.collision.shapes.PolygonShape;
import physics.common.Vec2;
import physics.dynamics.FixtureDef;
import physics.link.Constants;

public class BasicBlock extends Block {
	int id;
	int[][] texture;
	
	public BasicBlock(int id, int texture) {
		this.id = id;
		this.texture = new int[][] {{texture, 0, 0, 0}};
	}
	
	@Override
	public int getID(int x, int y, BlockGroup parent) { return id; }
	
	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) { return texture; }

	@Override
	public double getHeatCapacity(int x, int y, BlockGroup parent) { return 1.0; }

	@Override
	public double getHeat(int x, int y, BlockGroup parent) { return parent.rawHeat(x, y); }
	
	Block getBlock(Direction dir, int x, int y, BlockGroup parent) {
		return parent.getBlock(x + (int) dir.offset.x, y + (int) dir.offset.y);
	}

	@Override
	public double getHeatResistivity(int x, int y, BlockGroup parent) {
		return 0.8;
	}

	@Override
	public FixtureDef[] getPhysics(int x, int y, BlockGroup parent) {
		FixtureDef fd = new FixtureDef();
		fd.filter.categoryBits = Constants.SHIP_BIT | Constants.SHIP_SELECTED_BIT;
		fd.friction = 0.3;
		fd.restitution = 0.9;
		fd.shape = new PolygonShape().setAsBox(parent.scale * 0.5, parent.scale * 0.5, new Vec2((x + parent.xoffset) * parent.scale, (y + parent.yoffset) * parent.scale), 0.0);
		fd.density = 10.0;
		fd.userData = new BlockFixtureData(x, y, parent);
		return new FixtureDef[] {fd};
	}
}
