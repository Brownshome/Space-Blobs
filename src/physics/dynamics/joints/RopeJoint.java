package physics.dynamics.joints;

import physics.common.MathUtils;
import physics.common.Rot;
import physics.common.Settings;
import physics.common.Vec2;
import physics.dynamics.SolverData;
import physics.pooling.IWorldPool;

/**
 * A rope joint enforces a maximum distance between two points on two bodies. It has no other
 * effect. Warning: if you attempt to change the maximum length during the simulation you will get
 * some non-physical behavior. A model that would allow you to dynamically modify the length would
 * have some sponginess, so I chose not to implement it that way. See DistanceJoint if you want to
 * dynamically control length.
 * 
 * @author Daniel Murphy
 */
public class RopeJoint extends Joint {
  // Solver shared
  private final Vec2 m_localAnchorA = new Vec2();
  private final Vec2 m_localAnchorB = new Vec2();
  private double m_maxLength;
  private double m_length;
  private double m_impulse;

  // Solver temp
  private int m_indexA;
  private int m_indexB;
  private final Vec2 m_u = new Vec2();
  private final Vec2 m_rA = new Vec2();
  private final Vec2 m_rB = new Vec2();
  private final Vec2 m_localCenterA = new Vec2();
  private final Vec2 m_localCenterB = new Vec2();
  private double m_invMassA;
  private double m_invMassB;
  private double m_invIA;
  private double m_invIB;
  private double m_mass;
  private LimitState m_state;

  protected RopeJoint(IWorldPool worldPool, RopeJointDef def) {
    super(worldPool, def);
    m_localAnchorA.set(def.localAnchorA);
    m_localAnchorB.set(def.localAnchorB);

    m_maxLength = def.maxLength;

    m_mass = 0.0f;
    m_impulse = 0.0f;
    m_state = LimitState.INACTIVE;
    m_length = 0.0f;
  }

