package block.power;

import java.util.Arrays;

import block.BlockGroup;

/** Represents a path down which power can flow in a power grid */
class Conduit {
	public static final Conduit[] ARRAY = {};
	
	BlockGroup parent;
	/** The position of this conduit in ARRAY */
	int ID;
	
	/** The group of conduitBlocks that make up this Conduit */
	int[][] blocks;
	
	Node in;
	Node out;
	
	/** Creates a conduit with infinite capacity */
	Conduit() {}
	
	/** Creates a conduit with only one block */
	public Conduit(PowerGrid grid, BlockGroup parent, int x, int y) {
		blocks = new int[][] {{x, y}};
		this.parent = parent;
	}

	void merge(Conduit other) {
		//TODO
	}
	
	/** xy is the block at the end of the connection that the node is being added to */
	void setEnd(Node n, int x, int y) {
		if(blocks[0][0] == x && blocks[0][1] == y) {
			in = n;
			n.addOut(this);
		} else {
			assert blocks[blocks.length - 1][0] == x && blocks[blocks.length - 1][1] == y;
			out = n;
			n.addIn(this);
		}
	}
	
	/** from is the block that was touching the conduit */
	int addBlock(int x, int y, int xfrom, int yfrom) {
		int[][] newBlocks = new int[blocks.length + 1][];
		
		if(blocks[0][0] == xfrom && blocks[0][1] == yfrom) {
			System.arraycopy(blocks, 0, newBlocks, 1, blocks.length);
			newBlocks[0] = new int[] {x, y};
		} else {
			assert blocks[blocks.length - 1][0] == x && blocks[blocks.length - 1][1] == y;
			System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
			newBlocks[blocks.length] = new int[] {x, y};
		}
		
		blocks = newBlocks;
		return ID;
	}
	
	int fillInequality(int n, int index, double[][] mults, double[] limits) {
		if(blocks == null)
			return 0;
		
		mults[n][index] = 1;
		mults[n + 1][index] = -1;
		limits[n] = getMaxPositiveFlow();
		limits[n + 1] = getMaxNegativeFlow();
		return 2;
	}

	void notify(double flowRate) {
		if(blocks != null)
			for(int[] xy : blocks)
				((ConduitBlock) parent.getBlock(xy[0], xy[1])).notifyConduitFlow(flowRate, xy[0], xy[1], parent);
	}
	
	double getMaxPositiveFlow() {
		return Arrays.stream(blocks)
				.mapToDouble(xy -> 
				((ConduitBlock) parent.getBlock(xy[0], xy[0])).getMaxPositiveFlow(xy[0], xy[1], parent))
				.max()
				.orElse(Double.POSITIVE_INFINITY);
	}
	
	double getMaxNegativeFlow() {
		return Arrays.stream(blocks)
				.mapToDouble(xy -> 
				((ConduitBlock) parent.getBlock(xy[0], xy[0])).getMaxNegativeFlow(xy[0], xy[1], parent))
				.max()
				.orElse(Double.POSITIVE_INFINITY);
	}

	public Node getEnd(int x, int y) {
		if(blocks[0][0] == x && blocks[0][1] == y) {
			return in;
		} else {
			assert blocks[blocks.length - 1][0] == x && blocks[blocks.length - 1][1] == y;
			return out;
		}
	}

	public static Conduit newConduit(int x, int y, BlockGroup parent2) {
		// TODO Auto-generated method stub
		return null;
	}
}
