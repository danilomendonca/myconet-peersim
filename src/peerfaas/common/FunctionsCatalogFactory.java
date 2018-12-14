package peerfaas.common;

import peersim.core.CommonState;

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

    //TODO
    public FunctionsCatalog createCatalog(int capacity, int entropy){
        FunctionsCatalog fc = new FunctionsCatalogImpl();
        fc.setCapacity(capacity);
        double maxUtility = 0;
        for(int i = 0; i < entropy; i++){//TODO
            String functionName = "function" + i;
            double demand = CommonState.r.nextDouble() * 2; //TODO
            fc.updateDemand(functionName, demand);
            double utility = demand;
            if(utility > maxUtility) {
                maxUtility = utility;
            }
            fc.getUtilities().put(functionName, utility);
        }
        fc.normalizeUtilities(maxUtility);
        fc.updateShares(capacity);
        fc.printCatalog();
        return fc;
    }
}
