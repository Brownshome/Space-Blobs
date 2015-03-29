package block.power;

import java.util.ArrayList;

/* This class aims to maximize a fitness function. This function is mostly made up of
 * satifying Nodes' power flow request.
 */

/** This class handles all the power requests and other such things */
public class PowerGrid {
	ArrayList<Conduit> conduits;
	ArrayList<Node> nodes;
	
	public static void mergeGrids(PowerGridBlock elementOne, PowerGridBlock elementTwo) {
		// TODO Auto-generated method stub
	}
	
	void calculateFlow() {
		int inequalities = (nodes.size() + conduits.size()) * 2;
		int variables = conduits.size();
		
		double[][] mults = new double[inequalities][variables];
		double[] limits = new double[inequalities];
		double[] func = new double[variables];
		
		int n = 0;
		for(int i = 0; i < conduits.size(); i++) {
			conduits.get(i).fillInequality(n, i, mults, limits);
			n += 2;
		}
		
		for(Node node : nodes) {
			node.fillInequality(n, mults, limits);
			node.addToOptimisationFunc(func);
			n += 2;
		}
		
		PowerFlowSolver pfs = new PowerFlowSolver(mults, limits, func);
		
		double[] solution = pfs.primal();
		
		for(int i = 0; i < conduits.size(); i++)
			conduits.get(i).notify(solution[i]);
		
		for(Node node : nodes)
			node.notify(solution);
	}
}
