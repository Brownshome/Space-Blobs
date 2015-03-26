package block;

import physics.common.Vec2;

/** Data value goes from 0 - 255 */
public class HeatingBlock extends DataBlock {
	static final double STRENGTH = 10.0;
	static final double SCALE = 10.0;

	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) { 
		return new int[][] {
			{
				BlockGroupRenderer.HEAT_GRATE_OFF, 
				BlockGroupRenderer.HEAT_GRATE_ON, 
				Float.floatToRawIntBits(getData(x, y, parent) / 255.0f),
				0
			}};
	}
	
	@Override
	public double getHeat(int x, int y, BlockGroup parent) {
		double heat = parent.rawHeat(x, y);
		double aim = getData(x, y, parent) * SCALE;
		
		if(heat > aim)
			return heat;
		else
			return heat + STRENGTH;
	}

	@Override
	public int getDefaultData(Vec2 point, int x, int y, BlockGroup parent) {
		return 0;
	}
}