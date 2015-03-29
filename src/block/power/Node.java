package block.power;

import java.util.Arrays;

import block.BlockGroup;

/** Represents a sink, a source or a join between conduits */
class Node {
	int x;
	int y;
	BlockGroup parent;
	NodeBlock block;
	
	Conduit[] in;
	Conduit[] out;
	
	Node(int x, int y, BlockGroup parent) {
		this();
		this.x = x;
		this.y =  y;
		this.parent = parent;
	}
	
	/** Creates a new blank node with no inflows or outflows */
	Node() {
		in = new Conduit[0];
		out = new Conduit[0];
	}

	void fillInequality(int n, double[][] mults, double[] limits) {
		
	}
	
	void addToOptimisationFunc(double[] func) {
		
	}
	
	void notify(double[] solution) {
		
	}

	public void addOut(Conduit conduit) {
		out = Arrays.copyOf(out, out.length + 1);
		out[out.length - 1] = conduit;
	}

	public void addIn(Conduit conduit) {
		in = Arrays.copyOf(in, in.length + 1);
		in[in.length - 1] = conduit;
	}
}
