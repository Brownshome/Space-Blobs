package block;

public class HeatingBlock extends BasicBlock {
	public HeatingBlock() {
		super(1000, 0);
	}

	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) { 
		int heat = Block.toData(parent.id(x, y));
		return new int[][] {{BlockGroupRenderer.HEAT_GRATE_OFF, BlockGroupRenderer.HEAT_GRATE_OFF, Float.floatToRawIntBits(heat / 255.0f)}};
	}	
}
