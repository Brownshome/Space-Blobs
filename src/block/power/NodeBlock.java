package block.power;

import block.BlockGroup;

/** */
public interface NodeBlock extends PowerGridBlock {
	Conduit[] getConduits(int x, int y, BlockGroup parent);
	
	double getSinkAmount(int x, int y, BlockGroup parent);
	
	double getSourceAmount(int x, int y, BlockGroup parent);
}
