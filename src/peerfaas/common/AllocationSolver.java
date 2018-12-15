package peerfaas.common;

public class AllocationSolver {

    public static long getIdealShareForDemand(double demand){
        if(demand > 0)
            return Math.max(1, Math.round(demand));
        else
            return 0;
    }
}
