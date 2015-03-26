package block;

import java.util.ArrayList;

import physics.common.Vec2;
import physics.dynamics.FixtureDef;

public abstract class Block {
	/* the block ID range is partitioned into several units:
	 * 1-4096 is basic blocks
	 * 4096 - 2^22 x 384 is data blocks each holding 22 bits of data 
	 * 2^29 * 3 - Integer.MAX_VALUE are object references to tile entities and blocks
	 * that cannot fit their data into 22 bits.
	 **/

	static Block[] BASIC_BLOCKS = new Block[4096];
	static final int DATA_BLOCK_SIZE = 0x400000; //2^22
	static Block[] DATA_BLOCKS = new Block[384];
	static ArrayList<Block> OBJECT_BLOCKS = new ArrayList<>(0);
	
	static {
		BASIC_BLOCKS[0] = new Empty();
		BASIC_BLOCKS[1] = new BasicBlock(1, BlockGroupRenderer.BASIC_HULL);
		BASIC_BLOCKS[2] = new BasicBlock(2, BlockGroupRenderer.BLUE_HULL);
		
		DATA_BLOCKS[0] = new HeatingBlock();
		DATA_BLOCKS[1] = new CornerBlock();
	}
	
	//error checking is not performed, so get your ID right
	/** Do not use negative values, they are reserved for special flags, 0 is the null block, used for empty space */
	public static Block getBlock(int id) {
		if(id < BASIC_BLOCKS.length)
			return BASIC_BLOCKS[id];
		
		id -= BASIC_BLOCKS.length;
		
		if(id / DATA_BLOCK_SIZE < DATA_BLOCKS.length)
			return DATA_BLOCKS[id / DATA_BLOCK_SIZE];
		
		id -= DATA_BLOCK_SIZE * DATA_BLOCKS.length;
		
		return OBJECT_BLOCKS.get(id);
	}
	
	/** Only the last 22 bits of data are used */
	public static int toDataBlockID(int id, int data) {
		return BASIC_BLOCKS.length + id * DATA_BLOCK_SIZE + data;
	}
	
	public static int toDataBlockID(int id) {
		return BASIC_BLOCKS.length + id * DATA_BLOCK_SIZE;
	}
	
	public static int toData(int id) {
		return (id - BASIC_BLOCKS.length) % DATA_BLOCK_SIZE;
	}
	
	/** Returns the physical fixture pertaining to this block, return null is valid */
	public abstract FixtureDef[] getPhysics(int x, int y, BlockGroup parent);
	/** Returns the array of textures to render, the indexs should be quereied at runtime and not hardcoded, in the order that they render, 2 textures, a lerp value and a rotation value */
	public abstract int[][] getTextures(int x, int y, BlockGroup parent);
	/** Return true if the block textures change, note that the number of textures cannot change*/
	public boolean isVariableTexture(int x, int y, BlockGroup parent) { return false; }
	/** Return the number of textures to render, this cannot change once called or BAD THINGS (tm) will happen*/
	public int getTextureLayers(int x, int y, BlockGroup parent) { return 1; }
	/** Returns if 'block' can be placed onto this side, don't worry about the shape of the other block, the other block's method is used for that
	 * if id = 0 return if the side is ever placable on */
	public boolean canBePlaced(Direction dir, int block, int x, int y, BlockGroup parent) { return true; }
	/** All blocks tick will be called for pass 0 then 1 ect. Data holds the data returned from the previous
	 * method. Return null to terminate passes */
	public Object[] tick(Object[] data, int pass, int x, int y, BlockGroup parent) { return null; }
	/** Returns the heat transfer rate in units per unit of difference per second (-100 WATER FREEZE, 0 NORMAL, 100 WATER BOIL, 1000 METAL MELT) */
	public abstract double getHeatConductance(int x, int y, BlockGroup parent);
	/** How many units of heat it takes to change one unit of tempurature in the block */
	public abstract double getHeatCapacity(int x, int y, BlockGroup parent);
	/** Gets the heat, the default method is to read the data from the blockgroup */
	public abstract double getHeat(int x, int y, BlockGroup parent);
	/** Called whenever a neighboring block changes */
	public void blockChange(Direction down, int x, int y, BlockGroup parent) {}
	/** Called when a block is created, the point is a relative point to (0, 0) to allow blocks to change type depending on where they are clicked */
	public int getID(Vec2 point, int x, int y, BlockGroup parent) { return getID(x, y, parent); }
	/** Called when created without an appropriate position */
	public abstract int getID(int x, int y, BlockGroup parent);
}
