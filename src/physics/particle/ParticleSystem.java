package physics.particle;

import java.lang.reflect.Array;
import java.util.Arrays;

import physics.callbacks.ParticleDestructionListener;
import physics.callbacks.ParticleQueryCallback;
import physics.callbacks.ParticleRaycastCallback;
import physics.callbacks.QueryCallback;
import physics.collision.AABB;
import physics.collision.RayCastInput;
import physics.collision.RayCastOutput;
import physics.collision.shapes.Shape;
import physics.common.BufferUtils;
import physics.common.Rot;
import physics.common.Settings;
import physics.common.Transform;
import physics.common.Vec2;
import physics.dynamics.Body;
import physics.dynamics.Fixture;
import physics.dynamics.TimeStep;
import physics.dynamics.World;
import physics.particle.VoronoiDiagram.VoronoiDiagramCallback;

public class ParticleSystem {
  /** All particle types that require creating pairs */
  private static final int k_pairFlags = ParticleType.b2_springParticle;
  /** All particle types that require creating triads */
  private static final int k_triadFlags = ParticleType.b2_elasticParticle;
  /** All particle types that require computing depth */
  private static final int k_noPressureFlags = ParticleType.b2_powderParticle;

  static final int xTruncBits = 12;
  static final int yTruncBits = 12;
  static final int tagBits = 8 * 4 - 1  /* sizeof(int) */;
  static final long yOffset = 1 << (yTruncBits - 1);
  static final int yShift = tagBits - yTruncBits;
  static final int xShift = tagBits - yTruncBits - xTruncBits;
  static final long xScale = 1 << xShift;
  static final long xOffset = xScale * (1 << (xTruncBits - 1));
  static final int xMask = (1 << xTruncBits) - 1;
  static final int yMask = (1 << yTruncBits) - 1;

  static long computeTag(double x, double y) {
    return (((long) (y + yOffset)) << yShift) + (((long) (xScale * x)) + xOffset);
  }

  static long computeRelativeTag(long tag, int x, int y) {
    return tag + (y << yShift) + (x << xShift);
  }

  static int limitCapacity(int capacity, int maxCount) {
    return maxCount != 0 && capacity > maxCount ? maxCount : capacity;
  }

  int m_timestamp;
  int m_allParticleFlags;
  int m_allGroupFlags;
  double m_density;
  double m_inverseDensity;
  double m_gravityScale;
  double m_particleDiameter;
  double m_inverseDiameter;
  double m_squaredDiameter;

  int m_count;
  int m_internalAllocatedCapacity;
  int m_maxCount;
  ParticleBufferInt m_flagsBuffer;
  ParticleBuffer<Vec2> m_positionBuffer;
  ParticleBuffer<Vec2> m_velocityBuffer;
  double[] m_accumulationBuffer; // temporary values
  Vec2[] m_accumulation2Buffer; // temporary vector values
  double[] m_depthBuffer; // distance from the surface

  public ParticleBuffer<ParticleColor> m_colorBuffer;
  ParticleGroup[] m_groupBuffer;
  ParticleBuffer<Object> m_userDataBuffer;

  int m_proxyCount;
  int m_proxyCapacity;
  Proxy[] m_proxyBuffer;

  public int m_contactCount;
  int m_contactCapacity;
  public ParticleContact[] m_contactBuffer;

  public int m_bodyContactCount;
  int m_bodyContactCapacity;
  public ParticleBodyContact[] m_bodyContactBuffer;

  int m_pairCount;
  int m_pairCapacity;
  Pair[] m_pairBuffer;

  int m_triadCount;
  int m_triadCapacity;
  Triad[] m_triadBuffer;

  int m_groupCount;
  ParticleGroup m_groupList;

  double m_pressureStrength;
  double m_dampingStrength;
  double m_elasticStrength;
  double m_springStrength;
  double m_viscousStrength;
  double m_surfaceTensionStrengthA;
  double m_surfaceTensionStrengthB;
  double m_powderStrength;
  double m_ejectionStrength;
  double m_colorMixingStrength;

  World m_world;

  public ParticleSystem(World world) {
    m_world = world;
    m_timestamp = 0;
    m_allParticleFlags = 0;
    m_allGroupFlags = 0;
    m_density = 1;
    m_inverseDensity = 1;
    m_gravityScale = 1;
    m_particleDiameter = 1;
    m_inverseDiameter = 1;
    m_squaredDiameter = 1;

    m_count = 0;
    m_internalAllocatedCapacity = 0;
    m_maxCount = 0;

    m_proxyCount = 0;
    m_proxyCapacity = 0;

    m_contactCount = 0;
    m_contactCapacity = 0;

    m_bodyContactCount = 0;
    m_bodyContactCapacity = 0;

    m_pairCount = 0;
    m_pairCapacity = 0;

    m_triadCount = 0;
    m_triadCapacity = 0;

    m_groupCount = 0;

    m_pressureStrength = 0.05f;
    m_dampingStrength = 1.0f;
    m_elasticStrength = 0.25f;
    m_springStrength = 0.25f;
    m_viscousStrength = 0.25f;
    m_surfaceTensionStrengthA = 0.1f;
    m_surfaceTensionStrengthB = 0.2f;
    m_powderStrength = 0.5f;
    m_ejectionStrength = 0.5f;
    m_colorMixingStrength = 0.5f;

    m_flagsBuffer = new ParticleBufferInt();
    m_positionBuffer = new ParticleBuffer<Vec2>(Vec2.class);
    m_velocityBuffer = new ParticleBuffer<Vec2>(Vec2.class);
    m_colorBuffer = new ParticleBuffer<ParticleColor>(ParticleColor.class);
    m_userDataBuffer = new ParticleBuffer<Object>(Object.class);
  }
  
//  public void assertNotSamePosition() {
//    for (int i = 0; i < m_count; i++) {
//      Vec2 vi = m_positionBuffer.data[i];
//      for (int j = i + 1; j < m_count; j++) {
//        Vec2 vj = m_positionBuffer.data[j];
//        assert(vi.x != vj.x || vi.y != vj.y);
//      }
//    }
//  }

  public int createParticle(ParticleDef def) {
    if (m_count >= m_internalAllocatedCapacity) {
      int capacity = m_count != 0 ? 2 * m_count : Settings.minParticleBufferCapacity;
      capacity = limitCapacity(capacity, m_maxCount);
      capacity = limitCapacity(capacity, m_flagsBuffer.userSuppliedCapacity);
      capacity = limitCapacity(capacity, m_positionBuffer.userSuppliedCapacity);
      capacity = limitCapacity(capacity, m_velocityBuffer.userSuppliedCapacity);
      capacity = limitCapacity(capacity, m_colorBuffer.userSuppliedCapacity);
      capacity = limitCapacity(capacity, m_userDataBuffer.userSuppliedCapacity);
      if (m_internalAllocatedCapacity < capacity) {
        m_flagsBuffer.data =
            reallocateBuffer(m_flagsBuffer, m_internalAllocatedCapacity, capacity, false);
        m_positionBuffer.data =
            reallocateBuffer(m_positionBuffer, m_internalAllocatedCapacity, capacity, false);
        m_velocityBuffer.data =
            reallocateBuffer(m_velocityBuffer, m_internalAllocatedCapacity, capacity, false);
        m_accumulationBuffer =
            BufferUtils.reallocateBuffer(m_accumulationBuffer, 0, m_internalAllocatedCapacity,
                capacity, false);
        m_accumulation2Buffer =
            BufferUtils.reallocateBuffer(Vec2.class, m_accumulation2Buffer, 0,
                m_internalAllocatedCapacity, capacity, true);
        m_depthBuffer =
            BufferUtils.reallocateBuffer(m_depthBuffer, 0, m_internalAllocatedCapacity, capacity,
                true);
        m_colorBuffer.data =
            reallocateBuffer(m_colorBuffer, m_internalAllocatedCapacity, capacity, true);
        m_groupBuffer =
            BufferUtils.reallocateBuffer(ParticleGroup.class, m_groupBuffer, 0,
                m_internalAllocatedCapacity, capacity, false);
        m_userDataBuffer.data =
            reallocateBuffer(m_userDataBuffer, m_internalAllocatedCapacity, capacity, true);
        m_internalAllocatedCapacity = capacity;
      }
    }
    if (m_count >= m_internalAllocatedCapacity) {
      return Settings.invalidParticleIndex;
    }
    int index = m_count++;
    m_flagsBuffer.data[index] = def.flags;
    m_positionBuffer.data[index].set(def.position);
//    assertNotSamePosition();
    m_velocityBuffer.data[index].set(def.velocity);
    m_groupBuffer[index] = null;
    if (m_depthBuffer != null) {
      m_depthBuffer[index] = 0;
    }
    if (m_colorBuffer.data != null || def.color != null) {
      m_colorBuffer.data = requestParticleBuffer(m_colorBuffer.dataClass, m_colorBuffer.data);
      m_colorBuffer.data[index].set(def.color);
    }
    if (m_userDataBuffer.data != null || def.userData != null) {
      m_userDataBuffer.data =
          requestParticleBuffer(m_userDataBuffer.dataClass, m_userDataBuffer.data);
      m_userDataBuffer.data[index] = def.userData;
    }
    if (m_proxyCount >= m_proxyCapacity) {
      int oldCapacity = m_proxyCapacity;
      int newCapacity = m_proxyCount != 0 ? 2 * m_proxyCount : Settings.minParticleBufferCapacity;
      m_proxyBuffer =
          BufferUtils.reallocateBuffer(Proxy.class, m_proxyBuffer, oldCapacity, newCapacity);
      m_proxyCapacity = newCapacity;
    }
    m_proxyBuffer[m_proxyCount++].index = index;
    return index;
  }

  public void destroyParticle(int index, boolean callDestructionListener) {
    int flags = ParticleType.b2_zombieParticle;
    if (callDestructionListener) {
      flags |= ParticleType.b2_destructionListener;
    }
    m_flagsBuffer.data[index] |= flags;
  }

  private final AABB temp = new AABB();
  private final DestroyParticlesInShapeCallback dpcallback = new DestroyParticlesInShapeCallback();

  public int destroyParticlesInShape(Shape shape, Transform xf, boolean callDestructionListener) {
    dpcallback.init(this, shape, xf, callDestructionListener);
    shape.computeAABB(temp, xf, 0);
    m_world.queryAABB(dpcallback, temp);
    return dpcallback.destroyed;
  }

