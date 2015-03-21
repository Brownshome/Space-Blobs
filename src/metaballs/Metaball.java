package metaballs;

import math.Box;

public abstract class Metaball {
	/** The data MUST be 6 floats long and be compatable with the GPU */
	abstract float[] getData();
	
	public abstract Box boundingBox();
}
