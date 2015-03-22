package io.user.click;

import physics.common.Transform;
import physics.common.Vec2;
import block.Block;
import block.BlockFixtureData;
import block.BlockGroup;

public class CreativeBuildMode extends ClickMode {
	int id = 2;
	
	CreativeBuildMode() {
		super("clickmode.creativebuild", 16, 16);
	}

	@Override
	public void primary(Clickable c, Vec2 point) {
		if(c instanceof BlockFixtureData) {
			BlockFixtureData bfd = (BlockFixtureData) c;
			point = Transform.mul(bfd.owner.getTransform().invert(), point);
			point.mulLocal(1 / bfd.owner.scale);
			point.addLocal(-bfd.x, -bfd.y);

			bfd.owner.setBlock(bfd.x, bfd.y, Block.getBlock(id).getID(point, bfd.x, bfd.y, bfd.owner));
		}
	}

	@Override
	public void secondary(Clickable c, Vec2 point) {
		if(c instanceof BlockFixtureData) {
			BlockFixtureData bfd = (BlockFixtureData) c;
			bfd.owner.setBlock(bfd.x, bfd.y, 0);
		}
	}

	@Override
	public void hover(Clickable c, Vec2 point) {
		if(c == null)
			BlockGroup.unselect();
		
		if(c instanceof BlockFixtureData) {
			BlockFixtureData bfd = (BlockFixtureData) c;
			bfd.owner.setSelected(bfd.x, bfd.y);
		}
	}
	
	@Override
	public void deactivate() {
		BlockGroup.unselect();
	}
}
