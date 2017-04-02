package examples.jmdp;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;

public class Problem1 extends FiniteMDP<PropertiesState, PropertiesAction> {
	
	int N, K;
	double[][][] P;
	
	// Constructor
	public Problem1(PropertiesState initialState, int N) {
		super(initialState, N);
		this.K = initialState.getNumProps();
		initProbs();
	}
	
	private void initProbs() {
		P = new double[K][3][3];
		for (int k = 0; k < K; k++) {
			P[k][0][0] = 0.2;
			P[k][0][1] = 0.8;
			P[k][1][1] = 0.5;
			P[k][1][2] = 0.5;
			P[k][2][2] = 1.0;
		}
	}
	
	@Override
	public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t) {
		int action = a.getProperty(0);
		for (int k = 0; k < K; k++) {
			if (k != action) {
				int sk = i.getProperty(k);
				int spk = j.getProperty(k);
				if (sk != spk)
					return 0.0;
			}
		}		
		int s = i.getProperty(action);
		int sprime = j.getProperty(action);
		return P[action][s][sprime];
	}
	
	@Override
	public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
		int action = a.getProperty(0);
		int s = i.getProperty(action);
		if (s == 1) // Current state is 1, i.e., started
			return -Math.pow(action+1, 2) * P[action][1][2]; // Expected reward k^2 if finished, negative since solver is to minimize
		else
			return 0.0;			
	}
	
	@Override
	public double finalCost(PropertiesState i) {
		return 0.0;
	}
	
	@Override
	public Actions<PropertiesAction> feasibleActions(PropertiesState i, int t) {
		ActionsSet<PropertiesAction> actionSet = new ActionsSet<PropertiesAction>();
		for (int action = 0; action < K; action++)
			actionSet.add(new PropertiesAction(new int[]{action}));
		return actionSet;
	}
	
	@Override
	public States<PropertiesState> reachable(PropertiesState i, PropertiesAction a, int t) {
		StatesSet<PropertiesState> reachableStates = new StatesSet<PropertiesState>(i);
		//PropertiesState j = new PropertiesState(i);
		int[] iarray = i.getProperties();
		int action = a.getProperty(0);
		if (iarray[action] < 2) {
			iarray[action]++;
			PropertiesState j = new PropertiesState(iarray);			
			reachableStates.add(j);
		}
		return reachableStates;
	}
		
	public static void main(String argv[]) throws Exception {
		int K = 4;
		int N = 8;
		int arr[] = new int[K];
		for (int k = 0; k < K/2+1; k++) {
			arr[k] = 1;
		}
		
		PropertiesState initialState = new PropertiesState(arr);
		
		Problem1 instance = new Problem1(initialState, N);
		instance.solve();
		instance.getSolver().setPrintValueFunction(true);
		instance.printSolution();
	}

}
