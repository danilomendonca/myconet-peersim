package peerfaas.common;

import java.util.Map;

public class AllocationSolver {

    public static long getIdealAllocationForDemand(double demand){
        return (long) demand;//TODO
    }

    public static long getNextShareForFunction(double utility, long capacity){
        if(utility > 0)
            return Math.round(utility * capacity);
        else
            return 0;
    }
}
