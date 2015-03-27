package block.power;

import block.BlockGroup;

/** Every block that can form part of a power grid must implement this */
public interface PowerGridBlock {
	/** return -1 if you are a cable with no grid */
	public int getGridNo(int x, int y, BlockGroup parent);
	/** sets the power grid */
	public void setPowerGrid(int x, int y, BlockGroup parent, int index);
}
