package render;

import physics.callbacks.DebugDraw;
import physics.common.Color;
import physics.common.OBBViewportTransform;
import physics.common.Transform;
import physics.common.Vec2;
import physics.particle.ParticleColor;

public class PhysRenderer extends DebugDraw {
	public static final PhysRenderer INSTANCE = new PhysRenderer();
	
	private PhysRenderer() {
		super(new OBBViewportTransform());
		m_drawFlags = e_shapeBit;
	}

	@Override
	public void drawPoint(Vec2 argPoint, double argRadiusOnScreen, Color argColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color color) {
		drawPolygon(vertices, vertexCount, color);
	}

	static final int CIRC_NUM = 24;
	
	@Override
	public void drawCircle(Vec2 center, double radius, Color color) {
		Vec2 last = center.add(radius, 0);
		for(int i = 0; i < CIRC_NUM + 1; i++) {
			double a = i * 2 * Math.PI / CIRC_NUM;
			LineRenderer.draw(last, (last = center.add(Math.cos(a) * radius, Math.sin(a) * radius)), color);
		}
	}

	@Override
	public void drawSolidCircle(Vec2 center, double radius, Vec2 axis, Color color) {
		drawCircle(center, radius, color);
	}

	@Override
	public void drawSegment(Vec2 p1, Vec2 p2, Color color) {
		LineRenderer.draw(p1, p2, color);
	}

	@Override
	public void drawTransform(Transform xf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(double x, double y, String s, Color color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawParticles(Vec2[] centers, double radius, ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawParticlesWireframe(Vec2[] centers, double radius, ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub
		
	}

}
