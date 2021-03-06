package block;

import java.util.Arrays;
import java.util.stream.IntStream;

import main.Game;
import physics.collision.shapes.PolygonShape;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.BodyDef;
import physics.dynamics.BodyType;
import physics.dynamics.Fixture;
import physics.dynamics.FixtureDef;
import physics.link.Constants;

public class BlockGroup extends Body {
	static SelectedBlock selected;

	BlockGroupRenderer renderer;

	//used variables, need synchronization if multithreadedness happens
	public int width;
	public int height;
	public double scale;

	//arrays of data
	public int[] blocks;
	public double[] heat;
	public Fixture[][] fixtures; //the fixtures array is 2 larger on each axis, the array is 2D to accomodate complex block fixtures

	//fixture creation offset
	public int xoffset = 0;
	public int yoffset = 0;

	public int number;

	/** Use when building one from scratch */
	public BlockGroup(int id, double angle, Vec2 position, double scale) {
		BodyDef def = new BodyDef();
		def.position = position;
		def.angle = angle;

		innit(def);
		Game.getWorld().createBody(this);

		blocks = new int[] {id};
		fixtures = new Fixture[9][];
		width = 1;
		height = 1;
		this.scale = scale;
		Block initial = Block.getBlock(id);
		number = initial.getTextureLayers(0, 0, this) + 1; //the one is for the selection box, a more sensible system is needed

		FixtureDef[] fd = initial.getPhysics(0, 0, this);
		if(fd != null)
			createFixture(fd);
	}

	/** The override is so that the fixtures are kept in a data
	 * structure of my own as well as the body linked list
	 * this also destroys the old fixture at the location */
	public Fixture[] createFixture(FixtureDef[] fd) {
		BlockFixtureData bfd = (BlockFixtureData) fd[0].userData;
		int index = fi(bfd.x, bfd.y);

		if(fixtures[index] != null)
			for(Fixture f : fixtures[index])
				super.destroyFixture(f);

		fixtures[index] = new Fixture[fd.length];
		for(int i = 0; i < fd.length; i++) {
			fixtures[index][i] = super.createFixture(fd[i]);
		}

		return fixtures[index];
	}

	/** Use when creating BlockGroups from disk or generating existing structures */
	public BlockGroup(int[] ids, int width, int height, double angle, Vec2 position, double scale, Vec2 velocity) {
		BodyDef def = new BodyDef();
		def.position = position;
		def.angle = angle;
		def.linearVelocity = velocity;
		def.type = BodyType.DYNAMIC;

		innit(def);
		Game.getWorld().createBody(this);

		fixtures = new Fixture[(width + 2) * (height + 2)][];

		blocks = ids;
		heat = new double[ids.length];
		this.width = width;
		this.height = height;
		this.scale = scale;
		number = 1;
		FixtureDef[] fd = null;

		//(0, 0) is at 1, 1
		int[] sensors = new int[(width + 2) * (height + 2)];

		for(int i = 0; i < ids.length; i++) {
			Block b = Block.getBlock(id(i));
			number += b.getTextureLayers(x(i), y(i), this);

			if(b.canBePlaced(Direction.RIGHT, 0, x(i), y(i), this))
				sensors[fi(x(i) + 1, y(i))] |= 1;
			if(b.canBePlaced(Direction.LEFT, 0, x(i), y(i), this))
				sensors[fi(x(i) - 1, y(i))] |= 1;
			if(b.canBePlaced(Direction.UP, 0, x(i), y(i), this))
				sensors[fi(x(i), y(i) + 1)] |= 1;
			if(b.canBePlaced(Direction.DOWN, 0, x(i), y(i), this))
				sensors[fi(x(i), y(i) - 1)] |= 1;

			fd = b.getPhysics(x(i), y(i), this);

			if(fd != null) {
				createFixture(fd);
				sensors[fi(x(i), y(i))] = 2;
			}
		}

		for(int i = 0; i < sensors.length; i++) {
			if(sensors[i] == 1) {
				createSensor((i % (width + 2)) - 1, (i / (width + 2)) - 1);
			}
		}
	}

	public void tick() {
		//data is in the form {[x, y, id], block, null}
		Object[][] data =
			IntStream
			.range(0, blocks.length)
			.filter(i -> blocks[i] != 0)
			.mapToObj(i -> new Object[] {new int[] {x(i), y(i), blocks[i]}, Block.getBlock(blocks[i]), null})
			.toArray(Object[][]::new); 

		int length = data.length;
		int pass = 0;

		do {
			length = 0;
			for(int i = 0; i < data.length; i++) {
				if(data[i] == null)
					break;
				
				int[] xyid = (int[]) data[i][0];
				data[length++] = ((Block) data[i][1]).tick(data[i], pass, xyid[0], xyid[1], this);
			}
			
			pass++;
		} while(length != 0);
	}

