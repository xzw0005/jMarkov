package examples.jmdp;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.io.PrintWriter;

public class Problem2 extends FiniteMDP<PropertiesState, PropertiesAction> {

	int K;
	double R1, R2, g[], f1[], f2[], F1[], F2[];
	
	// Constructor
	public Problem2(int lastStage, States<PropertiesState> initSet, int K, double R1, double R2, char option) {
		super(initSet, lastStage);
		this.K = K;
		this.R1 = R1;
		this.R2 = R2;
		initProbs(option);
	}
	
	private void initProbs(char subprob) {
		g = new double[K+1];
		f1 = new double[K+1]; f2 = new double[K+1];
		F1 = new double[K+1]; F2 = new double[K+1];
		for (int k = 0; k <= K; k++) {
			if (subprob == 'a' || subprob == 'b')
				g[k] = uniformProbability(k, 0, K); 
			else if (subprob == 'c' || subprob == 'd') 
				g[k] = binomialProbability(k, K, 0.5);
			if (subprob == 'a' || subprob == 'c') {
				f1[k] = poissonProbability(k, 50);
				f2[k] = poissonProbability(k, 10);
				F1[k] = poissonFbar(k, 50);
				F2[k] = poissonFbar(k, 10);
			} else if (subprob == 'b' || subprob == 'd') {
				f1[k] = geometricProbability(k, 0.02);
				f2[k] = geometricProbability(k, 0.02);
				F1[k] = geometricFbar(k, 0.02);
				F2[k] = geometricFbar(k, 0.02);
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
		for (int k = 1; k <= a2; k++)
			r = r + R2 * f2[k] * k;
		r = r + R1 * F1[a1] * a1 + R2 * F2[a2] * a2 - a1 - a2;
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
	
	
	private double uniformProbability(int k, int a, int b) {
		return 1.0/(b - a + 1);
	}
	
	private double geometricProbability(int k, double p) {
		return Math.pow(1.0-p, k) * p;
	}
	
	private double geometricFbar(int k, double p) {
		return Math.pow(1.0-p, k+1);
	}
	
	private double binomialProbability(int k, int n, double p) {
		int coeff = (factorial(n).divide(factorial(n-k))).divide(factorial(k)).intValue();
		return coeff * Math.pow(p, k) * Math.pow(1-p, n-k);
	}
		
	private BigInteger factorial(int n) {
		BigInteger result = BigInteger.ONE;
		while (n > 0){
			result = result.multiply(BigInteger.valueOf(n));
			n--;
		}
		return result;
	}

	private double poissonProbability(int k, double lambda) {
		BigDecimal numerator = new BigDecimal(Math.exp(-lambda) * Math.pow(lambda, k));
		BigDecimal denominator = new BigDecimal(factorial(k));
		return (numerator.divide(denominator, MathContext.DECIMAL128)).doubleValue();
	}	
	
	private double poissonFbar(int k, double lambda) {
		double res = 1.0;
		for (int j = 0; j <= k; j++)
			res -= poissonProbability(j, lambda);
		return res;
	}
	
	
	public static void main(String argv[]) throws Exception {
		String PROBLEM = "QueueControl";
		char OPT = 'd';
		int N = 10, K = 100;
		double R1 = 10, R2 = 30;
		long startTime = System.nanoTime();
		StatesSet<PropertiesState> initSet = new StatesSet<PropertiesState>(new PropertiesState(new int[]{50}));
		Problem2 instance = new Problem2(N, initSet, K, R1, R2, OPT);
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