  public void destroyParticlesInGroup(ParticleGroup group, boolean callDestructionListener) {
    for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
      destroyParticle(i, callDestructionListener);
    }
  }

  private final AABB temp2 = new AABB();
  private final Vec2 tempVec = new Vec2();
  private final Transform tempTransform = new Transform();
  private final Transform tempTransform2 = new Transform();
  private CreateParticleGroupCallback createParticleGroupCallback =
      new CreateParticleGroupCallback();
  private final ParticleDef tempParticleDef = new ParticleDef();

  public ParticleGroup createParticleGroup(ParticleGroupDef groupDef) {
    double stride = getParticleStride();
    final Transform identity = tempTransform;
    identity.setIdentity();
    Transform transform = tempTransform2;
    transform.setIdentity();
    int firstIndex = m_count;
    if (groupDef.shape != null) {
      final ParticleDef particleDef = tempParticleDef;
      particleDef.flags = groupDef.flags;
      particleDef.color = groupDef.color;
      particleDef.userData = groupDef.userData;
      Shape shape = groupDef.shape;
      transform.set(groupDef.position, groupDef.angle);
      AABB aabb = temp;
      int childCount = shape.getChildCount();
      for (int childIndex = 0; childIndex < childCount; childIndex++) {
        if (childIndex == 0) {
          shape.computeAABB(aabb, identity, childIndex);
        } else {
          AABB childAABB = temp2;
          shape.computeAABB(childAABB, identity, childIndex);
          aabb.combine(childAABB);
        }
      }
      final double upperBoundY = aabb.upperBound.y;
      final double upperBoundX = aabb.upperBound.x;
      for (double y = Math.floor(aabb.lowerBound.y / stride) * stride; y < upperBoundY; y +=
          stride) {
        for (double x = Math.floor(aabb.lowerBound.x / stride) * stride; x < upperBoundX; x +=
            stride) {
          Vec2 p = tempVec;
          p.x = x;
          p.y = y;
          if (shape.testPoint(identity, p)) {
            Transform.mulToOut(transform, p, p);
            particleDef.position.x = p.x;
            particleDef.position.y = p.y;
            p.subLocal(groupDef.position);
            Vec2.crossToOutUnsafe(groupDef.angularVelocity, p, particleDef.velocity);
            particleDef.velocity.addLocal(groupDef.linearVelocity);
            createParticle(particleDef);
          }
        }
      }
    }
    int lastIndex = m_count;

    ParticleGroup group = new ParticleGroup();
    group.m_system = this;
    group.m_firstIndex = firstIndex;
    group.m_lastIndex = lastIndex;
    group.m_groupFlags = groupDef.groupFlags;
    group.m_strength = groupDef.strength;
    group.m_userData = groupDef.userData;
    group.m_transform.set(transform);
    group.m_destroyAutomatically = groupDef.destroyAutomatically;
    group.m_prev = null;
    group.m_next = m_groupList;
    if (m_groupList != null) {
      m_groupList.m_prev = group;
    }
    m_groupList = group;
    ++m_groupCount;
    for (int i = firstIndex; i < lastIndex; i++) {
      m_groupBuffer[i] = group;
    }

    updateContacts(true);
    if ((groupDef.flags & k_pairFlags) != 0) {
      for (int k = 0; k < m_contactCount; k++) {
        ParticleContact contact = m_contactBuffer[k];
        int a = contact.indexA;
        int b = contact.indexB;
        if (a > b) {
          int temp = a;
          a = b;
          b = temp;
        }
        if (firstIndex <= a && b < lastIndex) {
          if (m_pairCount >= m_pairCapacity) {
            int oldCapacity = m_pairCapacity;
            int newCapacity =
                m_pairCount != 0 ? 2 * m_pairCount : Settings.minParticleBufferCapacity;
            m_pairBuffer =
                BufferUtils.reallocateBuffer(Pair.class, m_pairBuffer, oldCapacity, newCapacity);
            m_pairCapacity = newCapacity;
          }
          Pair pair = m_pairBuffer[m_pairCount];
          pair.indexA = a;
          pair.indexB = b;
          pair.flags = contact.flags;
          pair.strength = groupDef.strength;
          pair.distance = Vec2.distance(m_positionBuffer.data[a], m_positionBuffer.data[b]);
          m_pairCount++;
        }
      }
    }
    if ((groupDef.flags & k_triadFlags) != 0) {
      VoronoiDiagram diagram = new VoronoiDiagram(lastIndex - firstIndex);
      for (int i = firstIndex; i < lastIndex; i++) {
        diagram.addGenerator(m_positionBuffer.data[i], i);
      }
      diagram.generate(stride / 2);
      createParticleGroupCallback.system = this;
      createParticleGroupCallback.def = groupDef;
      createParticleGroupCallback.firstIndex = firstIndex;
      diagram.getNodes(createParticleGroupCallback);
    }
    if ((groupDef.groupFlags & ParticleGroupType.b2_solidParticleGroup) != 0) {
      computeDepthForGroup(group);
    }

    return group;
  }

  public void joinParticleGroups(ParticleGroup groupA, ParticleGroup groupB) {
    assert (groupA != groupB);
    RotateBuffer(groupB.m_firstIndex, groupB.m_lastIndex, m_count);
    assert (groupB.m_lastIndex == m_count);
    RotateBuffer(groupA.m_firstIndex, groupA.m_lastIndex, groupB.m_firstIndex);
    assert (groupA.m_lastIndex == groupB.m_firstIndex);

    int particleFlags = 0;
    for (int i = groupA.m_firstIndex; i < groupB.m_lastIndex; i++) {
      particleFlags |= m_flagsBuffer.data[i];
    }

    updateContacts(true);
    if ((particleFlags & k_pairFlags) != 0) {
      for (int k = 0; k < m_contactCount; k++) {
        final ParticleContact contact = m_contactBuffer[k];
        int a = contact.indexA;
        int b = contact.indexB;
        if (a > b) {
          int temp = a;
          a = b;
          b = temp;
        }
        if (groupA.m_firstIndex <= a && a < groupA.m_lastIndex && groupB.m_firstIndex <= b
            && b < groupB.m_lastIndex) {
          if (m_pairCount >= m_pairCapacity) {
            int oldCapacity = m_pairCapacity;
            int newCapacity =
                m_pairCount != 0 ? 2 * m_pairCount : Settings.minParticleBufferCapacity;
            m_pairBuffer =
                BufferUtils.reallocateBuffer(Pair.class, m_pairBuffer, oldCapacity, newCapacity);
            m_pairCapacity = newCapacity;
          }
          Pair pair = m_pairBuffer[m_pairCount];
          pair.indexA = a;
          pair.indexB = b;
          pair.flags = contact.flags;
          pair.strength = Math.min(groupA.m_strength, groupB.m_strength);
          pair.distance = Vec2.distance(m_positionBuffer.data[a], m_positionBuffer.data[b]);
          m_pairCount++;
        }
      }
    }
    if ((particleFlags & k_triadFlags) != 0) {
      VoronoiDiagram diagram = new VoronoiDiagram(groupB.m_lastIndex - groupA.m_firstIndex);
      for (int i = groupA.m_firstIndex; i < groupB.m_lastIndex; i++) {
        if ((m_flagsBuffer.data[i] & ParticleType.b2_zombieParticle) == 0) {
          diagram.addGenerator(m_positionBuffer.data[i], i);
        }
      }
      diagram.generate(getParticleStride() / 2);
      JoinParticleGroupsCallback callback = new JoinParticleGroupsCallback();
      callback.system = this;
      callback.groupA = groupA;
      callback.groupB = groupB;
      diagram.getNodes(callback);
    }

    for (int i = groupB.m_firstIndex; i < groupB.m_lastIndex; i++) {
      m_groupBuffer[i] = groupA;
    }
    int groupFlags = groupA.m_groupFlags | groupB.m_groupFlags;
    groupA.m_groupFlags = groupFlags;
    groupA.m_lastIndex = groupB.m_lastIndex;
    groupB.m_firstIndex = groupB.m_lastIndex;
    destroyParticleGroup(groupB);

    if ((groupFlags & ParticleGroupType.b2_solidParticleGroup) != 0) {
      computeDepthForGroup(groupA);
    }
  }

  // Only called from solveZombie() or joinParticleGroups().
  void destroyParticleGroup(ParticleGroup group) {
    assert (m_groupCount > 0);
    assert (group != null);

    if (m_world.getParticleDestructionListener() != null) {
      m_world.getParticleDestructionListener().sayGoodbye(group);
    }

    for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
      m_groupBuffer[i] = null;
    }

    if (group.m_prev != null) {
      group.m_prev.m_next = group.m_next;
    }
    if (group.m_next != null) {
      group.m_next.m_prev = group.m_prev;
    }
    if (group == m_groupList) {
      m_groupList = group.m_next;
    }

    --m_groupCount;
  }

  public void computeDepthForGroup(ParticleGroup group) {
    for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
      m_accumulationBuffer[i] = 0;
    }
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      if (a >= group.m_firstIndex && a < group.m_lastIndex && b >= group.m_firstIndex
          && b < group.m_lastIndex) {
        double w = contact.weight;
        m_accumulationBuffer[a] += w;
        m_accumulationBuffer[b] += w;
      }
    }
    m_depthBuffer = requestParticleBuffer(m_depthBuffer);
    for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
      double w = m_accumulationBuffer[i];
      m_depthBuffer[i] = w < 0.8f ? 0 : Float.MAX_VALUE;
    }
    int interationCount = group.getParticleCount();
    for (int t = 0; t < interationCount; t++) {
      boolean updated = false;
      for (int k = 0; k < m_contactCount; k++) {
        final ParticleContact contact = m_contactBuffer[k];
        int a = contact.indexA;
        int b = contact.indexB;
        if (a >= group.m_firstIndex && a < group.m_lastIndex && b >= group.m_firstIndex
            && b < group.m_lastIndex) {
          double r = 1 - contact.weight;
          double ap0 = m_depthBuffer[a];
          double bp0 = m_depthBuffer[b];
          double ap1 = bp0 + r;
          double bp1 = ap0 + r;
          if (ap0 > ap1) {
            m_depthBuffer[a] = ap1;
            updated = true;
          }
          if (bp0 > bp1) {
            m_depthBuffer[b] = bp1;
            updated = true;
          }
        }
      }
      if (!updated) {
        break;
      }
    }
    for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
      double p = m_depthBuffer[i];
      if (p < Float.MAX_VALUE) {
        m_depthBuffer[i] *= m_particleDiameter;
      } else {
        m_depthBuffer[i] = 0;
      }
    }
  }

  public void addContact(int a, int b) {
    assert(a != b);
    Vec2 pa = m_positionBuffer.data[a];
    Vec2 pb = m_positionBuffer.data[b];
    double dx = pb.x - pa.x;
    double dy = pb.y - pa.y;
    double d2 = dx * dx + dy * dy;
//    assert(d2 != 0);
    if (d2 < m_squaredDiameter) {
      if (m_contactCount >= m_contactCapacity) {
        int oldCapacity = m_contactCapacity;
        int newCapacity =
            m_contactCount != 0 ? 2 * m_contactCount : Settings.minParticleBufferCapacity;
        m_contactBuffer =
            BufferUtils.reallocateBuffer(ParticleContact.class, m_contactBuffer, oldCapacity,
                newCapacity);
        m_contactCapacity = newCapacity;
      }
      double invD = d2 != 0 ? Math.sqrt(1 / d2) : Float.MAX_VALUE;
      ParticleContact contact = m_contactBuffer[m_contactCount];
      contact.indexA = a;
      contact.indexB = b;
      contact.flags = m_flagsBuffer.data[a] | m_flagsBuffer.data[b];
      contact.weight = 1 - d2 * invD * m_inverseDiameter;
      contact.normal.x = invD * dx;
      contact.normal.y = invD * dy;
      m_contactCount++;
    }
  }

  public void updateContacts(boolean exceptZombie) {
    for (int p = 0; p < m_proxyCount; p++) {
      Proxy proxy = m_proxyBuffer[p];
      int i = proxy.index;
      Vec2 pos = m_positionBuffer.data[i];
      proxy.tag = computeTag(m_inverseDiameter * pos.x, m_inverseDiameter * pos.y);
    }
    Arrays.sort(m_proxyBuffer, 0, m_proxyCount);
    m_contactCount = 0;
    int c_index = 0;
    for (int i = 0; i < m_proxyCount; i++) {
      Proxy a = m_proxyBuffer[i];
      long rightTag = computeRelativeTag(a.tag, 1, 0);
      for (int j = i + 1; j < m_proxyCount; j++) {
        Proxy b = m_proxyBuffer[j];
        if (rightTag < b.tag) {
          break;
        }
        addContact(a.index, b.index);
      }
      long bottomLeftTag = computeRelativeTag(a.tag, -1, 1);
      for (; c_index < m_proxyCount; c_index++) {
        Proxy c = m_proxyBuffer[c_index];
        if (bottomLeftTag <= c.tag) {
          break;
        }
      }
      long bottomRightTag = computeRelativeTag(a.tag, 1, 1);

      for (int b_index = c_index; b_index < m_proxyCount; b_index++) {
        Proxy b = m_proxyBuffer[b_index];
        if (bottomRightTag < b.tag) {
          break;
        }
        addContact(a.index, b.index);
      }
    }
    if (exceptZombie) {
      int j = m_contactCount;
      for (int i = 0; i < j; i++) {
        if ((m_contactBuffer[i].flags & ParticleType.b2_zombieParticle) != 0) {
          --j;
          ParticleContact temp = m_contactBuffer[j];
          m_contactBuffer[j] = m_contactBuffer[i];
          m_contactBuffer[i] = temp;
          --i;
        }
      }
      m_contactCount = j;
    }
  }

  private final UpdateBodyContactsCallback ubccallback = new UpdateBodyContactsCallback();

  public void updateBodyContacts() {
    final AABB aabb = temp;
    aabb.lowerBound.x = Float.MAX_VALUE;
    aabb.lowerBound.y = Float.MAX_VALUE;
    aabb.upperBound.x = -Float.MAX_VALUE;
    aabb.upperBound.y = -Float.MAX_VALUE;
    for (int i = 0; i < m_count; i++) {
      Vec2 p = m_positionBuffer.data[i];
      Vec2.minToOut(aabb.lowerBound, p, aabb.lowerBound);
      Vec2.maxToOut(aabb.upperBound, p, aabb.upperBound);
    }
    aabb.lowerBound.x -= m_particleDiameter;
    aabb.lowerBound.y -= m_particleDiameter;
    aabb.upperBound.x += m_particleDiameter;
    aabb.upperBound.y += m_particleDiameter;
    m_bodyContactCount = 0;

    ubccallback.system = this;
    m_world.queryAABB(ubccallback, aabb);
  }

  private SolveCollisionCallback sccallback = new SolveCollisionCallback();

  public void solveCollision(TimeStep step) {
    final AABB aabb = temp;
    final Vec2 lowerBound = aabb.lowerBound;
    final Vec2 upperBound = aabb.upperBound;
    lowerBound.x = Float.MAX_VALUE;
    lowerBound.y = Float.MAX_VALUE;
    upperBound.x = -Float.MAX_VALUE;
    upperBound.y = -Float.MAX_VALUE;
    for (int i = 0; i < m_count; i++) {
      final Vec2 v = m_velocityBuffer.data[i];
      final Vec2 p1 = m_positionBuffer.data[i];
      final double p1x = p1.x;
      final double p1y = p1.y;
      final double p2x = p1x + step.dt * v.x;
      final double p2y = p1y + step.dt * v.y;
      final double bx = p1x < p2x ? p1x : p2x;
      final double by = p1y < p2y ? p1y : p2y;
      lowerBound.x = lowerBound.x < bx ? lowerBound.x : bx;
      lowerBound.y = lowerBound.y < by ? lowerBound.y : by;
      final double b1x = p1x > p2x ? p1x : p2x;
      final double b1y = p1y > p2y ? p1y : p2y;
      upperBound.x = upperBound.x > b1x ? upperBound.x : b1x;
      upperBound.y = upperBound.y > b1y ? upperBound.y : b1y;
    }
    sccallback.step = step;
    sccallback.system = this;
    m_world.queryAABB(sccallback, aabb);
  }

  public void solve(TimeStep step) {
    ++m_timestamp;
    if (m_count == 0) {
      return;
    }
    m_allParticleFlags = 0;
    for (int i = 0; i < m_count; i++) {
      m_allParticleFlags |= m_flagsBuffer.data[i];
    }
    if ((m_allParticleFlags & ParticleType.b2_zombieParticle) != 0) {
      solveZombie();
    }
    if (m_count == 0) {
      return;
    }
    m_allGroupFlags = 0;
    for (ParticleGroup group = m_groupList; group != null; group = group.getNext()) {
      m_allGroupFlags |= group.m_groupFlags;
    }
    final double gravityx = step.dt * m_gravityScale * m_world.getGravity().x;
    final double gravityy = step.dt * m_gravityScale * m_world.getGravity().y;
    double criticalVelocytySquared = getCriticalVelocitySquared(step);
    for (int i = 0; i < m_count; i++) {
      Vec2 v = m_velocityBuffer.data[i];
      v.x += gravityx;
      v.y += gravityy;
      double v2 = v.x * v.x + v.y * v.y;
      if (v2 > criticalVelocytySquared) {
        double a = v2 == 0 ? Float.MAX_VALUE : Math.sqrt(criticalVelocytySquared / v2);
        v.x *= a;
        v.y *= a;
      }
    }
    solveCollision(step);
    if ((m_allGroupFlags & ParticleGroupType.b2_rigidParticleGroup) != 0) {
      solveRigid(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_wallParticle) != 0) {
      solveWall(step);
    }
    for (int i = 0; i < m_count; i++) {
      Vec2 pos = m_positionBuffer.data[i];
      Vec2 vel = m_velocityBuffer.data[i];
      pos.x += step.dt * vel.x;
      pos.y += step.dt * vel.y;
    }
    updateBodyContacts();
    updateContacts(false);
    if ((m_allParticleFlags & ParticleType.b2_viscousParticle) != 0) {
      solveViscous(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_powderParticle) != 0) {
      solvePowder(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_tensileParticle) != 0) {
      solveTensile(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_elasticParticle) != 0) {
      solveElastic(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_springParticle) != 0) {
      solveSpring(step);
    }
    if ((m_allGroupFlags & ParticleGroupType.b2_solidParticleGroup) != 0) {
      solveSolid(step);
    }
    if ((m_allParticleFlags & ParticleType.b2_colorMixingParticle) != 0) {
      solveColorMixing(step);
    }
    solvePressure(step);
    solveDamping(step);
  }

  void solvePressure(TimeStep step) {
    // calculates the sum of contact-weights for each particle
    // that means dimensionless density
    for (int i = 0; i < m_count; i++) {
      m_accumulationBuffer[i] = 0;
    }
    for (int k = 0; k < m_bodyContactCount; k++) {
      ParticleBodyContact contact = m_bodyContactBuffer[k];
      int a = contact.index;
      double w = contact.weight;
      m_accumulationBuffer[a] += w;
    }
    for (int k = 0; k < m_contactCount; k++) {
      ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      double w = contact.weight;
      m_accumulationBuffer[a] += w;
      m_accumulationBuffer[b] += w;
    }
    // ignores powder particles
    if ((m_allParticleFlags & k_noPressureFlags) != 0) {
      for (int i = 0; i < m_count; i++) {
        if ((m_flagsBuffer.data[i] & k_noPressureFlags) != 0) {
          m_accumulationBuffer[i] = 0;
        }
      }
    }
    // calculates pressure as a linear function of density
    double pressurePerWeight = m_pressureStrength * getCriticalPressure(step);
    for (int i = 0; i < m_count; i++) {
      double w = m_accumulationBuffer[i];
      double h =
          pressurePerWeight
              * Math.max(0.0f, Math.min(w, Settings.maxParticleWeight)
                  - Settings.minParticleWeight);
      m_accumulationBuffer[i] = h;
    }
    // applies pressure between each particles in contact
    double velocityPerPressure = step.dt / (m_density * m_particleDiameter);
    for (int k = 0; k < m_bodyContactCount; k++) {
      ParticleBodyContact contact = m_bodyContactBuffer[k];
      int a = contact.index;
      Body b = contact.body;
      double w = contact.weight;
      double m = contact.mass;
      Vec2 n = contact.normal;
      Vec2 p = m_positionBuffer.data[a];
      double h = m_accumulationBuffer[a] + pressurePerWeight * w;
      final Vec2 f = tempVec;
      final double coef = velocityPerPressure * w * m * h;
      f.x = coef * n.x;
      f.y = coef * n.y;
      final Vec2 velData = m_velocityBuffer.data[a];
      final double particleInvMass = getParticleInvMass();
      velData.x -= particleInvMass * f.x;
      velData.y -= particleInvMass * f.y;
      b.applyLinearImpulse(f, p, true);
    }
    for (int k = 0; k < m_contactCount; k++) {
      ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      double w = contact.weight;
      Vec2 n = contact.normal;
      double h = m_accumulationBuffer[a] + m_accumulationBuffer[b];
      final double fx = velocityPerPressure * w * h * n.x;
      final double fy = velocityPerPressure * w * h * n.y;
      final Vec2 velDataA = m_velocityBuffer.data[a];
      final Vec2 velDataB = m_velocityBuffer.data[b];
      velDataA.x -= fx;
      velDataA.y -= fy;
      velDataB.x += fx;
      velDataB.y += fy;
    }
  }

  void solveDamping(TimeStep step) {
    // reduces normal velocity of each contact
    double damping = m_dampingStrength;
    for (int k = 0; k < m_bodyContactCount; k++) {
      final ParticleBodyContact contact = m_bodyContactBuffer[k];
      int a = contact.index;
      Body b = contact.body;
      double w = contact.weight;
      double m = contact.mass;
      Vec2 n = contact.normal;
      Vec2 p = m_positionBuffer.data[a];
      final double tempX = p.x - b.m_sweep.c.x;
      final double tempY = p.y - b.m_sweep.c.y;
      final Vec2 velA = m_velocityBuffer.data[a];
      // getLinearVelocityFromWorldPointToOut, with -= velA
      double vx = -b.m_angularVelocity * tempY + b.m_linearVelocity.x - velA.x;
      double vy = b.m_angularVelocity * tempX + b.m_linearVelocity.y - velA.y;
      // done
      double vn = vx * n.x + vy * n.y;
      if (vn < 0) {
        final Vec2 f = tempVec;
        f.x = damping * w * m * vn * n.x;
        f.y = damping * w * m * vn * n.y;
        final double invMass = getParticleInvMass();
        velA.x += invMass * f.x;
        velA.y += invMass * f.y;
        f.x = -f.x;
        f.y = -f.y;
        b.applyLinearImpulse(f, p, true);
      }
    }
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      double w = contact.weight;
      Vec2 n = contact.normal;
      final Vec2 velA = m_velocityBuffer.data[a];
      final Vec2 velB = m_velocityBuffer.data[b];
      final double vx = velB.x - velA.x;
      final double vy = velB.y - velA.y;
      double vn = vx * n.x + vy * n.y;
      if (vn < 0) {
        double fx = damping * w * vn * n.x;
        double fy = damping * w * vn * n.y;
        velA.x += fx;
        velA.y += fy;
        velB.x -= fx;
        velB.y -= fy;
      }
    }
  }

  public void solveWall(TimeStep step) {
    for (int i = 0; i < m_count; i++) {
      if ((m_flagsBuffer.data[i] & ParticleType.b2_wallParticle) != 0) {
        final Vec2 r = m_velocityBuffer.data[i];
        r.x = 0.0f;
        r.y = 0.0f;
      }
    }
  }

  private final Vec2 tempVec2 = new Vec2();
  private final Rot tempRot = new Rot();
  private final Transform tempXf = new Transform();
  private final Transform tempXf2 = new Transform();

  void solveRigid(final TimeStep step) {
    for (ParticleGroup group = m_groupList; group != null; group = group.getNext()) {
      if ((group.m_groupFlags & ParticleGroupType.b2_rigidParticleGroup) != 0) {
        group.updateStatistics();
        Vec2 temp = tempVec;
        Vec2 cross = tempVec2;
        Rot rotation = tempRot;
        rotation.set(step.dt * group.m_angularVelocity);
        Rot.mulToOutUnsafe(rotation, group.m_center, cross);
        temp.set(group.m_linearVelocity).mulLocal(step.dt).addLocal(group.m_center).subLocal(cross);
        tempXf.p.set(temp);
        tempXf.q.set(rotation);
        Transform.mulToOut(tempXf, group.m_transform, group.m_transform);
        final Transform velocityTransform = tempXf2;
        velocityTransform.p.x = step.inv_dt * tempXf.p.x;
        velocityTransform.p.y = step.inv_dt * tempXf.p.y;
        velocityTransform.q.s = step.inv_dt * tempXf.q.s;
        velocityTransform.q.c = step.inv_dt * (tempXf.q.c - 1);
        for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
          Transform.mulToOutUnsafe(velocityTransform, m_positionBuffer.data[i],
              m_velocityBuffer.data[i]);
        }
      }
    }
  }

  void solveElastic(final TimeStep step) {
    double elasticStrength = step.inv_dt * m_elasticStrength;
    for (int k = 0; k < m_triadCount; k++) {
      final Triad triad = m_triadBuffer[k];
      if ((triad.flags & ParticleType.b2_elasticParticle) != 0) {
        int a = triad.indexA;
        int b = triad.indexB;
        int c = triad.indexC;
        final Vec2 oa = triad.pa;
        final Vec2 ob = triad.pb;
        final Vec2 oc = triad.pc;
        final Vec2 pa = m_positionBuffer.data[a];
        final Vec2 pb = m_positionBuffer.data[b];
        final Vec2 pc = m_positionBuffer.data[c];
        final double px = 1f / 3 * (pa.x + pb.x + pc.x);
        final double py = 1f / 3 * (pa.y + pb.y + pc.y);
        double rs = Vec2.cross(oa, pa) + Vec2.cross(ob, pb) + Vec2.cross(oc, pc);
        double rc = Vec2.dot(oa, pa) + Vec2.dot(ob, pb) + Vec2.dot(oc, pc);
        double r2 = rs * rs + rc * rc;
        double invR = r2 == 0 ? Float.MAX_VALUE : Math.sqrt(1f / r2);
        rs *= invR;
        rc *= invR;
        final double strength = elasticStrength * triad.strength;
        final double roax = rc * oa.x - rs * oa.y;
        final double roay = rs * oa.x + rc * oa.y;
        final double robx = rc * ob.x - rs * ob.y;
        final double roby = rs * ob.x + rc * ob.y;
        final double rocx = rc * oc.x - rs * oc.y;
        final double rocy = rs * oc.x + rc * oc.y;
        final Vec2 va = m_velocityBuffer.data[a];
        final Vec2 vb = m_velocityBuffer.data[b];
        final Vec2 vc = m_velocityBuffer.data[c];
        va.x += strength * (roax - (pa.x - px));
        va.y += strength * (roay - (pa.y - py));
        vb.x += strength * (robx - (pb.x - px));
        vb.y += strength * (roby - (pb.y - py));
        vc.x += strength * (rocx - (pc.x - px));
        vc.y += strength * (rocy - (pc.y - py));
      }
    }
  }

  void solveSpring(final TimeStep step) {
    double springStrength = step.inv_dt * m_springStrength;
    for (int k = 0; k < m_pairCount; k++) {
      final Pair pair = m_pairBuffer[k];
      if ((pair.flags & ParticleType.b2_springParticle) != 0) {
        int a = pair.indexA;
        int b = pair.indexB;
        final Vec2 pa = m_positionBuffer.data[a];
        final Vec2 pb = m_positionBuffer.data[b];
        final double dx = pb.x - pa.x;
        final double dy = pb.y - pa.y;
        double r0 = pair.distance;
        double r1 = Math.sqrt(dx * dx + dy * dy);
        if (r1 == 0) r1 = Float.MAX_VALUE;
        double strength = springStrength * pair.strength;
        final double fx = strength * (r0 - r1) / r1 * dx;
        final double fy = strength * (r0 - r1) / r1 * dy;
        final Vec2 va = m_velocityBuffer.data[a];
        final Vec2 vb = m_velocityBuffer.data[b];
        va.x -= fx;
        va.y -= fy;
        vb.x += fx;
        vb.y += fy;
      }
    }
  }

  void solveTensile(final TimeStep step) {
    m_accumulation2Buffer = requestParticleBuffer(Vec2.class, m_accumulation2Buffer);
    for (int i = 0; i < m_count; i++) {
      m_accumulationBuffer[i] = 0;
      m_accumulation2Buffer[i].setZero();
    }
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      if ((contact.flags & ParticleType.b2_tensileParticle) != 0) {
        int a = contact.indexA;
        int b = contact.indexB;
        double w = contact.weight;
        Vec2 n = contact.normal;
        m_accumulationBuffer[a] += w;
        m_accumulationBuffer[b] += w;
        final Vec2 a2A = m_accumulation2Buffer[a];
        final Vec2 a2B = m_accumulation2Buffer[b];
        final double inter = (1 - w) * w;
        a2A.x -= inter * n.x;
        a2A.y -= inter * n.y;
        a2B.x += inter * n.x;
        a2B.y += inter * n.y;
      }
    }
    double strengthA = m_surfaceTensionStrengthA * getCriticalVelocity(step);
    double strengthB = m_surfaceTensionStrengthB * getCriticalVelocity(step);
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      if ((contact.flags & ParticleType.b2_tensileParticle) != 0) {
        int a = contact.indexA;
        int b = contact.indexB;
        double w = contact.weight;
        Vec2 n = contact.normal;
        final Vec2 a2A = m_accumulation2Buffer[a];
        final Vec2 a2B = m_accumulation2Buffer[b];
        double h = m_accumulationBuffer[a] + m_accumulationBuffer[b];
        final double sx = a2B.x - a2A.x;
        final double sy = a2B.y - a2A.y;
        double fn = (strengthA * (h - 2) + strengthB * (sx * n.x + sy * n.y)) * w;
        final double fx = fn * n.x;
        final double fy = fn * n.y;
        final Vec2 va = m_velocityBuffer.data[a];
        final Vec2 vb = m_velocityBuffer.data[b];
        va.x -= fx;
        va.y -= fy;
        vb.x += fx;
        vb.y += fy;
      }
    }
  }

  void solveViscous(final TimeStep step) {
    double viscousStrength = m_viscousStrength;
    for (int k = 0; k < m_bodyContactCount; k++) {
      final ParticleBodyContact contact = m_bodyContactBuffer[k];
      int a = contact.index;
      if ((m_flagsBuffer.data[a] & ParticleType.b2_viscousParticle) != 0) {
        Body b = contact.body;
        double w = contact.weight;
        double m = contact.mass;
        Vec2 p = m_positionBuffer.data[a];
        final Vec2 va = m_velocityBuffer.data[a];
        final double tempX = p.x - b.m_sweep.c.x;
        final double tempY = p.y - b.m_sweep.c.y;
        final double vx = -b.m_angularVelocity * tempY + b.m_linearVelocity.x - va.x;
        final double vy = b.m_angularVelocity * tempX + b.m_linearVelocity.y - va.y;
        final Vec2 f = tempVec;
        final double pInvMass = getParticleInvMass();
        f.x = viscousStrength * m * w * vx;
        f.y = viscousStrength * m * w * vy;
        va.x += pInvMass * f.x;
        va.y += pInvMass * f.y;
        f.x = -f.x;
        f.y = -f.y;
        b.applyLinearImpulse(f, p, true);
      }
    }
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      if ((contact.flags & ParticleType.b2_viscousParticle) != 0) {
        int a = contact.indexA;
        int b = contact.indexB;
        double w = contact.weight;
        final Vec2 va = m_velocityBuffer.data[a];
        final Vec2 vb = m_velocityBuffer.data[b];
        final double vx = vb.x - va.x;
        final double vy = vb.y - va.y;
        final double fx = viscousStrength * w * vx;
        final double fy = viscousStrength * w * vy;
        va.x += fx;
        va.y += fy;
        vb.x -= fx;
        vb.y -= fy;
      }
    }
  }

  void solvePowder(final TimeStep step) {
    double powderStrength = m_powderStrength * getCriticalVelocity(step);
    double minWeight = 1.0f - Settings.particleStride;
    for (int k = 0; k < m_bodyContactCount; k++) {
      final ParticleBodyContact contact = m_bodyContactBuffer[k];
      int a = contact.index;
      if ((m_flagsBuffer.data[a] & ParticleType.b2_powderParticle) != 0) {
        double w = contact.weight;
        if (w > minWeight) {
          Body b = contact.body;
          double m = contact.mass;
          Vec2 p = m_positionBuffer.data[a];
          Vec2 n = contact.normal;
          final Vec2 f = tempVec;
          final Vec2 va = m_velocityBuffer.data[a];
          final double inter = powderStrength * m * (w - minWeight);
          final double pInvMass = getParticleInvMass();
          f.x = inter * n.x;
          f.y = inter * n.y;
          va.x -= pInvMass * f.x;
          va.y -= pInvMass * f.y;
          b.applyLinearImpulse(f, p, true);
        }
      }
    }
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      if ((contact.flags & ParticleType.b2_powderParticle) != 0) {
        double w = contact.weight;
        if (w > minWeight) {
          int a = contact.indexA;
          int b = contact.indexB;
          Vec2 n = contact.normal;
          final Vec2 va = m_velocityBuffer.data[a];
          final Vec2 vb = m_velocityBuffer.data[b];
          final double inter = powderStrength * (w - minWeight);
          final double fx = inter * n.x;
          final double fy = inter * n.y;
          va.x -= fx;
          va.y -= fy;
          vb.x += fx;
          vb.y += fy;
        }
      }
    }
  }

  void solveSolid(final TimeStep step) {
    // applies extra repulsive force from solid particle groups
    m_depthBuffer = requestParticleBuffer(m_depthBuffer);
    double ejectionStrength = step.inv_dt * m_ejectionStrength;
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      if (m_groupBuffer[a] != m_groupBuffer[b]) {
        double w = contact.weight;
        Vec2 n = contact.normal;
        double h = m_depthBuffer[a] + m_depthBuffer[b];
        final Vec2 va = m_velocityBuffer.data[a];
        final Vec2 vb = m_velocityBuffer.data[b];
        final double inter = ejectionStrength * h * w;
        final double fx = inter * n.x;
        final double fy = inter * n.y;
        va.x -= fx;
        va.y -= fy;
        vb.x += fx;
        vb.y += fy;
      }
    }
  }

  void solveColorMixing(final TimeStep step) {
    // mixes color between contacting particles
    m_colorBuffer.data = requestParticleBuffer(ParticleColor.class, m_colorBuffer.data);
    int colorMixing256 = (int) (256 * m_colorMixingStrength);
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      if ((m_flagsBuffer.data[a] & m_flagsBuffer.data[b] & ParticleType.b2_colorMixingParticle) != 0) {
        ParticleColor colorA = m_colorBuffer.data[a];
        ParticleColor colorB = m_colorBuffer.data[b];
        int dr = (colorMixing256 * (colorB.r - colorA.r)) >> 8;
        int dg = (colorMixing256 * (colorB.g - colorA.g)) >> 8;
        int db = (colorMixing256 * (colorB.b - colorA.b)) >> 8;
        int da = (colorMixing256 * (colorB.a - colorA.a)) >> 8;
        colorA.r += dr;
        colorA.g += dg;
        colorA.b += db;
        colorA.a += da;
        colorB.r -= dr;
        colorB.g -= dg;
        colorB.b -= db;
        colorB.a -= da;
      }
    }
  }

  void solveZombie() {
    // removes particles with zombie flag
    int newCount = 0;
    int[] newIndices = new int[m_count];
    for (int i = 0; i < m_count; i++) {
      int flags = m_flagsBuffer.data[i];
      if ((flags & ParticleType.b2_zombieParticle) != 0) {
        ParticleDestructionListener destructionListener = m_world.getParticleDestructionListener();
        if ((flags & ParticleType.b2_destructionListener) != 0 && destructionListener != null) {
          destructionListener.sayGoodbye(i);
        }
        newIndices[i] = Settings.invalidParticleIndex;
      } else {
        newIndices[i] = newCount;
        if (i != newCount) {
          m_flagsBuffer.data[newCount] = m_flagsBuffer.data[i];
          m_positionBuffer.data[newCount].set(m_positionBuffer.data[i]);
          m_velocityBuffer.data[newCount].set(m_velocityBuffer.data[i]);
          m_groupBuffer[newCount] = m_groupBuffer[i];
          if (m_depthBuffer != null) {
            m_depthBuffer[newCount] = m_depthBuffer[i];
          }
          if (m_colorBuffer.data != null) {
            m_colorBuffer.data[newCount].set(m_colorBuffer.data[i]);
          }
          if (m_userDataBuffer.data != null) {
            m_userDataBuffer.data[newCount] = m_userDataBuffer.data[i];
          }
        }
        newCount++;
      }
    }

    // update proxies
    for (int k = 0; k < m_proxyCount; k++) {
      Proxy proxy = m_proxyBuffer[k];
      proxy.index = newIndices[proxy.index];
    }

    // Proxy lastProxy = std.remove_if(
    // m_proxyBuffer, m_proxyBuffer + m_proxyCount,
    // Test.IsProxyInvalid);
    // m_proxyCount = (int) (lastProxy - m_proxyBuffer);
    int j = m_proxyCount;
    for (int i = 0; i < j; i++) {
      if (Test.IsProxyInvalid(m_proxyBuffer[i])) {
        --j;
        Proxy temp = m_proxyBuffer[j];
        m_proxyBuffer[j] = m_proxyBuffer[i];
        m_proxyBuffer[i] = temp;
        --i;
      }
    }
    m_proxyCount = j;

    // update contacts
    for (int k = 0; k < m_contactCount; k++) {
      ParticleContact contact = m_contactBuffer[k];
      contact.indexA = newIndices[contact.indexA];
      contact.indexB = newIndices[contact.indexB];
    }
    // ParticleContact lastContact = std.remove_if(
    // m_contactBuffer, m_contactBuffer + m_contactCount,
    // Test.IsContactInvalid);
    // m_contactCount = (int) (lastContact - m_contactBuffer);
    j = m_contactCount;
    for (int i = 0; i < j; i++) {
      if (Test.IsContactInvalid(m_contactBuffer[i])) {
        --j;
        ParticleContact temp = m_contactBuffer[j];
        m_contactBuffer[j] = m_contactBuffer[i];
        m_contactBuffer[i] = temp;
        --i;
      }
    }
    m_contactCount = j;

    // update particle-body contacts
    for (int k = 0; k < m_bodyContactCount; k++) {
      ParticleBodyContact contact = m_bodyContactBuffer[k];
      contact.index = newIndices[contact.index];
    }
    // ParticleBodyContact lastBodyContact = std.remove_if(
    // m_bodyContactBuffer, m_bodyContactBuffer + m_bodyContactCount,
    // Test.IsBodyContactInvalid);
    // m_bodyContactCount = (int) (lastBodyContact - m_bodyContactBuffer);
    j = m_bodyContactCount;
    for (int i = 0; i < j; i++) {
      if (Test.IsBodyContactInvalid(m_bodyContactBuffer[i])) {
        --j;
        ParticleBodyContact temp = m_bodyContactBuffer[j];
        m_bodyContactBuffer[j] = m_bodyContactBuffer[i];
        m_bodyContactBuffer[i] = temp;
        --i;
      }
    }
    m_bodyContactCount = j;

    // update pairs
    for (int k = 0; k < m_pairCount; k++) {
      Pair pair = m_pairBuffer[k];
      pair.indexA = newIndices[pair.indexA];
      pair.indexB = newIndices[pair.indexB];
    }
    // Pair lastPair = std.remove_if(m_pairBuffer, m_pairBuffer + m_pairCount, Test.IsPairInvalid);
    // m_pairCount = (int) (lastPair - m_pairBuffer);
    j = m_pairCount;
    for (int i = 0; i < j; i++) {
      if (Test.IsPairInvalid(m_pairBuffer[i])) {
        --j;
        Pair temp = m_pairBuffer[j];
        m_pairBuffer[j] = m_pairBuffer[i];
        m_pairBuffer[i] = temp;
        --i;
      }
    }
    m_pairCount = j;

    // update triads
    for (int k = 0; k < m_triadCount; k++) {
      Triad triad = m_triadBuffer[k];
      triad.indexA = newIndices[triad.indexA];
      triad.indexB = newIndices[triad.indexB];
      triad.indexC = newIndices[triad.indexC];
    }
    // Triad lastTriad =
    // std.remove_if(m_triadBuffer, m_triadBuffer + m_triadCount, Test.isTriadInvalid);
    // m_triadCount = (int) (lastTriad - m_triadBuffer);
    j = m_triadCount;
    for (int i = 0; i < j; i++) {
      if (Test.IsTriadInvalid(m_triadBuffer[i])) {
        --j;
        Triad temp = m_triadBuffer[j];
        m_triadBuffer[j] = m_triadBuffer[i];
        m_triadBuffer[i] = temp;
        --i;
      }
    }
    m_triadCount = j;

    // update groups
    for (ParticleGroup group = m_groupList; group != null; group = group.getNext()) {
      int firstIndex = newCount;
      int lastIndex = 0;
      boolean modified = false;
      for (int i = group.m_firstIndex; i < group.m_lastIndex; i++) {
        j = newIndices[i];
        if (j >= 0) {
          firstIndex = Math.min(firstIndex, j);
          lastIndex = Math.max(lastIndex, j + 1);
        } else {
          modified = true;
        }
      }
      if (firstIndex < lastIndex) {
        group.m_firstIndex = firstIndex;
        group.m_lastIndex = lastIndex;
        if (modified) {
          if ((group.m_groupFlags & ParticleGroupType.b2_rigidParticleGroup) != 0) {
            group.m_toBeSplit = true;
          }
        }
      } else {
        group.m_firstIndex = 0;
        group.m_lastIndex = 0;
        if (group.m_destroyAutomatically) {
          group.m_toBeDestroyed = true;
        }
      }
    }

    // update particle count
    m_count = newCount;
    // m_world.m_stackAllocator.Free(newIndices);

    // destroy bodies with no particles
    for (ParticleGroup group = m_groupList; group != null;) {
      ParticleGroup next = group.getNext();
      if (group.m_toBeDestroyed) {
        destroyParticleGroup(group);
      } else if (group.m_toBeSplit) {
        // TODO: split the group
      }
      group = next;
    }
  }

  private static class NewIndices {
    int start, mid, end;

    final int getIndex(final int i) {
      if (i < start) {
        return i;
      } else if (i < mid) {
        return i + end - mid;
      } else if (i < end) {
        return i + start - mid;
      } else {
        return i;
      }
    }
  }

  private final NewIndices newIndices = new NewIndices();


  void RotateBuffer(int start, int mid, int end) {
    // move the particles assigned to the given group toward the end of array
    if (start == mid || mid == end) {
      return;
    }
    newIndices.start = start;
    newIndices.mid = mid;
    newIndices.end = end;

    BufferUtils.rotate(m_flagsBuffer.data, start, mid, end);
    BufferUtils.rotate(m_positionBuffer.data, start, mid, end);
    BufferUtils.rotate(m_velocityBuffer.data, start, mid, end);
    BufferUtils.rotate(m_groupBuffer, start, mid, end);
    if (m_depthBuffer != null) {
      BufferUtils.rotate(m_depthBuffer, start, mid, end);
    }
    if (m_colorBuffer.data != null) {
      BufferUtils.rotate(m_colorBuffer.data, start, mid, end);
    }
    if (m_userDataBuffer.data != null) {
      BufferUtils.rotate(m_userDataBuffer.data, start, mid, end);
    }

    // update proxies
    for (int k = 0; k < m_proxyCount; k++) {
      Proxy proxy = m_proxyBuffer[k];
      proxy.index = newIndices.getIndex(proxy.index);
    }

    // update contacts
    for (int k = 0; k < m_contactCount; k++) {
      ParticleContact contact = m_contactBuffer[k];
      contact.indexA = newIndices.getIndex(contact.indexA);
      contact.indexB = newIndices.getIndex(contact.indexB);
    }

    // update particle-body contacts
    for (int k = 0; k < m_bodyContactCount; k++) {
      ParticleBodyContact contact = m_bodyContactBuffer[k];
      contact.index = newIndices.getIndex(contact.index);
    }

    // update pairs
    for (int k = 0; k < m_pairCount; k++) {
      Pair pair = m_pairBuffer[k];
      pair.indexA = newIndices.getIndex(pair.indexA);
      pair.indexB = newIndices.getIndex(pair.indexB);
    }

    // update triads
    for (int k = 0; k < m_triadCount; k++) {
      Triad triad = m_triadBuffer[k];
      triad.indexA = newIndices.getIndex(triad.indexA);
      triad.indexB = newIndices.getIndex(triad.indexB);
      triad.indexC = newIndices.getIndex(triad.indexC);
    }

    // update groups
    for (ParticleGroup group = m_groupList; group != null; group = group.getNext()) {
      group.m_firstIndex = newIndices.getIndex(group.m_firstIndex);
      group.m_lastIndex = newIndices.getIndex(group.m_lastIndex - 1) + 1;
    }
  }

  public void setParticleRadius(double radius) {
    m_particleDiameter = 2 * radius;
    m_squaredDiameter = m_particleDiameter * m_particleDiameter;
    m_inverseDiameter = 1 / m_particleDiameter;
  }

  public void setParticleDensity(double density) {
    m_density = density;
    m_inverseDensity = 1 / m_density;
  }

  public double getParticleDensity() {
    return m_density;
  }

  public void setParticleGravityScale(double gravityScale) {
    m_gravityScale = gravityScale;
  }

  public double getParticleGravityScale() {
    return m_gravityScale;
  }

  public void setParticleDamping(double damping) {
    m_dampingStrength = damping;
  }

  public double getParticleDamping() {
    return m_dampingStrength;
  }

  public double getParticleRadius() {
    return m_particleDiameter / 2;
  }

  double getCriticalVelocity(final TimeStep step) {
    return m_particleDiameter * step.inv_dt;
  }

  double getCriticalVelocitySquared(final TimeStep step) {
    double velocity = getCriticalVelocity(step);
    return velocity * velocity;
  }

  double getCriticalPressure(final TimeStep step) {
    return m_density * getCriticalVelocitySquared(step);
  }

  double getParticleStride() {
    return Settings.particleStride * m_particleDiameter;
  }

  double getParticleMass() {
    double stride = getParticleStride();
    return m_density * stride * stride;
  }

  double getParticleInvMass() {
    return 1.777777f * m_inverseDensity * m_inverseDiameter * m_inverseDiameter;
  }

  public int[] getParticleFlagsBuffer() {
    return m_flagsBuffer.data;
  }

  public Vec2[] getParticlePositionBuffer() {
    return m_positionBuffer.data;
  }

  public Vec2[] getParticleVelocityBuffer() {
    return m_velocityBuffer.data;
  }

  public ParticleColor[] getParticleColorBuffer() {
    m_colorBuffer.data = requestParticleBuffer(ParticleColor.class, m_colorBuffer.data);
    return m_colorBuffer.data;
  }

  public Object[] getParticleUserDataBuffer() {
    m_userDataBuffer.data = requestParticleBuffer(Object.class, m_userDataBuffer.data);
    return m_userDataBuffer.data;
  }

  public int getParticleMaxCount() {
    return m_maxCount;
  }

  public void setParticleMaxCount(int count) {
    assert (m_count <= count);
    m_maxCount = count;
  }

  void setParticleBuffer(ParticleBufferInt buffer, int[] newData, int newCapacity) {
    assert ((newData != null && newCapacity != 0) || (newData == null && newCapacity == 0));
    if (buffer.userSuppliedCapacity != 0) {
      // m_world.m_blockAllocator.Free(buffer.data, sizeof(T) * m_internalAllocatedCapacity);
    }
    buffer.data = newData;
    buffer.userSuppliedCapacity = newCapacity;
  }

  <T> void setParticleBuffer(ParticleBuffer<T> buffer, T[] newData, int newCapacity) {
    assert ((newData != null && newCapacity != 0) || (newData == null && newCapacity == 0));
    if (buffer.userSuppliedCapacity != 0) {
      // m_world.m_blockAllocator.Free(buffer.data, sizeof(T) * m_internalAllocatedCapacity);
    }
    buffer.data = newData;
    buffer.userSuppliedCapacity = newCapacity;
  }

  public void setParticleFlagsBuffer(int[] buffer, int capacity) {
    setParticleBuffer(m_flagsBuffer, buffer, capacity);
  }

  public void setParticlePositionBuffer(Vec2[] buffer, int capacity) {
    setParticleBuffer(m_positionBuffer, buffer, capacity);
  }

  public void setParticleVelocityBuffer(Vec2[] buffer, int capacity) {
    setParticleBuffer(m_velocityBuffer, buffer, capacity);
  }

  public void setParticleColorBuffer(ParticleColor[] buffer, int capacity) {
    setParticleBuffer(m_colorBuffer, buffer, capacity);
  }

  public ParticleGroup[] getParticleGroupBuffer() {
    return m_groupBuffer;
  }

  public int getParticleGroupCount() {
    return m_groupCount;
  }

  public ParticleGroup[] getParticleGroupList() {
    return m_groupBuffer;
  }

  public int getParticleCount() {
    return m_count;
  }

  public void setParticleUserDataBuffer(Object[] buffer, int capacity) {
    setParticleBuffer(m_userDataBuffer, buffer, capacity);
  }

  private static final int lowerBound(Proxy[] ray, int length, long tag) {
    int left = 0;
    int step, curr;
    while (length > 0) {
      step = length / 2;
      curr = left + step;
      if (ray[curr].tag < tag) {
        left = curr + 1;
        length -= step + 1;
      } else {
        length = step;
      }
    }
    return left;
  }

  private static final int upperBound(Proxy[] ray, int length, long tag) {
    int left = 0;
    int step, curr;
    while (length > 0) {
      step = length / 2;
      curr = left + step;
      if (ray[curr].tag <= tag) {
        left = curr + 1;
        length -= step + 1;
      } else {
        length = step;
      }
    }
    return left;
  }

  public void queryAABB(ParticleQueryCallback callback, final AABB aabb) {
    if (m_proxyCount == 0) {
      return;
    }

    final double lowerBoundX = aabb.lowerBound.x;
    final double lowerBoundY = aabb.lowerBound.y;
    final double upperBoundX = aabb.upperBound.x;
    final double upperBoundY = aabb.upperBound.y;
    int firstProxy =
        lowerBound(m_proxyBuffer, m_proxyCount,
            computeTag(m_inverseDiameter * lowerBoundX, m_inverseDiameter * lowerBoundY));
    int lastProxy =
        upperBound(m_proxyBuffer, m_proxyCount,
            computeTag(m_inverseDiameter * upperBoundX, m_inverseDiameter * upperBoundY));
    for (int proxy = firstProxy; proxy < lastProxy; ++proxy) {
      int i = m_proxyBuffer[proxy].index;
      final Vec2 p = m_positionBuffer.data[i];
      if (lowerBoundX < p.x && p.x < upperBoundX && lowerBoundY < p.y && p.y < upperBoundY) {
        if (!callback.reportParticle(i)) {
          break;
        }
      }
    }
  }

  /**
   * @param callback
   * @param point1
   * @param point2
   */
  public void raycast(ParticleRaycastCallback callback, final Vec2 point1, final Vec2 point2) {
    if (m_proxyCount == 0) {
      return;
    }
    int firstProxy =
        lowerBound(
            m_proxyBuffer,
            m_proxyCount,
            computeTag(m_inverseDiameter * Math.min(point1.x, point2.x) - 1, m_inverseDiameter
                * Math.min(point1.y, point2.y) - 1));
    int lastProxy =
        upperBound(
            m_proxyBuffer,
            m_proxyCount,
            computeTag(m_inverseDiameter * Math.max(point1.x, point2.x) + 1, m_inverseDiameter
                * Math.max(point1.y, point2.y) + 1));
    double fraction = 1;
    // solving the following equation:
    // ((1-t)*point1+t*point2-position)^2=diameter^2
    // where t is a potential fraction
    final double vx = point2.x - point1.x;
    final double vy = point2.y - point1.y;
    double v2 = vx * vx + vy * vy;
    if (v2 == 0) v2 = Float.MAX_VALUE;
    for (int proxy = firstProxy; proxy < lastProxy; ++proxy) {
      int i = m_proxyBuffer[proxy].index;
      final Vec2 posI = m_positionBuffer.data[i];
      final double px = point1.x - posI.x;
      final double py = point1.y - posI.y;
      double pv = px * vx + py * vy;
      double p2 = px * px + py * py;
      double determinant = pv * pv - v2 * (p2 - m_squaredDiameter);
      if (determinant >= 0) {
        double sqrtDeterminant = Math.sqrt(determinant);
        // find a solution between 0 and fraction
        double t = (-pv - sqrtDeterminant) / v2;
        if (t > fraction) {
          continue;
        }
        if (t < 0) {
          t = (-pv + sqrtDeterminant) / v2;
          if (t < 0 || t > fraction) {
            continue;
          }
        }
        final Vec2 n = tempVec;
        tempVec.x = px + t * vx;
        tempVec.y = py + t * vy;
        n.normalize();
        final Vec2 point = tempVec2;
        point.x = point1.x + t * vx;
        point.y = point1.y + t * vy;
        double f = callback.reportParticle(i, point, n, t);
        fraction = Math.min(fraction, f);
        if (fraction <= 0) {
          break;
        }
      }
    }
  }

  public double computeParticleCollisionEnergy() {
    double sum_v2 = 0;
    for (int k = 0; k < m_contactCount; k++) {
      final ParticleContact contact = m_contactBuffer[k];
      int a = contact.indexA;
      int b = contact.indexB;
      Vec2 n = contact.normal;
      final Vec2 va = m_velocityBuffer.data[a];
      final Vec2 vb = m_velocityBuffer.data[b];
      final double vx = vb.x - va.x;
      final double vy = vb.y - va.y;
      double vn = vx * n.x + vy * n.y;
      if (vn < 0) {
        sum_v2 += vn * vn;
      }
    }
    return 0.5f * getParticleMass() * sum_v2;
  }

  // reallocate a buffer
  static <T> T[] reallocateBuffer(ParticleBuffer<T> buffer, int oldCapacity, int newCapacity,
      boolean deferred) {
    assert (newCapacity > oldCapacity);
    return BufferUtils.reallocateBuffer(buffer.dataClass, buffer.data, buffer.userSuppliedCapacity,
        oldCapacity, newCapacity, deferred);
  }

  static int[] reallocateBuffer(ParticleBufferInt buffer, int oldCapacity, int newCapacity,
      boolean deferred) {
    assert (newCapacity > oldCapacity);
    return BufferUtils.reallocateBuffer(buffer.data, buffer.userSuppliedCapacity, oldCapacity,
        newCapacity, deferred);
  }

  @SuppressWarnings("unchecked")
  <T> T[] requestParticleBuffer(Class<T> klass, T[] buffer) {
    if (buffer == null) {
      buffer = (T[]) Array.newInstance(klass, m_internalAllocatedCapacity);
      for (int i = 0; i < m_internalAllocatedCapacity; i++) {
        try {
          buffer[i] = klass.newInstance();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return buffer;
  }

  double[] requestParticleBuffer(double[] buffer) {
    if (buffer == null) {
      buffer = new double[m_internalAllocatedCapacity];
    }
    return buffer;
  }

  public static class ParticleBuffer<T> {
    public T[] data;
    final Class<T> dataClass;
    int userSuppliedCapacity;

    public ParticleBuffer(Class<T> dataClass) {
      this.dataClass = dataClass;
    }
  }
  static class ParticleBufferInt {
    int[] data;
    int userSuppliedCapacity;
  }

  /** Used for detecting particle contacts */
  public static class Proxy implements Comparable<Proxy> {
    int index;
    long tag;

    @Override
    public int compareTo(Proxy o) {
      return (tag - o.tag) < 0 ? -1 : (o.tag == tag ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Proxy other = (Proxy) obj;
      if (tag != other.tag) return false;
      return true;
    }
  }

  /** Connection between two particles */
  public static class Pair {
    int indexA, indexB;
    int flags;
    double strength;
    double distance;
  }

  /** Connection between three particles */
  public static class Triad {
    int indexA, indexB, indexC;
    int flags;
    double strength;
    final Vec2 pa = new Vec2(), pb = new Vec2(), pc = new Vec2();
    double ka, kb, kc, s;
  }

  // Callback used with VoronoiDiagram.
  static class CreateParticleGroupCallback implements VoronoiDiagramCallback {
    @Override
	public void callback(int a, int b, int c) {
      final Vec2 pa = system.m_positionBuffer.data[a];
      final Vec2 pb = system.m_positionBuffer.data[b];
      final Vec2 pc = system.m_positionBuffer.data[c];
      final double dabx = pa.x - pb.x;
      final double daby = pa.y - pb.y;
      final double dbcx = pb.x - pc.x;
      final double dbcy = pb.y - pc.y;
      final double dcax = pc.x - pa.x;
      final double dcay = pc.y - pa.y;
      double maxDistanceSquared = Settings.maxTriadDistanceSquared * system.m_squaredDiameter;
      if (dabx * dabx + daby * daby < maxDistanceSquared
          && dbcx * dbcx + dbcy * dbcy < maxDistanceSquared
          && dcax * dcax + dcay * dcay < maxDistanceSquared) {
        if (system.m_triadCount >= system.m_triadCapacity) {
          int oldCapacity = system.m_triadCapacity;
          int newCapacity =
              system.m_triadCount != 0
                  ? 2 * system.m_triadCount
                  : Settings.minParticleBufferCapacity;
          system.m_triadBuffer =
              BufferUtils.reallocateBuffer(Triad.class, system.m_triadBuffer, oldCapacity,
                  newCapacity);
          system.m_triadCapacity = newCapacity;
        }
        Triad triad = system.m_triadBuffer[system.m_triadCount];
        triad.indexA = a;
        triad.indexB = b;
        triad.indexC = c;
        triad.flags =
            system.m_flagsBuffer.data[a] | system.m_flagsBuffer.data[b]
                | system.m_flagsBuffer.data[c];
        triad.strength = def.strength;
        final double midPointx = (double) 1 / 3 * (pa.x + pb.x + pc.x);
        final double midPointy = (double) 1 / 3 * (pa.y + pb.y + pc.y);
        triad.pa.x = pa.x - midPointx;
        triad.pa.y = pa.y - midPointy;
        triad.pb.x = pb.x - midPointx;
        triad.pb.y = pb.y - midPointy;
        triad.pc.x = pc.x - midPointx;
        triad.pc.y = pc.y - midPointy;
        triad.ka = -(dcax * dabx + dcay * daby);
        triad.kb = -(dabx * dbcx + daby * dbcy);
        triad.kc = -(dbcx * dcax + dbcy * dcay);
        triad.s = Vec2.cross(pa, pb) + Vec2.cross(pb, pc) + Vec2.cross(pc, pa);
        system.m_triadCount++;
      }
    }

    ParticleSystem system;
    ParticleGroupDef def; // pointer
    int firstIndex;
  }

  // Callback used with VoronoiDiagram.
  static class JoinParticleGroupsCallback implements VoronoiDiagramCallback {
    @Override
	public void callback(int a, int b, int c) {
      // Create a triad if it will contain particles from both groups.
      int countA =
          ((a < groupB.m_firstIndex) ? 1 : 0) + ((b < groupB.m_firstIndex) ? 1 : 0)
              + ((c < groupB.m_firstIndex) ? 1 : 0);
      if (countA > 0 && countA < 3) {
        int af = system.m_flagsBuffer.data[a];
        int bf = system.m_flagsBuffer.data[b];
        int cf = system.m_flagsBuffer.data[c];
        if ((af & bf & cf & k_triadFlags) != 0) {
          final Vec2 pa = system.m_positionBuffer.data[a];
          final Vec2 pb = system.m_positionBuffer.data[b];
          final Vec2 pc = system.m_positionBuffer.data[c];
          final double dabx = pa.x - pb.x;
          final double daby = pa.y - pb.y;
          final double dbcx = pb.x - pc.x;
          final double dbcy = pb.y - pc.y;
          final double dcax = pc.x - pa.x;
          final double dcay = pc.y - pa.y;
          double maxDistanceSquared = Settings.maxTriadDistanceSquared * system.m_squaredDiameter;
          if (dabx * dabx + daby * daby < maxDistanceSquared
              && dbcx * dbcx + dbcy * dbcy < maxDistanceSquared
              && dcax * dcax + dcay * dcay < maxDistanceSquared) {
            if (system.m_triadCount >= system.m_triadCapacity) {
              int oldCapacity = system.m_triadCapacity;
              int newCapacity =
                  system.m_triadCount != 0
                      ? 2 * system.m_triadCount
                      : Settings.minParticleBufferCapacity;
              system.m_triadBuffer =
                  BufferUtils.reallocateBuffer(Triad.class, system.m_triadBuffer, oldCapacity,
                      newCapacity);
              system.m_triadCapacity = newCapacity;
            }
            Triad triad = system.m_triadBuffer[system.m_triadCount];
            triad.indexA = a;
            triad.indexB = b;
            triad.indexC = c;
            triad.flags = af | bf | cf;
            triad.strength = Math.min(groupA.m_strength, groupB.m_strength);
            final double midPointx = (double) 1 / 3 * (pa.x + pb.x + pc.x);
            final double midPointy = (double) 1 / 3 * (pa.y + pb.y + pc.y);
            triad.pa.x = pa.x - midPointx;
            triad.pa.y = pa.y - midPointy;
            triad.pb.x = pb.x - midPointx;
            triad.pb.y = pb.y - midPointy;
            triad.pc.x = pc.x - midPointx;
            triad.pc.y = pc.y - midPointy;
            triad.ka = -(dcax * dabx + dcay * daby);
            triad.kb = -(dabx * dbcx + daby * dbcy);
            triad.kc = -(dbcx * dcax + dbcy * dcay);
            triad.s = Vec2.cross(pa, pb) + Vec2.cross(pb, pc) + Vec2.cross(pc, pa);
            system.m_triadCount++;
          }
        }
      }
    }

    ParticleSystem system;
    ParticleGroup groupA;
    ParticleGroup groupB;
  };

  static class DestroyParticlesInShapeCallback implements ParticleQueryCallback {
    ParticleSystem system;
    Shape shape;
    Transform xf;
    boolean callDestructionListener;
    int destroyed;

    public DestroyParticlesInShapeCallback() {
      // TODO Auto-generated constructor stub
    }

    public void init(ParticleSystem system, Shape shape, Transform xf,
        boolean callDestructionListener) {
      this.system = system;
      this.shape = shape;
      this.xf = xf;
      this.destroyed = 0;
      this.callDestructionListener = callDestructionListener;
    }

    @Override
    public boolean reportParticle(int index) {
      assert (index >= 0 && index < system.m_count);
      if (shape.testPoint(xf, system.m_positionBuffer.data[index])) {
        system.destroyParticle(index, callDestructionListener);
        destroyed++;
      }
      return true;
    }
  }

  static class UpdateBodyContactsCallback implements QueryCallback {
    ParticleSystem system;

    private final Vec2 tempVec = new Vec2();

    @Override
    public boolean reportFixture(Fixture fixture) {
      if (fixture.isSensor()) {
        return true;
      }
      final Shape shape = fixture.getShape();
      Body b = fixture.getBody();
      Vec2 bp = b.getWorldCenter();
      double bm = b.getMass();
      double bI = b.getInertia() - bm * b.getLocalCenter().lengthSquared();
      double invBm = bm > 0 ? 1 / bm : 0;
      double invBI = bI > 0 ? 1 / bI : 0;
      int childCount = shape.getChildCount();
      for (int childIndex = 0; childIndex < childCount; childIndex++) {
        AABB aabb = fixture.getAABB(childIndex);
        final double aabblowerBoundx = aabb.lowerBound.x - system.m_particleDiameter;
        final double aabblowerBoundy = aabb.lowerBound.y - system.m_particleDiameter;
        final double aabbupperBoundx = aabb.upperBound.x + system.m_particleDiameter;
        final double aabbupperBoundy = aabb.upperBound.y + system.m_particleDiameter;
        int firstProxy =
            lowerBound(
                system.m_proxyBuffer,
                system.m_proxyCount,
                computeTag(system.m_inverseDiameter * aabblowerBoundx, system.m_inverseDiameter
                    * aabblowerBoundy));
        int lastProxy =
            upperBound(
                system.m_proxyBuffer,
                system.m_proxyCount,
                computeTag(system.m_inverseDiameter * aabbupperBoundx, system.m_inverseDiameter
                    * aabbupperBoundy));

        for (int proxy = firstProxy; proxy != lastProxy; ++proxy) {
          int a = system.m_proxyBuffer[proxy].index;
          Vec2 ap = system.m_positionBuffer.data[a];
          if (aabblowerBoundx <= ap.x && ap.x <= aabbupperBoundx && aabblowerBoundy <= ap.y
              && ap.y <= aabbupperBoundy) {
            double d;
            final Vec2 n = tempVec;
            d = fixture.computeDistance(ap, childIndex, n);
            if (d < system.m_particleDiameter) {
              double invAm =
                  (system.m_flagsBuffer.data[a] & ParticleType.b2_wallParticle) != 0 ? 0 : system
                      .getParticleInvMass();
              final double rpx = ap.x - bp.x;
              final double rpy = ap.y - bp.y;
              double rpn = rpx * n.y - rpy * n.x;
              if (system.m_bodyContactCount >= system.m_bodyContactCapacity) {
                int oldCapacity = system.m_bodyContactCapacity;
                int newCapacity =
                    system.m_bodyContactCount != 0
                        ? 2 * system.m_bodyContactCount
                        : Settings.minParticleBufferCapacity;
                system.m_bodyContactBuffer =
                    BufferUtils.reallocateBuffer(ParticleBodyContact.class,
                        system.m_bodyContactBuffer, oldCapacity, newCapacity);
                system.m_bodyContactCapacity = newCapacity;
              }
              ParticleBodyContact contact = system.m_bodyContactBuffer[system.m_bodyContactCount];
              contact.index = a;
              contact.body = b;
              contact.weight = 1 - d * system.m_inverseDiameter;
              contact.normal.x = -n.x;
              contact.normal.y = -n.y;
              contact.mass = 1 / (invAm + invBm + invBI * rpn * rpn);
              system.m_bodyContactCount++;
            }
          }
        }
      }
      return true;
    }
  }

  static class SolveCollisionCallback implements QueryCallback {
    ParticleSystem system;
    TimeStep step;

    private final RayCastInput input = new RayCastInput();
    private final RayCastOutput output = new RayCastOutput();
    private final Vec2 tempVec = new Vec2();
    private final Vec2 tempVec2 = new Vec2();

    @Override
    public boolean reportFixture(Fixture fixture) {
      if (fixture.isSensor()) {
        return true;
      }
      final Shape shape = fixture.getShape();
      Body body = fixture.getBody();
      int childCount = shape.getChildCount();
      for (int childIndex = 0; childIndex < childCount; childIndex++) {
        AABB aabb = fixture.getAABB(childIndex);
        final double aabblowerBoundx = aabb.lowerBound.x - system.m_particleDiameter;
        final double aabblowerBoundy = aabb.lowerBound.y - system.m_particleDiameter;
        final double aabbupperBoundx = aabb.upperBound.x + system.m_particleDiameter;
        final double aabbupperBoundy = aabb.upperBound.y + system.m_particleDiameter;
        int firstProxy =
            lowerBound(
                system.m_proxyBuffer,
                system.m_proxyCount,
                computeTag(system.m_inverseDiameter * aabblowerBoundx, system.m_inverseDiameter
                    * aabblowerBoundy));
        int lastProxy =
            upperBound(
                system.m_proxyBuffer,
                system.m_proxyCount,
                computeTag(system.m_inverseDiameter * aabbupperBoundx, system.m_inverseDiameter
                    * aabbupperBoundy));

        for (int proxy = firstProxy; proxy != lastProxy; ++proxy) {
          int a = system.m_proxyBuffer[proxy].index;
          Vec2 ap = system.m_positionBuffer.data[a];
          if (aabblowerBoundx <= ap.x && ap.x <= aabbupperBoundx && aabblowerBoundy <= ap.y
              && ap.y <= aabbupperBoundy) {
            Vec2 av = system.m_velocityBuffer.data[a];
            final Vec2 temp = tempVec;
            Transform.mulTransToOutUnsafe(body.m_xf0, ap, temp);
            Transform.mulToOutUnsafe(body.m_xf, temp, input.p1);
            input.p2.x = ap.x + step.dt * av.x;
            input.p2.y = ap.y + step.dt * av.y;
            input.maxFraction = 1;
            if (fixture.raycast(output, input, childIndex)) {
              final Vec2 p = tempVec;
              p.x =
                  (1 - output.fraction) * input.p1.x + output.fraction * input.p2.x
                      + Settings.linearSlop * output.normal.x;
              p.y =
                  (1 - output.fraction) * input.p1.y + output.fraction * input.p2.y
                      + Settings.linearSlop * output.normal.y;

              final double vx = step.inv_dt * (p.x - ap.x);
              final double vy = step.inv_dt * (p.y - ap.y);
              av.x = vx;
              av.y = vy;
              final double particleMass = system.getParticleMass();
              final double ax = particleMass * (av.x - vx);
              final double ay = particleMass * (av.y - vy);
              Vec2 b = output.normal;
              final double fdn = ax * b.x + ay * b.y;
              final Vec2 f = tempVec2;
              f.x = fdn * b.x;
              f.y = fdn * b.y;
              body.applyLinearImpulse(f, p, true);
            }
          }
        }
      }
      return true;
    }
  }

  static class Test {
    static boolean IsProxyInvalid(final Proxy proxy) {
      return proxy.index < 0;
    }

    static boolean IsContactInvalid(final ParticleContact contact) {
      return contact.indexA < 0 || contact.indexB < 0;
    }

    static boolean IsBodyContactInvalid(final ParticleBodyContact contact) {
      return contact.index < 0;
    }

    static boolean IsPairInvalid(final Pair pair) {
      return pair.indexA < 0 || pair.indexB < 0;
    }

    static boolean IsTriadInvalid(final Triad triad) {
      return triad.indexA < 0 || triad.indexB < 0 || triad.indexC < 0;
    }
  };
}
