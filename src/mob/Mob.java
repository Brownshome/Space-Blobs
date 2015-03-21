package mob;

import main.Game;
import metaballs.Ball;
import metaballs.Metaball;
import metaballs.MetaballShape;
import physics.collision.shapes.CircleShape;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.BodyDef;
import physics.dynamics.BodyType;
import physics.dynamics.Filter;
import physics.dynamics.FixtureDef;
import physics.dynamics.joints.RevoluteJoint;
import physics.dynamics.joints.RevoluteJointDef;

public abstract class Mob extends MetaballShape {
	public static final double DESNITY = 50.0;
	
	Metaball[] balls;
	Body root;
	RevoluteJoint[] joints;
	Body[] bodies;
	
	public Mob(Vec2 position, double[] size, int[][] connectivity, Vec2[] offsets) {
		setupRender(size.length);
		balls = new Metaball[size.length];

		BodyDef def = new BodyDef();
		FixtureDef fixDef = new FixtureDef();
		Filter filter = new Filter();
		filter.maskBits = ~0x2;
		filter.categoryBits = 0x2;
		fixDef.setFilter(filter);
		fixDef.setDensity(DESNITY);
		
		def.type = BodyType.DYNAMIC;
		def.position = position;
		
		bodies = new Body[size.length];
		
		for(int i = 0; i < size.length; i++) {
			def.position = position.add(offsets[i]);
			bodies[i] = Game.getWorld().createBody(def);
			balls[i] = new Ball(bodies[i].getPosition(), size[i]);
			fixDef.setShape(new CircleShape(size[i]));
			bodies[i].createFixture(fixDef);
		}
		
		root = bodies[0];

		joints = new RevoluteJoint[connectivity.length];
		
		RevoluteJointDef djd = new RevoluteJointDef();
		
		for(int i = 0; i < connectivity.length; i++) {
			djd.bodyA = bodies[connectivity[i][0]];
			djd.bodyB = bodies[connectivity[i][1]];
			djd.localAnchorB = djd.bodyA.getPosition().sub(djd.bodyB.getPosition());
			joints[i] = (RevoluteJoint) Game.getWorld().createJoint(djd);
		}
	}
	
	public void tick() {
		super.render(balls);
	}
}
