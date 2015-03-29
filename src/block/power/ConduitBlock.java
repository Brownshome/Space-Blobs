package block.power;

import block.Block;
import block.BlockGroup;
import block.DataBlock;
import block.Direction;

import static block.Direction.*;

//This is implemented as a dataBlock, the first 11 bits are used for conduit data that this is part of, the other 12 bits can be used
//for other purposes

public abstract class ConduitBlock extends DataBlock implements NodeBlock {
	public static final int IS_NODE = 0x400;
	public static final int CONDUIT_MASK = 0x3FF;
	
	/** Notify elements of the conduit to update flow rates, e.g. graphically */
	abstract void notifyConduitFlow(double amount, int x, int y, BlockGroup parent);
	/** Returns the maximum flow rate forward */
	abstract double getMaxPositiveFlow(int x, int y, BlockGroup parent);
	/** Returns the maximum flow rate backward, in most cases this will be the same as forward */
	abstract double getMaxNegativeFlow(int x, int y, BlockGroup parent);
	
	@Override
	public Node node(int x, int y, BlockGroup parent) {
		return conduit(x, y, parent).getEnd(x, y);
	}
	
	public Conduit conduit(int x, int y, BlockGroup parent) {
		return Conduit.ARRAY[conduitID(x, y, parent)];
	}
	
	public int conduitID(int x, int y, BlockGroup parent) {
		return getData(CONDUIT_MASK, x, y, parent);
	}
	
