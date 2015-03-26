package block;

import physics.common.Vec2;

public class PowerCell extends DataBlock {
	final int maxValue;
	
	public PowerCell(int capacity) {
		maxValue = capacity;
	}
	
	@Override
	public int getDefaultData(Vec2 point, int x, int y, BlockGroup parent) {
		return 0; //TODO read from selected block
	}
}