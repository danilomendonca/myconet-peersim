package peerfaas.common.catalog;

import peerfaas.common.random.RandomDemand;
import peersim.core.Node;

public class FunctionsCatalogFactory {

    static FunctionsCatalogFactory instance = null;
    public static FunctionsCatalogFactory getInstance(){
        if(instance != null)
            return instance;
        else{
            instance = new FunctionsCatalogFactory();
            return instance;
        }

    }

    private final RandomDemand randomDemand = new RandomDemand();

    //TODO
    public FunctionsCatalog createCatalog(Node node, int capacity, int entropy, int pid){
        FunctionsCatalog fc = new FunctionsCatalogImpl();
        fc.setCapacity(capacity);
        double maxUtility = 0;
        for(int i = 0; i < entropy; i++){
            String functionName = "function" + i;
            maxUtility = assignUtility(fc, functionName, 1);
        }
        fc.normalizeUtilities(maxUtility);
        fc.updateShares(node, capacity, pid);
        fc.printCatalog();
        return fc;
    }

    private double assignUtility(FunctionsCatalog fc, String functionName, double baseDemand) {
        double initialDemand = randomDemand.initialDemand(baseDemand);
        double maxUtility = 0;
        fc.updateDemand(functionName, initialDemand);
        double utility = initialDemand; //AllocationSolver.getIdealShareForDemand(initialDemand);
        if(utility > maxUtility) {
            maxUtility = utility;
        }
        fc.getUtilities().put(functionName, utility);
        return maxUtility;
    }
}
