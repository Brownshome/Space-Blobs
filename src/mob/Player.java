package mob;

import io.user.KeyBinds;
import io.user.KeyIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import main.Game;

import org.lwjgl.input.Keyboard;

import physics.collision.shapes.CircleShape;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.Filter;
import physics.dynamics.FixtureDef;
import physics.dynamics.joints.Joint;
import physics.dynamics.joints.RevoluteJoint;
import physics.dynamics.joints.WeldJointDef;
import physics.link.Constants;

public class Player extends Mob {
	public static final double STRENGTH = 0.05;
	public static final double SPEED = 10;
	static Player controlled;
	
	static {
		KeyBinds.add(() -> {if(controlled != null) controlled.up();}, Keyboard.KEY_W, KeyIO.KEY_DOWN, "player.jetpack.up");
		KeyBinds.add(() -> {if(controlled != null) controlled.down();}, Keyboard.KEY_S, KeyIO.KEY_DOWN, "player.jetpack.down");
		KeyBinds.add(() -> {if(controlled != null) controlled.left();}, Keyboard.KEY_A, KeyIO.KEY_DOWN, "player.jetpack.left");
		KeyBinds.add(() -> {if(controlled != null) controlled.right();}, Keyboard.KEY_D, KeyIO.KEY_DOWN, "player.jetpack.right");
		KeyBinds.add(() -> {if(controlled != null) controlled.toggleJetpack();}, Keyboard.KEY_R, KeyIO.KEY_PRESSED, "player.jetpack.toggle");
		KeyBinds.add(() -> {if(controlled != null) controlled.grab();}, Keyboard.KEY_G, KeyIO.KEY_PRESSED, "player.grab.true");
	}
	
	double[] aim;
	double angle;
	boolean jetpackon = false;
	HashMap<Body, Body> grabable = new HashMap<>();
	ArrayList<Joint> grabbed = new ArrayList<>();
	
	public Player(Vec2 position) {
		super(
			position, 
			new double[] {0.03, 0.015, 0.015, 0.02, 0.02, 0.025}, 
			new int[][] {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}}, 
			new Vec2[] {new Vec2(), new Vec2(0.04, 0.01), new Vec2(-0.04, 0.01), new Vec2(0.03, -0.04), new Vec2(-0.03, -0.04), new Vec2(0, 0.05)}
		);
		
		FixtureDef def = new FixtureDef();
		def.isSensor = true;
		def.shape = new CircleShape();
		Filter f = new Filter();
		f.categoryBits = Constants.PLAYER_GRAB_BIT;
		f.maskBits = Constants.SHIP_BIT;
		def.filter = f;
		
		for(int i = 1; i < 5; i++) {
			def.shape.m_radius = bodies[i].m_fixtureList.m_shape.m_radius + 0.01;
			bodies[i].m_userData = this;
			bodies[i].createFixture(def);
		}
		
		aim = new double[5];
		for(RevoluteJoint j : joints) {
			j.enableMotor(true);
			j.setMaxMotorTorque(STRENGTH);
		}
	}
	
	public void addGrabable(Body a, Body b) {
		grabable.put(a, b);
	}
	
	public void removeGrabable(Body a) {
		grabable.remove(a);
	}
	
	public void grab() {
		if(KeyIO.isShiftDown()) {
			letGo();
		} else {

			WeldJointDef wjd = new WeldJointDef();
			wjd.dampingRatio = 1.0;
			wjd.frequencyHz = 2.0;
			wjd.collideConnected = true;

			for(Entry<Body, Body> g : grabable.entrySet()) {
				wjd.initialize(g.getKey(), g.getValue(), g.getValue().getPosition());
				grabbed.add(Game.getWorld().createJoint(wjd));
			}
		}
	}
	
	public void letGo() {
		for(Joint g : grabbed) {
			Game.getWorld().destroyJoint(g);
		}
	}
	
	public void setControlled() {
		controlled = this;
	}
	
	public static void clearControlled() {
		controlled = null;
	}
	
	@Override
	public void tick() {
		super.tick();
		for(int i = 0; i < joints.length; i++) {
			joints[i].setMotorSpeed(-(joints[i].getJointSpeed() * 0.1 + joints[i].getJointAngle()) * SPEED);
		}
		
		if(jetpackon)
			root.applyTorque(-root.getAngle() * 0.01);
	}
	
	void    up() {	if(jetpackon) root.applyForceToCenter(new Vec2(0, 1));	}
	void  down() {	if(jetpackon) root.applyForceToCenter(new Vec2(0, -1));	}
	void  left() {	if(jetpackon) root.applyForceToCenter(new Vec2(-1, 0));	}
	void right() {	if(jetpackon) root.applyForceToCenter(new Vec2(1, 0));	}
	
	void toggleJetpack() {
		jetpackon = !jetpackon;
		root.setAngularDamping(jetpackon ? 0.05 : 0);
	}
}
