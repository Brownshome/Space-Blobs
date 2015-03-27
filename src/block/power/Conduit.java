package block.power;

import java.util.function.DoubleConsumer;

/** Represents a path down which power can flow in a power grid */
class Conduit {
	int index;
	/** The maximum amount that power can flow from the source to the sink */
	double max;
	/** The maximum amount that power can flow from the sink to the source */
	double min;
	/** All of the blocks that make up this conduit */
	ConduitBlock[] blocks;
	
	Conduit(int index, double max, double min) {
		this.max = max;
		this.min = min;
		this.index = index;
	}
	
	void fillInequality(int n, double[][] mults, double[] limits) {
		mults[n][index] = 1;
		mults[n + 1][index] = -1;
		limits[n] = max;
		limits[n + 1] = min;
	}

	void notify(double[] solution) {
		notify.accept(solution[index]);
	}
}
