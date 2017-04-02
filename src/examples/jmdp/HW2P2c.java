package examples.jmdp;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;


public class HW2P2c extends FiniteMDP<PropertiesState, PropertiesAction> {

    int K;
    double R1;
    double R2;
    double g[]; 
    double f1[];
    double F1[];
    double f2[];
    double F2[];
   
    

    // Constructor
    public HW2P2c(int lastStage, States<PropertiesState> initSet, int K, double R1, double R2) {
        super(initSet, lastStage);
        this.K = K;
        this.R1 = R1;
        this.R2 = R2;
        initProbs();
    }
    // Build PMF and CCDF of arrival and service distribution
    
    private BigInteger factorial(int input){
		BigInteger output = BigInteger.ONE;
		while (input > 0){
			output = output.multiply(BigInteger.valueOf(input));
			input--;
		}		
		return output;
    }
    
    private double BinomialPMF(int k, int n, double p){
    	BigDecimal temp1 = new BigDecimal(factorial(k));
    	BigDecimal temp2 = new BigDecimal(factorial(n));
    	BigDecimal temp3 = new BigDecimal(factorial(n-k));
    	BigDecimal temp4 = temp1.multiply(temp3);
    	BigDecimal temp5 = temp2.divide(temp4);
    	BigDecimal temp6 = new BigDecimal(Math.pow(p, k));
    	BigDecimal temp7 = new BigDecimal(Math.pow(1-p, n-k));
    	BigDecimal temp8 = temp6.multiply(temp7);
    	BigDecimal temp9 = temp5.multiply(temp8);
    	double PMF = temp9.doubleValue();
    	return PMF;
    }
    
    
    private double poissonPMF(int k, double lambda){
    	double Numerator = Math.pow(lambda, k)* Math.exp(-lambda);
    	BigDecimal denominator = new BigDecimal(factorial(k));
    	BigDecimal temp = denominator.multiply(BigDecimal.valueOf(1/Numerator)) ;
    	double PMF = temp.doubleValue();
    	return 1/PMF;
    }
    
    private double poissonCCDF(int K, double lambda){
    	double CDF = 0.0;
    	for (int k = 0; k <= K; k++) { 
    		CDF += poissonPMF(k,lambda);
    	}
    	double CCDF = 1.0 - CDF;
    	return CCDF;
    }
    //  initialize transition probability matrix
    public void initProbs() {
    	g = new double[K+1];
		f1 = new double[K+1];
		F1 = new double[K+1];
		f2 = new double[K+1];
		F2 = new double[K+1];
    	for (int k = 0; k <= K; k++) {
            g[k]=BinomialPMF(k, 100, 0.5);
            f1[k]= poissonPMF(k,50.0);
            F1[k]= poissonCCDF(k,50.0);
            f2[k]= poissonPMF(k,10.0);
            F2[k]= poissonCCDF(k,10.0);
        }
	}

	@Override
    public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t) {
            return g[j.getProperty(0)];
    }
	
   
    // build expected immediate cost function
    @Override
    public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
    		int a1 = a.getProperty(0);
    		int a2 = a.getProperty(1);
    		double r = 0.0;
    		for (int k=0; k<=a1;k++)
    			r = r + R1*f1[k]*k;
    		for (int k=0; k<=a2;k++)
    			r = r + R2*f2[k]*k;
    		r = r+ R1*F1[a1]*a1 + R2*F2[a2]*a2 - a1 - a2;
    		return  -1*r;
    }
    
    // initialize final cost 
    @Override
    public double finalCost(PropertiesState i) {
        return 0.0;
    }
    // build feasible actions given state
    @Override
    public Actions<PropertiesAction> feasibleActions(PropertiesState i, int t) {
        ActionsSet<PropertiesAction> actionSet = new ActionsSet<PropertiesAction>();
        for (int a1 = 0; a1 <= i.getProperty(0); a1++) {
        	for (int a2 = 0; a2 <= i.getProperty(0)-a1; a2++) {
                actionSet.add(new PropertiesAction(new int[]{a1,a2}));
        	}
        }
        return actionSet;
    }
    
    //build reachable state given state and action
    @Override
    public States<PropertiesState> reachable(PropertiesState i, PropertiesAction a, int t) {
        StatesSet<PropertiesState> stSet = new StatesSet<PropertiesState>();
        for (int n = 0; n <= K; n++) {
            stSet.add(new PropertiesState(new int[]{n}));
        }
        return stSet;
    }

    /**
     * @param a Not used
     * @throws Exception
     */
    //  main function
    public static void main(String a[]) throws Exception {
    	long startime = System.nanoTime();
    	int N = 10;
    	int K = 100;
    	double R1 = 10.0;
    	double R2 = 30.0;
        StatesSet<PropertiesState> initSet = new StatesSet<PropertiesState>(new PropertiesState(new int[]{50}));
        HW2P2c pro = new HW2P2c(N, initSet, K, R1, R2);
        pro.solve();
        pro.getSolver().setPrintValueFunction(true);
        PrintWriter out = new PrintWriter( "sol_QueueContorl_3_jMDP.txt");
        long cpu_time = System.nanoTime()-startime;
        out.println("Liangliang Xu");
        out.println("Problem:	QueueControl_3");
        out.printf("Cpu Time:	%f s %n",cpu_time/Math.pow(10.0, 9.0));
        pro.printSolution(out);
        out.flush();
        out.close();

    }

}// class end
