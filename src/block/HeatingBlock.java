package block;

public class HeatingBlock extends BasicBlock {
	static final int HEAT_ON = BlockGroupRenderer.getTexture("Heating Grate On");
	static final int HEAT_OFF = BlockGroupRenderer.getTexture("Heating Grate Off");
	
	public HeatingBlock() {
		super(0, 1000, 0);
	}

	@Override
	public int[][] getTextures(int x, int y, BlockGroup parent) { 
		int heat = Block.toData(parent.id(x, y));
		return new int[][] {{HEAT_OFF, HEAT_ON, Float.floatToRawIntBits(heat / 255.0f)}};
	}	
}
