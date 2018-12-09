package peerfaas.common;

import java.util.*;

public class FunctionsCatalogImpl implements FunctionsCatalog{

    Map<String,Double> utilities;
    Map<String,Double> demands;
    Map<String,Long> shares;

    public FunctionsCatalogImpl(){
        demands = new HashMap<>();
        utilities = new HashMap<>();
        shares = new HashMap<>();
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
        for (String fName : getUtilities().keySet()) {
            double newUtility = getUtilities().get(fName);
            double normalizedUtility = newUtility / (maxUtility);
            getUtilities().put(fName, normalizedUtility);
        }
        setUtilities(sortByValue(getUtilities()));
    }

    public void updateShares(){
        long capacity = 6; //TODO
        long availableCapacity = capacity;
        for (String fName : getUtilities().keySet()) {
            double utility = getUtilities().get(fName);
            long nextShare = 1;//AllocationSolver.getNextShareForFunction(utility, capacity);
            long actualShare = Math.min(nextShare, availableCapacity);
            getShares().put(fName, actualShare);
            availableCapacity -=  actualShare;
        }
    }

    public void printCatalog(){
        for(String fName : getUtilities().keySet())
            System.out.println(fName + " utility: " + getUtilities().get(fName) + " share: " + getShares().get(fName));
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