  @Override
  public void initVelocityConstraints(final SolverData data) {
    m_indexA = m_bodyA.m_islandIndex;
    m_indexB = m_bodyB.m_islandIndex;
    m_localCenterA.set(m_bodyA.m_sweep.localCenter);
    m_localCenterB.set(m_bodyB.m_sweep.localCenter);
    m_invMassA = m_bodyA.m_invMass;
    m_invMassB = m_bodyB.m_invMass;
    m_invIA = m_bodyA.m_invI;
    m_invIB = m_bodyB.m_invI;

    Vec2 cA = data.positions[m_indexA].c;
    double aA = data.positions[m_indexA].a;
    Vec2 vA = data.velocities[m_indexA].v;
    double wA = data.velocities[m_indexA].w;

    Vec2 cB = data.positions[m_indexB].c;
    double aB = data.positions[m_indexB].a;
    Vec2 vB = data.velocities[m_indexB].v;
    double wB = data.velocities[m_indexB].w;

    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();
    final Vec2 temp = pool.popVec2();

    qA.set(aA);
    qB.set(aB);

    // Compute the effective masses.
    Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subLocal(m_localCenterA), m_rA);
    Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subLocal(m_localCenterB), m_rB);

    m_u.set(cB).addLocal(m_rB).subLocal(cA).subLocal(m_rA);

    m_length = m_u.length();

    double C = m_length - m_maxLength;
    if (C > 0.0f) {
      m_state = LimitState.AT_UPPER;
    } else {
      m_state = LimitState.INACTIVE;
    }

    if (m_length > Settings.linearSlop) {
      m_u.mulLocal(1.0f / m_length);
    } else {
      m_u.setZero();
      m_mass = 0.0f;
      m_impulse = 0.0f;
      return;
    }

    // Compute effective mass.
    double crA = Vec2.cross(m_rA, m_u);
    double crB = Vec2.cross(m_rB, m_u);
    double invMass = m_invMassA + m_invIA * crA * crA + m_invMassB + m_invIB * crB * crB;

    m_mass = invMass != 0.0f ? 1.0f / invMass : 0.0f;

    if (data.step.warmStarting) {
      // Scale the impulse to support a variable time step.
      m_impulse *= data.step.dtRatio;

      double Px = m_impulse * m_u.x;
      double Py = m_impulse * m_u.y;
      vA.x -= m_invMassA * Px;
      vA.y -= m_invMassA * Py;
      wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px);

      vB.x += m_invMassB * Px;
      vB.y += m_invMassB * Py;
      wB += m_invIB * (m_rB.x * Py - m_rB.y * Px);
    } else {
      m_impulse = 0.0f;
    }

    pool.pushRot(2);
    pool.pushVec2(1);

    // data.velocities[m_indexA].v = vA;
    data.velocities[m_indexA].w = wA;
    // data.velocities[m_indexB].v = vB;
    data.velocities[m_indexB].w = wB;
  }

  @Override
  public void solveVelocityConstraints(final SolverData data) {
    Vec2 vA = data.velocities[m_indexA].v;
    double wA = data.velocities[m_indexA].w;
    Vec2 vB = data.velocities[m_indexB].v;
    double wB = data.velocities[m_indexB].w;

    // Cdot = dot(u, v + cross(w, r))
    Vec2 vpA = pool.popVec2();
    Vec2 vpB = pool.popVec2();
    Vec2 temp = pool.popVec2();

    Vec2.crossToOutUnsafe(wA, m_rA, vpA);
    vpA.addLocal(vA);
    Vec2.crossToOutUnsafe(wB, m_rB, vpB);
    vpB.addLocal(vB);

    double C = m_length - m_maxLength;
    double Cdot = Vec2.dot(m_u, temp.set(vpB).subLocal(vpA));

    // Predictive constraint.
    if (C < 0.0f) {
      Cdot += data.step.inv_dt * C;
    }

    double impulse = -m_mass * Cdot;
    double oldImpulse = m_impulse;
    m_impulse = Math.min(0.0f, m_impulse + impulse);
    impulse = m_impulse - oldImpulse;

    double Px = impulse * m_u.x;
    double Py = impulse * m_u.y;
    vA.x -= m_invMassA * Px;
    vA.y -= m_invMassA * Py;
    wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px);
    vB.x += m_invMassB * Px;
    vB.y += m_invMassB * Py;
    wB += m_invIB * (m_rB.x * Py - m_rB.y * Px);

    pool.pushVec2(3);

    // data.velocities[m_indexA].v = vA;
    data.velocities[m_indexA].w = wA;
    // data.velocities[m_indexB].v = vB;
    data.velocities[m_indexB].w = wB;
  }

  @Override
  public boolean solvePositionConstraints(final SolverData data) {
    Vec2 cA = data.positions[m_indexA].c;
    double aA = data.positions[m_indexA].a;
    Vec2 cB = data.positions[m_indexB].c;
    double aB = data.positions[m_indexB].a;

    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();
    final Vec2 u = pool.popVec2();
    final Vec2 rA = pool.popVec2();
    final Vec2 rB = pool.popVec2();
    final Vec2 temp = pool.popVec2();

    qA.set(aA);
    qB.set(aB);

    // Compute the effective masses.
    Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subLocal(m_localCenterA), rA);
    Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subLocal(m_localCenterB), rB);
    u.set(cB).addLocal(rB).subLocal(cA).subLocal(rA);

    double length = u.normalize();
    double C = length - m_maxLength;

    C = MathUtils.clamp(C, 0.0f, Settings.maxLinearCorrection);

    double impulse = -m_mass * C;
    double Px = impulse * u.x;
    double Py = impulse * u.y;

    cA.x -= m_invMassA * Px;
    cA.y -= m_invMassA * Py;
    aA -= m_invIA * (rA.x * Py - rA.y * Px);
    cB.x += m_invMassB * Px;
    cB.y += m_invMassB * Py;
    aB += m_invIB * (rB.x * Py - rB.y * Px);

    pool.pushRot(2);
    pool.pushVec2(4);

    // data.positions[m_indexA].c = cA;
    data.positions[m_indexA].a = aA;
    // data.positions[m_indexB].c = cB;
    data.positions[m_indexB].a = aB;

    return length - m_maxLength < Settings.linearSlop;
  }

  @Override
  public void getAnchorA(Vec2 argOut) {
    m_bodyA.getWorldPointToOut(m_localAnchorA, argOut);
  }

  @Override
  public void getAnchorB(Vec2 argOut) {
    m_bodyB.getWorldPointToOut(m_localAnchorB, argOut);
  }

  @Override
  public void getReactionForce(double inv_dt, Vec2 argOut) {
    argOut.set(m_u).mulLocal(inv_dt).mulLocal(m_impulse);
  }

  @Override
  public double getReactionTorque(double inv_dt) {
    return 0f;
  }

  public Vec2 getLocalAnchorA() {
    return m_localAnchorA;
  }

  public Vec2 getLocalAnchorB() {
    return m_localAnchorB;
  }

  public double getMaxLength() {
    return m_maxLength;
  }

  public void setMaxLength(double maxLength) {
    this.m_maxLength = maxLength;
  }

  public LimitState getLimitState() {
    return m_state;
  }

}