	/** Before this is called the surounding blocks must have updated their status with regards to Node */
	public void onPlace(int x, int y, BlockGroup parent) {
		//test neighbors, they will be one of several cases:
		//	the same conduitBlock,
		//	a conduit,
		//  a node,

		//if the neighbour is a conduit and this block doesn't already have an allocated conduit set conduit to
		//the neighbour's conduit. If the neighbour is a conduit and it's conduit doesn't equal this's one and
		//there are only two powerGridBlocks touching this one merge the two conduits. If there are more than 3
		//powerGridBlock's touching this one when it is placed.

		//step one, get the neighbouring blocks
		
		//NB, any conduit blocks that turn up are the ends of conduits, there is no other option
		
		PowerGridBlock[] blocks = new PowerGridBlock[4];

		int numberOfPowerGridBlocks = 0;
		int numberOfConduits = 0;
		int numberOfNodes = 0;
		
		Direction[] powerBlocks = new Direction[4];
		Direction[] conduits = new Direction[4];
		Direction[] nodes = new Direction[4];
		
		Block tmp = parent.getBlock(x, y + 1);
		if(tmp instanceof PowerGridBlock) {
			blocks[UP.ordinal()] = (PowerGridBlock) tmp;
			powerBlocks[numberOfPowerGridBlocks++] = UP;
			
			if(blocks[UP.ordinal()] instanceof ConduitBlock)
				conduits[numberOfConduits++] = UP;
			
			if(blocks[UP.ordinal()].isNode(x, y + 1, parent))
				nodes[numberOfNodes++] = UP;
		}

		tmp = parent.getBlock(x + 1, y);
		if(tmp instanceof PowerGridBlock) {
			blocks[RIGHT.ordinal()] = (PowerGridBlock) tmp;
			powerBlocks[numberOfPowerGridBlocks++] = RIGHT;
			
			if(blocks[RIGHT.ordinal()] instanceof ConduitBlock)
				conduits[numberOfConduits++] = RIGHT;
			
			if(blocks[RIGHT.ordinal()].isNode(x + 1, y, parent))
				nodes[numberOfNodes++] = RIGHT;
		}

		tmp = parent.getBlock(x, y - 1);
		if(tmp instanceof PowerGridBlock) {
			blocks[DOWN.ordinal()] = (PowerGridBlock) tmp;
			powerBlocks[numberOfPowerGridBlocks++] = DOWN;
			
			if(blocks[DOWN.ordinal()] instanceof ConduitBlock)
				conduits[numberOfConduits++] = DOWN;
			
			if(blocks[DOWN.ordinal()].isNode(x, y - 1, parent))
				nodes[numberOfNodes++] = DOWN;
		}

		tmp = parent.getBlock(x - 1, y);
		if(tmp instanceof PowerGridBlock) {
			blocks[LEFT.ordinal()] = (PowerGridBlock) tmp;
			powerBlocks[numberOfPowerGridBlocks++] = LEFT;
			
			if(blocks[LEFT.ordinal()] instanceof ConduitBlock)
				conduits[numberOfConduits++] = LEFT;
			
			if(blocks[LEFT.ordinal()].isNode(x - 1, y, parent))
				nodes[numberOfNodes++] = LEFT;
		}

		switch(numberOfPowerGridBlocks) {
			case 0:
				setData(CONDUIT_MASK, Conduit.newConduit(x, y, parent).ID, x, y, parent);
			case 1:
				if(numberOfNodes == 0) {
					Direction d = conduits[0];
					
					assert numberOfConduits == 1;
					
					Conduit c = ((ConduitBlock) blocks[d.ordinal()]).conduit(x + d.x, y + d.y, parent);
					setData(CONDUIT_MASK, c.ID, x, y, parent);
					c.addBlock(x, y, x + d.x, y + d.y);
				} else {
					assert numberOfNodes == 0 && numberOfConduits == 1;
					Direction d = nodes[0];
					
					Conduit c = Conduit.newConduit(x, y, parent);
					c.setEnd(((NodeBlock) blocks[d.ordinal()]).node(x + d.x, y + d.y, parent), x, y);
				}
				break;
			case 2:
				switch(numberOfNodes) {
					case 0:
						assert numberOfConduits == 2;
						
						Direction f = conduits[0];
						Direction s = conduits[1];
						
						Conduit fc = ((ConduitBlock) blocks[f.ordinal()]).conduit(x + f.x, y + f.y, parent);
						Conduit sc = ((ConduitBlock) blocks[s.ordinal()]).conduit(x + s.x, y + s.y, parent);
						
						fc.addBlock(x, y, f.x + x, f.y + y);
						setData(CONDUIT_MASK, fc.ID, x, y, parent);
						fc.merge(sc);
						break;
					case 1:
						assert numberOfConduits == 1;
						
						Direction dc = conduits[0];
						Direction dn = nodes[0];
						
						Node n = ((NodeBlock) blocks[dn.ordinal()]).node(x + dn.x, y + dn.y, parent);
						Conduit c = ((ConduitBlock) blocks[dc.ordinal()]).conduit(x + dc.x, y + dc.y, parent);
						
						c.addBlock(x, y, dc.x + x, dc.y + y);
						setData(CONDUIT_MASK, c.ID, x, y, parent);
						
						c.setEnd(n, x, y);
						
						break;
					case 2:
						assert numberOfConduits == 0;
						
						f = nodes[0];
						s = nodes[1];
						
						Node fn = ((NodeBlock) blocks[f.ordinal()]).node(x + f.x, y + f.y, parent);
						Node sn = ((NodeBlock) blocks[s.ordinal()]).node(x + s.x, y + s.y, parent);
						
						c = Conduit.newConduit(x, y, parent);
						
						//TODO make sure that the nodes are on opposite ends of the conduit
						c.setEnd(fn, x, y);
						c.setEnd(sn, x, y);
						
						break;
				}
				break;
			default:
				Node n = new Node();
				
				if(numberOfConduits == 0)
					setData(CONDUIT_MASK, Conduit.newConduit(x, y, parent).ID, x, y, parent);
				else {
					
					{
						Direction d = conduits[0];
						Conduit c = ((ConduitBlock) blocks[d.ordinal()]).conduit(x + d.x, y + d.y, parent);
						setData(CONDUIT_MASK, c.ID, x, y, parent);
						c.addBlock(x, y, d.x + x, d.y + y);
						c.setEnd(n, x, y);
					}
					
					for(int i = 1; i < numberOfConduits; i++) {
						Direction d = conduits[i];
						Conduit c = ((ConduitBlock) blocks[d.ordinal()]).conduit(x + d.x, y + d.y, parent);
						c.setEnd(n, x + d.x, y + d.y);
					}
					
					//must create a infinite conduit between this and the other node
					for(int i = 0; i < numberOfNodes; i++) {
						Direction d = nodes[i];
						Node other = ((NodeBlock) blocks[d.ordinal()]).node(x + d.x, y + d.y, parent);
						Conduit invisable = Conduit.newConduit(x, y, parent);
						invisable.setEnd(n, x, y);
						invisable.setEnd(other, x, y);
					}
				}
		}
	}

	@Override
	public boolean isNode(int x, int y, BlockGroup parent) {
		return (getData(x, y, parent) & IS_NODE) != 0;
	}

	public void blockChange(Direction down, int x, int y, BlockGroup parent) {
		//update isNode, the block is a node if there are more than 2 powerGridBlock s touching
		//it. If this block turns into a node and it is in a conduit split the conduit, even if it is at the end.
	}
}