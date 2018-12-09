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
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.Map;

/**
 * Event driven version of epidemic averaging.
 */
public class FaaSResources extends FunctionsCatalogHolder
        implements CDProtocol, EDProtocol {

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

    /**
     * @param prefix string prefix for config properties
     */
    public FaaSResources(String prefix) {
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
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if (linkable.degree() > 0) {
            Node peern = linkable.getNeighbor(
                    CommonState.r.nextInt(linkable.degree()));

            // XXX quick and dirty handling of failures
            // (message would be lost anyway, we save time)
            if (!peern.isUp()) return;

            ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                            node,
                            peern,
                            new FunctionsCatalogMessage(value, node),
                            pid);
        }
    }

//--------------------------------------------------------------------------

    /**
     * This is the standard method to define to process incoming messages.
     */
    public void processEvent(Node node, int pid, Object event) {

        FunctionsCatalogMessage aem = (FunctionsCatalogMessage) event;

        if (aem.sender != null)
            ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                            node,
                            aem.sender,
                            new FunctionsCatalogMessage(value, null),
                            pid);

        processMessage(aem);
    }

    private void processMessage(FunctionsCatalogMessage msg) {
    }


}

