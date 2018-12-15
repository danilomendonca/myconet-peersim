/*
 * Copyright (c) 2003 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peerfaas.protocol;

import peerfaas.common.AllocationSolver;
import peerfaas.common.catalog.FunctionsCatalog;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Event driven version of epidemic averaging.
 */
public class FaaSForce extends FunctionsCatalogHolder
        implements CDProtocol, EDProtocol {

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

    /**
     * @param prefix string prefix for config properties
     */
    public FaaSForce(String prefix) {
        super(prefix);
    }


//--------------------------------------------------------------------------
// methods
//--------------------------------------------------------------------------

    /**
     * This is the standard method the define periodic activity.
     * The frequency of execution of this method is defined by a
     * {@link peersim.edsim.CDScheduler} component in the configuration.
     */
    public void nextCycle(Node node, int pid) {
        sendUpdatedCatalog(node, pid);
    }

    private void sendUpdatedCatalog(Node node, int pid) {
        boolean updatedOurCatalog = checkOurCatalog(node, pid, getCatalog().getUtilities().keySet());
        if(updatedOurCatalog) {
            getCatalog().printCatalog();
            Linkable linkable =
                    (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
            for(int i = 0; i < linkable.degree(); i++){
                Node peer = linkable.getNeighbor(i);
                // XXX quick and dirty handling of failures
                // (message would be lost anyway, we save time)
                if (!peer.isUp()) return;

                System.out.println("Sending updated catalog from Node " + node.getIndex() + " to Node " + peer.getIndex());
                ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                        send(
                                node,
                                peer,
                                new FunctionsCatalogMessage(getCatalog(), node),
                                pid);
            }
        }
    }

    private void sendUpdatedCatalogToRandom(Node node, int pid) {
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if (linkable.degree() > 0) {
            Node peern = linkable.getNeighbor(
                    CommonState.r.nextInt(linkable.degree()));

            // XXX quick and dirty handling of failures
            // (message would be lost anyway, we save time)
            if (!peern.isUp()) return;

            boolean updatedOurCatalog = checkOurCatalog(node, pid, getCatalog().getUtilities().keySet());
            if(updatedOurCatalog) {
                System.out.println("Sending updated catalog from Node " + node.getIndex() + " to Node " + peern.getIndex());
                getCatalog().printCatalog();
                ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                        send(
                                node,
                                peern,
                                new FunctionsCatalogMessage(getCatalog(), node),
                                pid);
            }
        }
    }

//--------------------------------------------------------------------------

    /**
     * This is the standard method to define to process incoming messages.
     */
    public void processEvent(Node node, int pid, Object event) {
        FunctionsCatalogMessage aem = (FunctionsCatalogMessage) event;
        /*boolean updatedOurCatalog = processMessage(node, aem, pid);
        if (updatedOurCatalog) {
            System.out.println("Catalog from Node " + node.getIndex() + " updated");
            //sendUpdatedCatalog(node, pid);
        }*/
    }

    private boolean processMessage(Node node, FunctionsCatalogMessage msg, int pid) {
        //Node senderNode = msg.sender;
        FunctionsCatalog fc = msg.value;
        boolean updatedOurCatalog = checkOurCatalog(node, pid, fc.getShares().keySet());
        return updatedOurCatalog;
    }

    private boolean checkOurCatalog(Node node, int pid, Set<String> functions) {
        double maxUtility = 0;
        int capacity = getCatalog().getCapacity();
        Collection<Double> actualUtilities = new ArrayList<>(getCatalog().getUtilities().values());
        for(String fName : functions) {
            updateExternalDemand(node, fName, pid);
            double updatedUtility = getUpdatedUtility(node, fName, pid);
            if(updatedUtility > maxUtility)
                maxUtility = updatedUtility;
            getCatalog().getUtilities().put(fName, updatedUtility);
        }
        getCatalog().normalizeUtilities(maxUtility);
        Collection<Double> newUtilities = new ArrayList<>(getCatalog().getUtilities().values());
        if(!newUtilities.equals(actualUtilities)) {
            getCatalog().updateShares(node, capacity, pid);
            return true;
        }else
            return false;
    }

    private double getUpdatedUtility(Node node, String functionName, int pid){
        double internalDemand = getCatalog().getAverageDemand(functionName);
        double baseUtility = internalDemand; //AllocationSolver.getIdealShareForDemand(demand);
        double neighborsContribution = getNeighborsContribution(node, functionName, pid);
        double newUtility = baseUtility + neighborsContribution;
        getCatalog().resetDemand(functionName); //TODO NOT THE RIGHT PLACE
        return newUtility;
    }

    private void updateExternalDemand(Node node, String functionName, int pid) {
        Linkable linkable =
                (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
        double externalDemand = 0;
        for(int i = 0; i < linkable.degree(); i++) {
            Node neighbor = linkable.getNeighbor(i);
            FaaSForce neighborForce = (FaaSForce) neighbor.getProtocol(pid);
            double demand = neighborForce.getCatalog().getAverageDemand(functionName);
            externalDemand += demand;
        }
        getCatalog().getExternalDemand().put(functionName, externalDemand);
    }

    private double getNeighborsContribution(Node node, String functionName, int pid) {
        Linkable linkable =
                (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
        double neighborsContribution = 0;
        double myInternalDemand = getCatalog().getAverageDemand(functionName);
        double myExternalDemand = getCatalog().getExternalDemand().get(functionName);
        double myTotalDemand =  myInternalDemand + myExternalDemand;
        for(int i = 0; i < linkable.degree(); i++) {
            Node neighbor = linkable.getNeighbor(i);
            FaaSForce neighborForce = (FaaSForce) neighbor.getProtocol(pid);
            double latency = 10; //CommonState.r.nextDouble() * 100; //TODO
            double maxLatency = 100; //TODO
            double latencyAttenuation = 1 / Math.exp(latency/maxLatency);
            double demand = neighborForce.getCatalog().getAverageDemand(functionName);
            double externalDemand = neighborForce.getCatalog().getExternalDemand().getOrDefault(functionName, 0d);
            long actualShare = neighborForce.getCatalog().getShares().getOrDefault(functionName, 0l);
            if(myTotalDemand < demand + externalDemand ||
                    myTotalDemand == demand + externalDemand && myInternalDemand < demand)
                neighborsContribution -= actualShare * latencyAttenuation;
        }
        return neighborsContribution;
    }

}

//--------------------------------------------------------------------------
//--------------------------------------------------------------------------

/**
 * The type of a message. It contains a value of type double and the
 * sender node of type {@link peersim.core.Node}.
 */
class FunctionsCatalogMessage {

    final FunctionsCatalog value;
    /**
     * If not null,
     * this has to be answered, otherwise this is the answer.
     */
    final Node sender;

    public FunctionsCatalogMessage(FunctionsCatalog value, Node sender) {
        this.value = value;
        this.sender = sender;
    }
}

