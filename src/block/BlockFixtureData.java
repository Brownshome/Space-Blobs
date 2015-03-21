package block;

/** Represents the data needed to place a fixture within a blockgroup */
public class BlockFixtureData {
	int x;
	int y;
	BlockGroup owner;
	
	public BlockFixtureData(int x, int y, BlockGroup owner) {
		this.x = x;
		this.y = y;
		this.owner = owner;
	}
}
