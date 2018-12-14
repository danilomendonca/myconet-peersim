package peerfaas.common;

public class AllocationSolver {

    public static long getIdealShareForDemand(double demand){
        return (long) Math.ceil(Math.max(0, demand));
    }
}
