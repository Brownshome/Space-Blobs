package block.power;

import block.BlockGroup;

/** Every block that has a node associated with it must implement this class, except for conduit blank nodes */
public interface NodeBlock extends PowerGridBlock {
	/** Returns the connecting conduits to this node */
	Conduit[] getConduits(int x, int y, BlockGroup parent);
	/** Return the maximum amount that the Node can accept, must be positive */
	double getSinkAmount(int x, int y, BlockGroup parent);
	/** Returns the maximum amount that the Node can supply to the network */
	double getSourceAmount(int x, int y, BlockGroup parent);
	/** Returns the node that this Block represents */
	Node node(int x, int y, BlockGroup parent);
	
	@Override
	default boolean isNode(int x, int y, BlockGroup parent) { return true; }
}
