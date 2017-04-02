package examples.jmdp;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;
import java.io.PrintWriter;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class QueueControl extends FiniteMDP<PropertiesState, PropertiesAction> {

	int K;
	double R1, R2, g[], f1[], f2[], F1bar[], F2bar[];
	
	// Constructor
	public QueueControl(int lastStage, States<PropertiesState> initSet, int K, double R1, double R2, char option) {
		super(initSet, lastStage);
		this.K = K;
		this.R1 = R1;
		this.R2 = R2;
		initProbs(option);
	}
	
	private void initProbs(char option) {
		g = new double[K+1];
		f1 = new double[K+1]; f2 = new double[K+1];
		F1bar = new double[K+1]; F2bar = new double[K+1];

		UniformIntegerDistribution unif = new UniformIntegerDistribution(0, 100);
		PoissonDistribution pois50 = new PoissonDistribution(50);
		PoissonDistribution pois10 = new PoissonDistribution(10);
		BinomialDistribution binom = new BinomialDistribution(100, 0.5);
		GeometricDistribution geom = new GeometricDistribution(0.02);
		
		for (int k = 0; k <= K; k++) {
			if (option == 'a' || option == 'b'){
				g[k] = unif.probability(k);				
			}
				
			else if (option == 'c' || option == 'd'){
				g[k] = binom.probability(k);
			}
			if (option == 'a' || option == 'c') {
				f1[k] = pois50.probability(k);
				f2[k] = pois10.probability(k);
				F1bar[k] = 1.0 - pois50.cumulativeProbability(k);
				F2bar[k] = 1.0 - pois10.cumulativeProbability(k);				
			} else if (option == 'b' || option == 'd') {
				f1[k] = geom.probability(k);
				f2[k] = geom.probability(k);
				F1bar[k] = 1.0 - geom.cumulativeProbability(k);
				F2bar[k] = 1.0 - geom.cumulativeProbability(k);
			} 		
		}
	}	
	
	@Override
	public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t){
		return g[j.getProperty(0)];
	}
	
	@Override
	public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
		int a1 = a.getProperty(0);
		int a2 = a.getProperty(1);
		double r = 0;
		for (int k = 0; k <= a1; k++)
			r = r + R1 * f1[k] * k;
		for (int k = 0; k <= a2; k++)
			r = r + R2 * f2[k] * k;
		r = r + R1 * F1bar[a1] * a1 + R2 * F2bar[a2] * a2 - a1 - a2;
		return -1 * r;
	}
	
	@Override
	public double finalCost(PropertiesState i) {
		return 0.0;
	}
	
	@Override
	public Actions<PropertiesAction> feasibleActions(PropertiesState i, int t) {
		ActionsSet<PropertiesAction> actionSet = new ActionsSet<PropertiesAction>();
		for (int a1 = 0; a1 <= i.getProperty(0); a1++) {
			for (int a2 = 0; a2 <= i.getProperty(0) - a1; a2++) {
				actionSet.add(new PropertiesAction(new int[]{a1, a2}));
			}
		}
		return actionSet;
	}

	@Override
	public States<PropertiesState> reachable(PropertiesState i, PropertiesAction a, int t) {
		StatesSet<PropertiesState> stSet = new StatesSet<PropertiesState>();
		for (int k = 0; k <= K; k++)
			stSet.add(new PropertiesState(new int[]{k}));
		return stSet;
	}

	public static void main(String argv[]) throws Exception {
		String PROBLEM = "QueueControl";
		char OPT = 'd';
		int N = 10, K = 100;
		double R1 = 10, R2 = 30;
		long startTime = System.nanoTime();
		StatesSet<PropertiesState> initSet = new StatesSet<PropertiesState>(new PropertiesState(new int[]{50}));
		QueueControl instance = new QueueControl(N, initSet, K, R1, R2, OPT);
		instance.solve();
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println("Running time = " + elapsedTime/1e9 + " seconds.");
		
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
