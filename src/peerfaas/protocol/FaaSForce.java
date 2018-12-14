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

import peerfaas.common.FunctionsCatalog;
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
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        boolean updatedOurCatalog = checkOurCatalog(node, pid, getValue().getUtilities().keySet());
        if(updatedOurCatalog) {
            getValue().printCatalog();
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
                                new FunctionsCatalogMessage(getValue(), node),
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

            boolean updatedOurCatalog = checkOurCatalog(node, pid, getValue().getUtilities().keySet());
            if(updatedOurCatalog) {
                System.out.println("Sending updated catalog from Node " + node.getIndex() + " to Node " + peern.getIndex());
                getValue().printCatalog();
                ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                        send(
                                node,
                                peern,
                                new FunctionsCatalogMessage(getValue(), node),
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
        int capacity = getValue().getCapacity();
        Collection<Double> actualUtilities = new ArrayList<>(getValue().getUtilities().values());
        for(String fName : functions) {
            double updatedUtility = getUpdatedUtility(node, fName, pid);
            if(updatedUtility > maxUtility)
                maxUtility = updatedUtility;
            getValue().getUtilities().put(fName, updatedUtility);
        }
        getValue().normalizeUtilities(maxUtility);
        Collection<Double> newUtilities = new ArrayList<>(getValue().getUtilities().values());
        if(!newUtilities.equals(actualUtilities)) {
            getValue().updateShares(capacity);
            return true;
        }else
            return false;
    }

    private double getUpdatedUtility(Node node, String functionName, int pid){
        double baseUtility = getValue().getAverageDemand(functionName);
        getValue().resetDemand(functionName);
        double neighborsContribution = getNeighborsContribution(node, functionName, pid);
        double newUtility = baseUtility - neighborsContribution;
        return newUtility;
    }

    private double getNeighborsContribution(Node node, String functionName, int pid) {
        Linkable linkable =
                (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
        double neighborsContribution = 0;
        for(int i = 0; i < linkable.degree(); i++) {
            Node neighbor = linkable.getNeighbor(i);
            FaaSForce neighborForce = (FaaSForce) neighbor.getProtocol(pid);
            double latency = 10; //CommonState.r.nextDouble() * 100; //TODO
            double maxLatency = 100; //TODO
            double latencyAttenuation = 1 / Math.exp(latency/maxLatency);
            long share = neighborForce.getValue().getShares().getOrDefault(functionName, 0l);
            neighborsContribution += (share) * latencyAttenuation;
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

