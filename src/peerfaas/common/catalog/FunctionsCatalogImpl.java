package peerfaas.common.catalog;

import peerfaas.common.AllocationSolver;
import peerfaas.protocol.FaaSForce;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.util.IncrementalStats;

import java.util.*;

public class FunctionsCatalogImpl implements FunctionsCatalog{

    Map<String,Double> demands;
    Map<String,IncrementalStats> demandStats;
    Map<String,Double> utilities;
    Map<String,Long> shares;
    Map<String, AbstractMap.SimpleEntry<Long, Double>> delegatedDemand;
    int capacity;

    public FunctionsCatalogImpl(){
        demands = new HashMap<>();
        utilities = new HashMap<>();
        shares = new HashMap<>();
        demandStats = new HashMap<>();
        delegatedDemand = new HashMap<>();
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
        delegatedDemand.clear();
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
    public void normalizeUtilities(double maxUtility) {
        maxUtility = Math.max(1, maxUtility);
        for (String fName : getUtilities().keySet()) {
            double newUtility = getUtilities().get(fName);
            double normalizedUtility = newUtility / (maxUtility);
            getUtilities().put(fName, normalizedUtility);
        }
        setUtilities(sortByValue(getUtilities()));
    }

    @Override
    public void updateShares(Node node, int capcity, int pid){
        long capacity = capcity;
        long availableCapacity = capacity;
        for (String fName : getUtilities().keySet()) {
            double demand = getAverageDemand(fName);
            double externalDemand = getExternalDemand(node, fName, pid);
            long idealShare = AllocationSolver.getIdealShareForDemand(demand + externalDemand);
            long givenShare = Math.min(availableCapacity, idealShare);
            availableCapacity -=  givenShare;
            getShares().put(fName, givenShare);
        }
    }

    private double getExternalDemand(Node node, String functionName, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        double externalDemand = 0;
        for(int i = 0; i < linkable.degree(); i++){
            Node neighbor = linkable.getNeighbor(i);
            if (!neighbor.isUp()) continue;
            // XXX quick and dirty handling of failures
            // (message would be lost anyway, we save time)
            FaaSForce neighborForce = (FaaSForce) neighbor.getProtocol(pid);
            double neighborDemand = getNeighborDemand(neighborForce, functionName);
            if(neighborDemand > 0) {
                externalDemand += neighborDemand;
                //TODO 1) the actual demand handled by this node depends on the allocation; now assuming all of it;
                //TODO 2) a message needs to be sent to the other node instead of updating its catalog directly
                AbstractMap.SimpleEntry<Long, Double> delegatedDemand = new AbstractMap.SimpleEntry<>(node.getID(), externalDemand);
                neighborForce.getValue().getDelegatedDemand().put(functionName, delegatedDemand);
            }
        }
        return externalDemand;
    }

    private double getNeighborDemand(FaaSForce neighborForce, String functionName) {
        if(!neighborForce.getValue().getDelegatedDemand().containsKey(functionName)) {
            double demand = neighborForce.getValue().getAverageDemand(functionName);
            long actualShare = neighborForce.getValue().getShares().getOrDefault(functionName, 0l);
            if (actualShare == 0) {
                return demand;
            }else
                return 0;
        }else
            return 0;
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
    public Map<String, AbstractMap.SimpleEntry<Long, Double>> getDelegatedDemand() {
        return delegatedDemand;
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
