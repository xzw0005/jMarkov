package examples.jmdp;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;
import java.io.PrintWriter;
//import java.math.BigInteger;
//import java.math.BigDecimal;
//import java.math.MathContext;

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
	
//	private void initProbs(char option) {
//		g = new double[K+1];
//		f1 = new double[K+1]; f2 = new double[K+1];
//		F1bar = new double[K+1]; F2bar = new double[K+1];
//		for (int k = 0; k <= K; k++) {
//			if (option == 'a' || option == 'b')
//				g[k] = discreteUniformPmf(k, 0, K); 
//			else if (option == 'c' || option == 'd') 
//				g[k] = binomialPmf(k, K, 0.5);
//			if (option == 'a' || option == 'c') {
//				f1[k] = poissonPmf(k, 50);
//				f2[k] = poissonPmf(k, 10);
//				F1bar[k] = poissonTail(k, 50);
//				F2bar[k] = poissonTail(k, 10);
//			} else if (option == 'b' || option == 'd') {
//				f1[k] = geometricPmf(k, 0.02);
//				f2[k] = geometricPmf(k, 0.02);
//				F1bar[k] = geometricTail(k, 0.02);
//				F2bar[k] = geometricTail(k, 0.02);
//			} 		
//		}
//	}

	private void initProbs(char option) {
		g = new double[K+1];
		f1 = new double[K+1]; f2 = new double[K+1];
		F1bar = new double[K+1]; F2bar = new double[K+1];

		UniformIntegerDistribution unif = new UniformIntegerDistribution(0, 101);
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
	
//	private double discreteUniformPmf(int k, int a, int b) {
//		int n = b - a + 1;
//		return 1.0/n;
//	}
//	
//	private BigInteger factorial(int n) {
////		if (n == 0)
////			return 1;
////		else
////			return factorial(n-1) * n; 
//		BigInteger result = BigInteger.ONE;
//		while (n > 0){
//			result = result.multiply(BigInteger.valueOf(n));
//			n--;
//		}		
//		return result;
//	}
//	
//	private double poissonPmf(int k, double lambda) {
//		BigDecimal numerator = new BigDecimal(Math.exp(-lambda) * Math.pow(lambda, k));
//		//int denominator = factorial(k).intValue();
//		BigDecimal denominator = new BigDecimal(factorial(k));
//		BigDecimal res = numerator.divide(denominator, MathContext.DECIMAL128);
//		return res.doubleValue();
//	}
//	
//	private double poissonTail(int k, double lambda) {
//		double cdf = 0.0;
//		for (int j = 0; j <= k; j++)
//			cdf += poissonPmf(j, lambda);
//		return 1.0 - cdf;
//	}
//	
//	private double geometricPmf(int k, double p) {
//		return Math.pow(1.0-p, k) * p;
//	}
//	
//	private double geometricTail(int k, double p) {
//		return Math.pow(1.0-p, k+1);
//	}
//	
//	private double binomialPmf(int k, int n, double p) {
//		int coeff = (factorial(n).divide(factorial(n-k))).divide(factorial(k)).intValue();
//		return coeff * Math.pow(p, k) * Math.pow(1-p, n-k);		
//	}
//	
//	private double binomialTail(int k, int n, double p) {
//		double ret = 0.0;
//		while (k < n) {
//			ret += binomialPmf(k+1, n, p);
//			k++;
//		}
//		return ret;
//	}

	public static void main(String argv[]) throws Exception {
		String PROBLEM = "QueueControl";
		char OPT = 'a';
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
