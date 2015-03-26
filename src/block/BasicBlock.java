package block;

import physics.collision.shapes.PolygonShape;
import physics.common.Settings;
import physics.common.Vec2;
import physics.dynamics.FixtureDef;
import physics.link.Constants;

public class BasicBlock extends Block {
	int id;
	int[][] texture;
	
	/** Use this one for subclasses */
	public BasicBlock(int id, int[][] texture) {
		this.texture = texture;
		this.id = id;
	}
	
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
	public double getHeatConductance(int x, int y, BlockGroup parent) {
		return 1.0;
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

	/** This tick is for heat flows, 1st pass calculates new heat, second pass sets it */
	@Override
	public Object[] tick(Object[] data, int pass, int x, int y, BlockGroup parent) {
		switch(pass) {
			case 0:
				data[2] = getNewHeat(x, y, parent);
				return data;
			case 1:
				parent.heat[parent.i(x, y)] = (double) data[2];
		}
		
		return null;
	}

	private double getNewHeat(int x, int y, BlockGroup parent) {
		double h = parent.heat(x, y);
		double hU = parent.heat(x, y + 1);
		double hR = parent.heat(x + 1, y);
		double hL = parent.heat(x - 1, y);
		double hD = parent.heat(x, y - 1);
		
		double c = getHeatConductance(x, y, parent);
		
		double cU = parent.getBlock(x, y + 1).getHeatConductance(x, y + 1, parent);
		double cR = parent.getBlock(x + 1, y).getHeatConductance(x + 1, y, parent);
		double cL = parent.getBlock(x - 1, y).getHeatConductance(x - 1, y, parent);
		double cD = parent.getBlock(x, y - 1).getHeatConductance(x, y - 1, parent);
		
		//1 find rates
		double rU = (hU - h) * Math.min(cU, c) * Settings.DELTA;
		double rL = (hL - h) * Math.min(cL, c) * Settings.DELTA;
		double rD = (hD - h) * Math.min(cD, c) * Settings.DELTA;
		double rR = (hR - h) * Math.min(cR, c) * Settings.DELTA;
		
		//2 detect overflow
		//TODO
		
		//3 move heat
		return h + (rU + rL + rD + rR) * getHeatCapacity(x, y, parent);
	}
}