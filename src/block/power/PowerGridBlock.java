package block.power;

import block.BlockGroup;

/** Every block that can form part of a power grid must implement this */
public interface PowerGridBlock {
	/** return null if you are a cable with no grid */
	public PowerGrid getGrid(int x, int y, BlockGroup parent);
	/** sets the power grid */
	public void setPowerGrid(int x, int y, BlockGroup parent, PowerGrid gird);
	/** returns whether this block is a node, also returns true for conduits that are blank nodes */
	public boolean isNode(int x, int y, BlockGroup parent);
}
