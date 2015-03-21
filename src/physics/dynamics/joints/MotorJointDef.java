package physics.dynamics.joints;

import physics.common.Vec2;
import physics.dynamics.Body;

/**
 * Motor joint definition.
 * 
 * @author dmurph
 */
public class MotorJointDef extends JointDef {
  /**
   * Position of bodyB minus the position of bodyA, in bodyA's frame, in meters.
   */
  public final Vec2 linearOffset = new Vec2();

  /**
   * The bodyB angle minus bodyA angle in radians.
   */
  public double angularOffset;

  /**
   * The maximum motor force in N.
   */
  public double maxForce;

  /**
   * The maximum motor torque in N-m.
   */
  public double maxTorque;

  /**
   * Position correction factor in the range [0,1].
   */
  public double correctionFactor;

  public MotorJointDef() {
    super(JointType.MOTOR);
    angularOffset = 0;
    maxForce = 1;
    maxTorque = 1;
    correctionFactor = 0.3f;
  }

  public void initialize(Body bA, Body bB) {
    bodyA = bA;
    bodyB = bB;
    Vec2 xB = bodyB.getPosition();
    bodyA.getLocalPointToOut(xB, linearOffset);

    double angleA = bodyA.getAngle();
    double angleB = bodyB.getAngle();
    angularOffset = angleB - angleA;
  }
}
