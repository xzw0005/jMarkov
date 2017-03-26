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
	double[][] trainsitionProbability;
	
	// Constructor
	public Problem1(PropertiesState initialState, int N) {
		super(initialState, N);
		this.K = initialState.getNumProps();
		initProbs();
	}
	
	// transition probabilities
	private void initProbs() {
		trainsitionProbability = new double[3][3];
		for (int k = 0; k < K; k++) {
			trainsitionProbability[0][0] = 0.2;
			trainsitionProbability[0][1] = 0.8;
			trainsitionProbability[1][1] = 0.5;
			trainsitionProbability[1][2] = 0.5;
		}
	}
	
	@Override
	public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t) {
		int action = a.getProperty(0);
		int st = i.getProperty(action);
		int sp = j.getProperty(action);
		return trainsitionProbability[st][sp];
	}
	
	@Override
	public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
		int act = a.getProperty(0);
		int s = i.getProperty(act);
		if (s == 1) 
			return -Math.pow(act, 2) * 0.5;
		else
			return 0;			
	}
	
	@Override
	public double finalCost(PropertiesState i) {
		return 0;
	}
	
	@Override
	public Actions<PropertiesAction> feasibleActions(PropertiesState i, int t) {
		ActionsSet<PropertiesAction> actionSet = new ActionsSet<PropertiesAction>();
		for (int act = 0; act < K; act++)
			actionSet.add(new PropertiesAction(new int[]{act}));
		return actionSet;
	}
	
	@Override
	public States<PropertiesState> reachable(PropertiesState i, PropertiesAction a, int t) {
		StatesSet<PropertiesState> stSet = new StatesSet<PropertiesState>(i);
		int[] arr = i.getProperties();
		int act = a.getProperty(0);
		if (arr[act] < 2)
			arr[act] = arr[act] + 1;
		stSet.add(new PropertiesState(arr));
		return stSet;
	}
		
	public static void main(String a[]) throws Exception {
		int N = 8;
		int K = 4;
		PropertiesState s0 = new PropertiesState(new int[K]);
		Problem1 q1 = new Problem1(s0, N);
		q1.solve();
		q1.printSolution();
	}

}
