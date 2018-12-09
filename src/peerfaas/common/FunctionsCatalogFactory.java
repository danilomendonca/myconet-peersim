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
    public FunctionsCatalog createCatalog(){
        FunctionsCatalog fc = new FunctionsCatalogImpl();
        int entropy = 10;
        double maxUtility = 0;
        for(int i = 0; i < entropy; i++){//TODO
            String functionName = "function" + i;
            double demand = CommonState.r.nextDouble() * 5; //TODO
            fc.getDemands().put(functionName, demand);
            double utility = demand;
            if(utility > maxUtility) {
                maxUtility = utility;
            }
            fc.getUtilities().put(functionName, utility);
        }
        fc.normalizeUtilities(maxUtility);
        fc.updateShares();
        fc.printCatalog();
        return fc;
    }
}
