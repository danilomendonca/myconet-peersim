package peerfaas.common;

public class AllocationSolver {

    public static long getNextShareForDemand(double demand){
        return (long) Math.ceil(Math.max(0, demand));
    }
}
