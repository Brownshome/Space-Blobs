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
	
	public void setData(int mask, int data, int x, int y, BlockGroup parent) {
		int i = parent.i(x, y);
		parent.blocks[i] = (data & mask) & (parent.blocks[i] & ~mask);
		
		assert (data & mask) == (parent.blocks[i] & mask);
	}
	
	public int getData(int mask, int x, int y, BlockGroup parent) {
		return getData(x, y, parent) & mask;
	}
}
