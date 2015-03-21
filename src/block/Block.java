package block;

import java.util.ArrayList;

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
		BASIC_BLOCKS[1] = new BasicBlock(1, 1000, 0);
		BASIC_BLOCKS[2] = new BasicBlock(2, 50, 1);
		BASIC_BLOCKS[3] = new BasicBlock(3, 1000, 2);
		
		DATA_BLOCKS[0] = new HeatingBlock();
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
	
	public static int toData(int id) {
		return (id - BASIC_BLOCKS.length) % DATA_BLOCK_SIZE;
	}
	
	/** Returns the physical fixture pertaining to this block, return null is valid */
	public abstract FixtureDef getPhysics(int x, int y, BlockGroup parent);
	/** Returns the array of textures to render, the indexs should be quereied at runtime and not hardcoded, in the order that they render, 2 textures and a lerp value */
	public abstract int[][] getTextures(int x, int y, BlockGroup parent);
	/** Return true if the block textures change, note that the number of textures cannot change*/
	public boolean isVariableTexture(int x, int y, BlockGroup parent) { return false; }
	/** Return the number of textures to render, this cannot change once called or BAD THINGS (tm) will happen*/
	public int getTextureLayers(int x, int y, BlockGroup parent) { return 1; }
	/** Returns if the block is solid on the face, used in culling the physics mesh among other things*/
	public boolean isSideSolid(Direction dir, int x, int y, BlockGroup parent) { return true; }
	/** Returns the heat transfer rate in unit of difference per units per second  */
	public abstract double getHeatResistivity(int x, int y, BlockGroup parent);
	/** How many units of heat it takes to change one unit of tempurature in the block */
	public abstract double getHeatCapacity(int x, int y, BlockGroup parent);
	/** Gets the heat, the default method is to read the data from the blockgroup */
	public abstract double getHeat(int x, int y, BlockGroup parent);
}
