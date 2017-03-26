package examples.jmdp;


import jmarkov.basic.Actions;
import jmarkov.basic.ActionsSet;
import jmarkov.basic.PropertiesAction;
import jmarkov.basic.PropertiesState;
import jmarkov.basic.States;
import jmarkov.basic.StatesSet;
import jmarkov.jmdp.FiniteMDP;


public class INSY7440_HW2 extends FiniteMDP<PropertiesState, PropertiesAction> {

    int K;
    double R1;
    double R2;
    double g[]; 
    double G[]; 

    // Constructor
    public INSY7440_HW2(int lastStage, States<PropertiesState> initSet, int K, double R1, double R2) {
        super(initSet, lastStage);
        this.K = K;
        this.R1 = R1;
        this.R2 = R2;
        initProbs();
    }

    private void initProbs() {
    	g = new double[K+1];
    	G = new double[K+1];
    	for (int k = 0; k <= K; k++) {
            g[k]=1.0/(K+1.0);
            G[k]=1.0-(k)/(K+1.0);
        }
	}

	@Override
    public double prob(PropertiesState i, PropertiesState j, PropertiesAction a, int t) {
            return g[j.getProperty(0)];
    }

    @Override
    public double immediateCost(PropertiesState i, PropertiesAction a, int t) {
    		int a1 = a.getProperty(0);
    		int a2 = a.getProperty(1);
    		double r=0;
    		for (int k=1; k<=a1-1;k++)
    			r = r + R1*g[k]*k;
    		for (int k=1; k<=a2-1;k++)
    			r = r + R2*g[k]*k;
    		r = r+ R1*G[a1]*a1 + R2*G[a2]*a2 - a1 - a2;
    		return  -1*r;
    }

    @Override
    public double finalCost(PropertiesState i) {
        return 0.0;
    }

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
    public static void main(String a[]) throws Exception {
    	int N = 10;
    	int K = 100;
    	double R1 = 2;
    	double R2 = 1.3;
        StatesSet<PropertiesState> initSet = new StatesSet<PropertiesState>(new PropertiesState(new int[]{K}));

        INSY7440_HW2 pro = new INSY7440_HW2(N, initSet, K, R1, R2);
        pro.solve();
        pro.printSolution();
    }

}// class end
