package block;

import physics.common.Vec2;

public abstract class DataBlock extends BasicBlock {
	public abstract int getDefaultData(Vec2 point, int x, int y, BlockGroup parent);
	
	@Override
	public int getID(Vec2 point, int x, int y, BlockGroup parent) {
		return Block.toDataBlockID(1, getDefaultData(point, x, y, parent));
	}
	
	public int getData(int x, int y, BlockGroup parent) {
		return Block.toData(parent.id(x, y));
	}
}
