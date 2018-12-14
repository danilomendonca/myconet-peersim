package peerfaas.common.catalog;

import peerfaas.common.AllocationSolver;
import peersim.util.IncrementalStats;

import java.util.*;

public class FunctionsCatalogImpl implements FunctionsCatalog{

    Map<String,Double> demands;
    Map<String,IncrementalStats> demandStats;
    Map<String,Double> utilities;
    Map<String,Long> shares;
    int capacity;

    public FunctionsCatalogImpl(){
        demands = new HashMap<>();
        utilities = new HashMap<>();
        shares = new HashMap<>();
        demandStats = new HashMap<>();
    }

    @Override
    public Map<String, Double> getDemands() {
        return demands;
    }

    @Override
    public void setDemands(Map<String, Double> value) {
        this.demands = value;
    }

    @Override
    public void updateDemand(String functionName, Double value) {
        if(!demandStats.containsKey(functionName))
            demandStats.put(functionName, new IncrementalStats());
        demands.put(functionName, value);
        demandStats.get(functionName).add(value);
    }

    @Override
    public Double getAverageDemand(String functionName) {
        if(demandStats.containsKey(functionName))
            return demandStats.get(functionName).getAverage();
        else
            throw new RuntimeException("Average for function " + functionName + " does not exist");
    }

    @Override
    public void resetDemand(String functionName) {
        demandStats.get(functionName).reset();
        demandStats.get(functionName).add(demands.get(functionName));
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    @Override
    public Map<String, Double> getUtilities() {
        return utilities;
    }

    @Override
    public void setUtilities(Map<String, Double> value) {
        this.utilities = value;
    }

    @Override
    public Map<String, Long> getShares() {
        return shares;
    }

    @Override
    public void setShares(Map<String, Long> value) {
        this.shares = value;
    }

    @Override
    public void normalizeUtilities(double maxUtility) {
        maxUtility = Math.max(1, maxUtility);
        for (String fName : getUtilities().keySet()) {
            double newUtility = getUtilities().get(fName);
            double normalizedUtility = newUtility / (maxUtility);
            getUtilities().put(fName, normalizedUtility);
        }
        setUtilities(sortByValue(getUtilities()));
    }

    public void updateShares(int capcity){
        long capacity = capcity;
        long availableCapacity = capacity;
        for (String fName : getUtilities().keySet()) {
            double demand = getAverageDemand(fName);
            long idealShare = AllocationSolver.getNextShareForDemand(demand);
            long givenShare = Math.min(availableCapacity, idealShare);
            availableCapacity -=  givenShare;
            getShares().put(fName, givenShare);
        }
    }

    public void printCatalog(){
        for(String fName : getUtilities().keySet())
            System.out.println(fName + " demand: " + getDemands().get(fName) + " utility: " + getUtilities().get(fName) + " share: " + getShares().get(fName));
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

}
