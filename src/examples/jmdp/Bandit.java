package examples.jmdp;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;
import java.io.PrintWriter;

public class Bandit extends FiniteMDP<PropertiesState, PropertiesAction> {
	
	int N, K;
	double[][] P;
	
	// Constructor
	public Bandit(PropertiesState initialState, int N) {
		super(initialState, N);
		this.K = initialState.getNumProps();
		initProbs();
	}
	
	// transition probabilities
	private void initProbs() {
		P = new double[3][3];
		P[0][0] = 0.2;
		P[0][1] = 0.8;
		P[1][1] = 0.5;
		P[1][2] = 0.5;
		P[2][2] = 1.0;
	}
	
	@Override
	public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t) {
		int action = a.getProperty(0);
		for (int k = 0; k < K; k++) {
			if (k != action) {
				int sk = i.getProperty(k);
				int skprime = j.getProperty(k);
				if (sk != skprime)
					return 0.0;
			}
		}
		int st = i.getProperty(action);
		int sprime = j.getProperty(action);
		return P[st][sprime];
	}
	
	@Override
	public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
		int act = a.getProperty(0);
		int s = i.getProperty(act);
		if (s == 1) 
			return -Math.pow(act+1, 2) * 0.5;
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
		
	public static void main(String argv[]) throws Exception {
		String PROBLEM = "Bandit";		
		int N = 8;
		//int K = 4;
		long startTime = System.nanoTime();
		
		char OPT = '2';		
		int[] initStateArray = {0,0,1,1};
		PropertiesState initialState = new PropertiesState(initStateArray);
		Bandit instance = new Bandit(initialState, N);
		instance.solve();
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("Running time: " + elapsedTime/1e9 + " seconds.");
		
		String fileName = "sol_" + PROBLEM + '_' + OPT + "_jMarkov.txt";
		PrintWriter pw = new PrintWriter(fileName);
		pw.println("Xing Wang");
		pw.println("Problem: " + PROBLEM + '_' + OPT);
		pw.println("CPU Time: " + elapsedTime/1e9 + " seconds");
		pw.println("Expected Total Reward: ");
		pw.println("Optimal Policy: ");		
		instance.getSolver().setPrintValueFunction(true);
		instance.printSolution(pw);
		pw.close();
	}

}
