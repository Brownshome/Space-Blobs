package block;

import java.util.Arrays;

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
	static {
		KeyBinds.add(BlockGroup::mouseClick, 0, KeyIO.MOUSE_BUTTON_PRESSED, "block.select");
	}

	static void mouseClick() {
		Fixture[] list = new PointTest(KeyBinds.getMousePos()).getFixtures();
		for(Fixture f : list) {
			if((f.m_filter.categoryBits & Constants.SHIP_SELECTED_BIT) != 0) {
				BlockFixtureData data = (BlockFixtureData) f.m_userData;
				data.owner.setSelected(data.x, data.y);
			}
		}
	}

	BlockGroupRenderer renderer;

	//used variables, need synchronization if multithreadedness happens
	int width;
	int height;
	double scale;
	int[] blocks;
	public double[] heat;
	
	int[] selected = null;

	int number;
	boolean updateRender = false;

	/** Use when building one from scratch */
	public BlockGroup(int id, double angle, Vec2 position, double scale) {
		BodyDef def = new BodyDef();
		def.position = position;
		def.angle = angle;

		innit(def);
		Game.getWorld().createBody(this);

		blocks = new int[] {id};
		width = 1;
		height = 1;
		this.scale = scale;
		Block initial = Block.getBlock(id);
		number = initial.getTextureLayers(0, 0, this);
		updateRender = initial.isVariableTexture(0, 0, this);

		FixtureDef fd = initial.getPhysics(0, 0, this);
		if(fd != null)
			createFixture(fd);
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

		blocks = ids;
		heat = new double[ids.length];
		this.width = width;
		this.height = height;
		this.scale = scale;
		number = 0;
		FixtureDef fd = null;

		int[] sensors = new int[(width + 2) * (height + 2)];

		for(int i = 0; i < ids.length; i++) {
			Block b = Block.getBlock(id(i));
			number += b.getTextureLayers(x(i), y(i), this);
			updateRender |= b.isVariableTexture(x(i), y(i), this);

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
				fd = new FixtureDef();
				fd.shape = new PolygonShape().setAsBox(scale * 0.5, scale * 0.5, new Vec2((i % (width + 2) - 1) * scale, (i / (width + 2) - 1) * scale), 0.0);
				fd.setSensor(true);
				fd.filter.categoryBits = Constants.SHIP_SELECTED_BIT;
				fd.userData = new BlockFixtureData((i % (width + 2)), (i / (width + 2)), this);

				createFixture(fd);
			}
		}
	}

	public void tickHeat() {
		//copy array
		double[] next = heat.clone();

		//calculate new vaues from conductance
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
		selected = new int[] {x, y};
	}

	public void unselect() {
		selected = null;
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
		if(x < 0)
			expandLeft(-x);

		if(x >= width)
			expandRight(x - width + 1);

		if(y < 0)
			expandDown(-y);

		if(y > 0)
			expandUp(y - height + 1);

		blocks[x + width * y] = id;
	}

	public void expandUp(int amount) {
		heat = Arrays.copyOf(heat, amount * width + heat.length);
		blocks = Arrays.copyOf(blocks, blocks.length + amount * width);
		height += amount;
	}

	public void expandDown(int amount) {
		double[] newHeat = new double[amount * width + heat.length];
		System.arraycopy(heat, 0, newHeat, amount * width, heat.length);
		double[] newBlocks = new double[amount * width + blocks.length];
		System.arraycopy(blocks, 0, newBlocks, amount * width, blocks.length);
		height += amount;
	}

	public void expandLeft(int amount) {

	}

	public void expandRight(int amount) {

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
		int[] result = new int[number * 6];
		int index = 0;
		for(int i = 0; i < blocks.length; i++) {
			for(int[] texture : getBlock(i).getTextures(x(i), y(i), this)) {
				result[index++] = x(i);
				result[index++] = y(i);
				result[index++] = texture[0];
				result[index++] = texture[1];
				result[index++] = texture[2];
				result[index++] = Float.floatToRawIntBits((float) (heat[i] / (heat[i] + 1000)));
			}
		}

		return result;
	}

	public Block getBlock(int x, int y) {
		return Block.getBlock(id(x, y));
	}
}
