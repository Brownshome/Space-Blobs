package block;

import io.user.click.Clickable;

/** Represents the data needed to place a fixture within a blockgroup */
public class BlockFixtureData implements Clickable {
	public int x;
	public int y;
	public BlockGroup owner;
	
	public BlockFixtureData(int x, int y, BlockGroup owner) {
		this.x = x;
		this.y = y;
		this.owner = owner;
	}
}