	public int id(int x, int y) {
		if(x < 0 || y < 0 || x >= width)
			return 0;

		return id(x + y * width);
	}

	public void setSelected(int x, int y) {
		if(selected == null || selected.parent != this)
			number++;

		if(selected != null && selected.parent != this)
			selected.parent.number--;

		selected = new SelectedBlock(x, y, this);
	}

	public static void unselect() {
		if(selected != null) {
			selected.parent.number--;
			selected = null;
		}
	}

	public double heat(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height)
			return 0;

		return heat(x + y * width);
	}

	public double heat(int i) {
		return getBlock(i).getHeat(x(i), y(i), this);
	}

	/** sets a block without expanding, checking legality or updating */
	public void setBlockRaw(int x, int y, int id) {
		number -= getBlock(x, y).getTextureLayers(x, y, this);

		blocks[x + width * y] = id;

		number += Block.getBlock(id).getTextureLayers(x, y, this);

		FixtureDef[] fd = Block.getBlock(id).getPhysics(x, y, this);

		if(fd != null) {
			createFixture(fd);
		} else {
			if(hasBlockAdjacent(x, y)) {
				createSensor(x, y);
			}
		}
	}

	public void removeBlock(int x, int y) {
		if(id(x, y) == 0)
			return;

		setBlockRaw(x, y, 0);
		signalBlockChange(x, y);
		updateSensors(x, y, 0);

		renderer.resizeBuffer();

		//TODO check for all blocks removed
	}

	/** Sets the block without checking if the placement is legal */
	public void setBlockUnchecked(int x, int y, int id) {
		int[] xy = expandTo(x, y);
		x = xy[0];
		y = xy[1];

		setBlockRaw(x, y, id);
		signalBlockChange(x, y);
		updateSensors(x, y, id);
		renderer.resizeBuffer();
	}

	/** This method checks for connectivity, to build bulk structures use the bulk method, to remove use removeBlock() */
	public void setBlock(int x, int y, int id) {
		int[] xy = expandTo(x, y);
		x = xy[0];
		y = xy[1];

		if(!checkCanBePlaced(x, y, id))
			return;

		setBlockRaw(x, y, id);
		signalBlockChange(x, y);
		updateSensors(x, y, id);
		renderer.resizeBuffer();

		Block.getBlock(id).onPlace(x, y, this);
		
		//TODO break object into multiple parts.
	}

	public void signalBlockChange(int x, int y) {
		getBlock(id(x, y + 1)).blockChange(Direction.DOWN, x, y + 1, this);
		getBlock(id(x, y - 1)).blockChange(Direction.UP, x, y - 1, this);
		getBlock(id(x + 1, y)).blockChange(Direction.LEFT, x + 1, y, this);
		getBlock(id(x - 1, y)).blockChange(Direction.RIGHT, x - 1, y, this);
	}

	public void updateSensors(int x, int y, int id) {
		Block block = Block.getBlock(id);

		if(block.canBePlaced(Direction.UP, 0, x, y, this)) {
			if(fixtures[fi(x, y + 1)] == null)
				createSensor(x, y + 1);
		} else
			if(fixtures[fi(x, y + 1)] != null && fixtures[fi(x, y + 1)][0].m_isSensor && !hasBlockAdjacent(x, y + 1))
				removeSensor(x, y + 1);

		if(block.canBePlaced(Direction.DOWN, 0, x, y, this)) {
			if(fixtures[fi(x, y - 1)] == null)
				createSensor(x, y - 1);
		} else
			if(fixtures[fi(x, y - 1)] != null && fixtures[fi(x, y - 1)][0].m_isSensor && !hasBlockAdjacent(x, y - 1))
				removeSensor(x, y - 1);

		if(block.canBePlaced(Direction.RIGHT, 0, x, y, this)) {
			if(fixtures[fi(x + 1, y)] == null)
				createSensor(x + 1, y);
		} else
			if(fixtures[fi(x + 1, y)] != null && fixtures[fi(x + 1, y)][0].m_isSensor && !hasBlockAdjacent(x + 1, y))
				removeSensor(x + 1, y);

		if(block.canBePlaced(Direction.LEFT, 0, x, y, this)) {
			if(fixtures[fi(x - 1, y)] == null)
				createSensor(x - 1, y);
		} else
			if(fixtures[fi(x - 1, y)] != null && fixtures[fi(x - 1, y)][0].m_isSensor && !hasBlockAdjacent(x - 1, y))
				removeSensor(x - 1, y);
	}

	private final int[] POOL = new int[2];

	public int[] expandTo(int x, int y) {
		if(x < 0) {
			expandLeft(-x);
			x = 0;
		}

		if(x >= width) {
			expandRight(x - width + 1);
			x = width - 1;
		}

		if(y < 0) {
			expandDown(-y);
			y = 0;
		}

		if(y >= height) {
			expandUp(y - height + 1);
			y = height - 1;
		}

		POOL[0] = x;
		POOL[1] = y;

		return POOL;
	}

	/** checks if there is a supporting block next to (x, y) */
	public boolean hasBlockAdjacent(int x, int y) {
		return 	getBlock(x, y + 1).canBePlaced(Direction.DOWN, 0, x, y + 1, this) || 
			getBlock(x, y - 1).canBePlaced(Direction.UP, 0, x, y - 1, this) || 
			getBlock(x + 1, y).canBePlaced(Direction.LEFT, 0, x + 1, y, this) || 
			getBlock(x - 1, y).canBePlaced(Direction.RIGHT, 0, x - 1, y, this);
	}

	/** warning this does change block for a small period of time */
	public boolean checkCanBePlaced(int x, int y, int id) {
		int old = id(x, y);
		blocks[i(x, y)] = id;
		Block test = getBlock(x, y);

		if(getBlock(x, y + 1).canBePlaced(Direction.DOWN, id, x, y + 1, this) && test.canBePlaced(Direction.UP, id(x, y + 1), x, y, this)) {
			blocks[i(x, y)] = old;
			return true;
		}

		if(getBlock(x, y - 1).canBePlaced(Direction.UP, id, x, y - 1, this) && test.canBePlaced(Direction.DOWN, id(x, y - 1), x, y, this)) {
			blocks[i(x, y)] = old;
			return true;
		}

		if(getBlock(x + 1, y).canBePlaced(Direction.LEFT, id, x + 1, y, this) && test.canBePlaced(Direction.RIGHT, id(x + 1, y), x, y, this)) {
			blocks[i(x, y)] = old;
			return true;
		}

		if(getBlock(x - 1, y).canBePlaced(Direction.RIGHT, id, x - 1, y, this) && test.canBePlaced(Direction.LEFT, id(x - 1, y), x, y, this)) {
			blocks[i(x, y)] = old;
			return true;
		}

		blocks[i(x, y)] = old;
		return false;
	}

	private void createSensor(int x, int y) {
		FixtureDef fd = new FixtureDef();
		fd.shape = new PolygonShape().setAsBox(scale * 0.5, scale * 0.5, new Vec2((x + xoffset) * scale, (y + yoffset) * scale), 0.0);
		fd.setSensor(true);
		fd.filter.categoryBits = Constants.SHIP_SELECTED_BIT;
		fd.userData = new BlockFixtureData(x, y, this);

		createFixture(new FixtureDef[] {fd});
	}

	private void removeSensor(int x, int y) {
		int index = fi(x, y);
		super.destroyFixture(fixtures[index][0]);
		fixtures[index] = null;
	}

	public void expandUp(int amount) {
		heat = Arrays.copyOf(heat, amount * width + heat.length);
		blocks = Arrays.copyOf(blocks, blocks.length + amount * width);
		fixtures = Arrays.copyOf(fixtures, fixtures.length + amount * (width + 2));
		height += amount;
	}

	public void expandDown(int amount) {
		double[] newHeat = new double[amount * width + heat.length];
		System.arraycopy(heat, 0, newHeat, amount * width, heat.length);
		int[] newBlocks = new int[amount * width + blocks.length];
		System.arraycopy(blocks, 0, newBlocks, amount * width, blocks.length);
		Fixture[][] newFixtures = new Fixture[amount * (width + 2) + fixtures.length][];
		System.arraycopy(fixtures, 0, newFixtures, amount * (width + 2), fixtures.length);
		height += amount;
		heat = newHeat;
		blocks = newBlocks;
		fixtures = newFixtures;

		Fixture next = super.getFixtureList();
		yoffset--;

		if(selected != null && selected.parent == this)
			selected.y++;

		while(next != null) {
			BlockFixtureData bfd = (BlockFixtureData) next.getUserData();
			bfd.y++;
			next = next.getNext();
		}
	}

	public void expandLeft(int amount) {
		double[] newHeat = new double[amount * height + heat.length];
		int[] newBlocks = new int[amount * height + heat.length];
		Fixture[][] newFixtures = new Fixture[amount * (height + 2) + fixtures.length][];

		for(int row = 0; row < height; row++) {
			System.arraycopy(heat, row * width, newHeat, row * (width + amount) + amount, width);
			System.arraycopy(blocks, row * width, newBlocks, row * (width + amount) + amount, width);
			System.arraycopy(fixtures, row * (width + 2), newFixtures, row * (width + amount + 2) + amount, width + 2);
		}

		System.arraycopy(fixtures, height * (width + 2), newFixtures, height * (width + amount + 2) + amount, width + 2);
		System.arraycopy(fixtures, (height + 1) * (width + 2), newFixtures, (height + 1) * (width + amount + 2) + amount, width + 2);

		fixtures = newFixtures;
		blocks = newBlocks;
		heat = newHeat;

		width += amount;
		xoffset--;

		if(selected != null && selected.parent == this)
			selected.x++;

		Fixture next = super.getFixtureList();

		while(next != null) {
			BlockFixtureData bfd = (BlockFixtureData) next.getUserData();
			bfd.x++;
			next = next.getNext();
		}
	}

	public void expandRight(int amount) {
		double[] newHeat = new double[amount * height + heat.length];
		int[] newBlocks = new int[amount * height + heat.length];
		Fixture[][] newFixtures = new Fixture[amount * (height + 2) + fixtures.length][];

		for(int row = 0; row < height; row++) {
			System.arraycopy(heat, row * width, newHeat, row * (width + amount), width);
			System.arraycopy(blocks, row * width, newBlocks, row * (width + amount), width);
			System.arraycopy(fixtures, row * (width + 2), newFixtures, row * (width + 2 + amount), width + 2);
		}

		System.arraycopy(fixtures, height * (width + 2), newFixtures, height * (width + 2 + amount), width + 2);
		System.arraycopy(fixtures, (height + 1) * (width + 2), newFixtures, (height + 1) * (width + 2 + amount), width + 2);

		blocks = newBlocks;
		heat = newHeat;
		fixtures = newFixtures;

		width += amount;
	}

	public double rawHeat(int x, int y) {
		if(x < 0 || y < 0 || x >= width || y >= height)
			return 0;

		return rawHeat(x + y * width);
	}

	public double rawHeat(int i) {
		if(i < 0 || i >= heat.length)
			return 0;

		return heat[i];
	}

	public double resistivity(int x, int y) {
		return getBlock(x, y).getHeatConductance(x, y, this);
	}

	int x(int index) {
		return index % width;
	}

	int y(int index) {
		return index / width;
	}

	public int i(int x, int y) {
		return x + width * y;
	}

	/** Used for indexing the fixture array */
	int fi(int x, int y) {
		return (x + 1) + (width + 2) * (y + 1);
	}

	public long getUpdateOffset() {
		return 0; //OPTI implement subBuffer updates
	}

	/** Do not use for out of bounds values, this will not and cannot return meaningful results */
	public Block getBlock(int i) {
		return Block.getBlock(id(i));
	}

	public int id(int i) {
		if(i < 0 || i >= blocks.length)
			return 0;

		return blocks[i];
	}

	public int[] getRenderData() {
		int[] result = new int[number * BlockGroupRenderer.BLOCK_DATA_SIZE];
		int index = 0;

		for(int i = 0; i < blocks.length; i++) {
			for(int[] texture : getBlock(i).getTextures(x(i), y(i), this)) {
				result[index++] = x(i) + xoffset;
				result[index++] = y(i) + yoffset;
				result[index++] = texture[0];
				result[index++] = texture[1];
				result[index++] = texture[2];
				result[index++] = Float.floatToRawIntBits((float) (heat[i] / (heat[i] + 1000)));
				result[index++] = texture[3];
			}
		}

		if(selected != null && selected.parent == this) {
			result[index++] = selected.x + xoffset;
			result[index++] = selected.y + yoffset;
			result[index++] = BlockGroupRenderer.SELECTED_TEXTURE;
			result[index++] = 0;
			result[index++] = 0;
			result[index++] = 0;
			result[index++] = 0;
		}

		return result;
	}

	public Block getBlock(int x, int y) {
		return Block.getBlock(id(x, y));
	}
}