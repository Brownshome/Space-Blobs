package block;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import io.user.KeyBinds;
import io.user.KeyIO;
import main.Game;
import physics.collision.shapes.PolygonShape;
import physics.common.Settings;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.BodyDef;
import physics.dynamics.BodyType;
import physics.dynamics.Fixture;
import physics.dynamics.FixtureDef;
import physics.link.Constants;

public class BlockGroup extends Body {
	static SelectedBlock selected;

	static {
		KeyBinds.add(BlockGroup::mouseClick, 0, KeyIO.MOUSE_BUTTON_PRESSED, "block.select");
		KeyBinds.add(() -> {if(selected != null) selected.parent.setBlock(selected.x, selected.y, 2);}, Keyboard.KEY_RETURN, KeyIO.KEY_PRESSED, "block.place");
	}

	static void mouseClick() {
		Fixture[] list = new PointTest(KeyBinds.getMousePos()).getFixtures();
		for(Fixture f : list) {
			if((f.m_filter.categoryBits & Constants.SHIP_SELECTED_BIT) != 0) {
				BlockFixtureData data = (BlockFixtureData) f.m_userData;
				data.owner.setSelected(data.x, data.y);
				return;
			}
		}
	}

	BlockGroupRenderer renderer;

	//used variables, need synchronization if multithreadedness happens
	int width;
	int height;
	double scale;

	//arrays of data
	int[] blocks;
	double[] heat;
	Fixture[] fixtures; //the fixtures array is 2 larger on each axis
	
	//fixture creation offset
	int xoffset = 0;
	int yoffset = 0;

	int number;

	/** Use when building one from scratch */
	public BlockGroup(int id, double angle, Vec2 position, double scale) {
		BodyDef def = new BodyDef();
		def.position = position;
		def.angle = angle;

		innit(def);
		Game.getWorld().createBody(this);

		blocks = new int[] {id};
		fixtures = new Fixture[9];
		width = 1;
		height = 1;
		this.scale = scale;
		Block initial = Block.getBlock(id);
		number = initial.getTextureLayers(0, 0, this) + 1; //the one is for the selection box, a more sensible system is needed

		FixtureDef fd = initial.getPhysics(0, 0, this);
		if(fd != null)
			createFixture(fd);
	}

	/** The override is so that the fixtures are kept in a data
	 * structure of my own as well as the body linked list
	 * this also destroys the old fixture at the location */
	@Override
	public Fixture createFixture(FixtureDef fd) {
		BlockFixtureData bfd = (BlockFixtureData) fd.userData;
		int index = fi(bfd.x, bfd.y);
		
		if(fixtures[index] != null)
			super.destroyFixture(fixtures[index]);
			
		return fixtures[index] = super.createFixture(fd);
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

		fixtures = new Fixture[(width + 2) * (height + 2)];
		
		blocks = ids;
		heat = new double[ids.length];
		this.width = width;
		this.height = height;
		this.scale = scale;
		number = 1;
		FixtureDef fd = null;

		int[] sensors = new int[(width + 2) * (height + 2)];

		for(int i = 0; i < ids.length; i++) {
			Block b = Block.getBlock(id(i));
			number += b.getTextureLayers(x(i), y(i), this);

			sensors[x(i) + 2 + (y(i) + 1) * (width + 2)] |= 1;
			sensors[x(i) + (y(i) + 1) * (width + 2)] |= 1;
			sensors[x(i) + 1 + (y(i) + 2) * (width + 2)] |= 1;
			sensors[x(i) + 1 + y(i) * (width + 2)] |= 1;

			fd = b.getPhysics(x(i), y(i), this);

			if(fd != null) {
				createFixture(fd);
				sensors[x(i) + y(i) * (width + 2)] = 2;
			}
		}

		for(int i = 0; i < sensors.length; i++) {
			if(sensors[i] == 1) {
				createSensor((i % (width + 2)) - 1, (i / (width + 2)) - 1);
			}
		}
	}

	public void tickHeat() {
		//copy array
		double[] next = heat.clone();

		//calculate new values from conductance
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++) {
				Block c = getBlock(x, y);

				double u = (Settings.DELTA * 2.0) / (resistivity(x, y + 1) + c.getHeatResistivity(x, y, this));
				double l = (Settings.DELTA * 2.0) / (resistivity(x - 1, y) + c.getHeatResistivity(x, y, this));
				double r = (Settings.DELTA * 2.0) / (resistivity(x + 1, y) + c.getHeatResistivity(x, y, this));
				double d = (Settings.DELTA * 2.0) / (resistivity(x, y - 1) + c.getHeatResistivity(x, y, this));

				double h = heat[x + y * width];

				next[x + y * width] += ((heat(x, y + 1) - h) * u + (heat(x - 1, y) - h) * l + (heat(x + 1, y) - h) * r + (heat(x, y - 1) - h) * d) / c.getHeatCapacity(x, y, this);
			}

		heat = next;
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
			selected = null;
			selected.parent.number--;
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

	public void setBlock(int x, int y, int id) {
		
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

		number -= getBlock(x, y).getTextureLayers(x, y, this);
		number += Block.getBlock(id).getTextureLayers(x, y, this);

		blocks[x + width * y] = id;
		FixtureDef fd = Block.getBlock(id).getPhysics(x, y, this);
		createFixture(fd);
		
		getBlock(id(x, y + 1)).blockChange(Direction.DOWN);
		getBlock(id(x, y - 1)).blockChange(Direction.UP);
		getBlock(id(x + 1, y)).blockChange(Direction.LEFT);
		getBlock(id(x - 1, y)).blockChange(Direction.RIGHT);

		if(fixtures[fi(x, y + 1)] == null)
			createSensor(x, y + 1);
		
		if(fixtures[fi(x, y - 1)] == null)
			createSensor(x, y - 1);
		
		if(fixtures[fi(x + 1, y)] == null)
			createSensor(x + 1, y);
		
		if(fixtures[fi(x - 1, y)] == null)
			createSensor(x - 1, y);
		
		renderer.resizeBuffer();
	}

	private void createSensor(int x, int y) {
		FixtureDef fd = new FixtureDef();
		fd.shape = new PolygonShape().setAsBox(scale * 0.5, scale * 0.5, new Vec2((x + xoffset) * scale, (y + yoffset) * scale), 0.0);
		fd.setSensor(true);
		fd.filter.categoryBits = Constants.SHIP_SELECTED_BIT;
		fd.userData = new BlockFixtureData(x, y, this);

		createFixture(fd);
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
		Fixture[] newFixtures = new Fixture[amount * (width + 2) + fixtures.length];
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
		Fixture[] newFixtures = new Fixture[amount * (height + 2) + fixtures.length];
		
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
		Fixture[] newFixtures = new Fixture[amount * (height + 2) + fixtures.length];
		
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
		return getBlock(x, y).getHeatResistivity(x, y, this);
	}

	int x(int index) {
		return index % width;
	}

	int y(int index) {
		return index / width;
	}

	int i(int x, int y) {
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