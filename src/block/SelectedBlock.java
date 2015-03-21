package block;

public class SelectedBlock {
	int x;
	int y;
	BlockGroup parent;
	
	public SelectedBlock(int x, int y, BlockGroup parent) {
		this.x = x;
		this.y = y;
		this.parent = parent;
	}
}
