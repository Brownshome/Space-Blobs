package block;

import physics.dynamics.FixtureDef;

/** an empty block */
public class Empty extends Block {

	@Override
	public int getID(int x, int y, BlockGroup parent) { return 0; }
	
	@Override
	public FixtureDef[] getPhysics(int x, int y, BlockGroup parent) { return null; }

	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) { return new int[0][]; }

	@Override
	public double getHeatConductance(int x, int y, BlockGroup parent) { return 0.0; }

	@Override
	public int getTextureLayers(int x, int y, BlockGroup parent) { return 0; }

	@Override
	public boolean canBePlaced(Direction dir, int block, int x, int y, BlockGroup parent) { return false; }
	
	@Override
	public double getHeatCapacity(int x, int y, BlockGroup parent) { return Double.POSITIVE_INFINITY; }

	@Override
	public double getHeat(int x, int y, BlockGroup parent) { return 0; }
}
