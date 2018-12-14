/*
 * Copyright (c) 2003-2005 The BISON Project
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

package peerfaas.control;

import peerfaas.protocol.FaaSForce;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;
import peersim.vector.SingleValue;

/**
 * Print statistics for an average aggregation computation. Statistics printed
 * are defined by {@link IncrementalStats#toString}
 * 
 * @author Alberto Montresor
 * @version $Revision: 1.17 $
 */
public class DemandVariation implements Control {

    // /////////////////////////////////////////////////////////////////////
    // Constants
    // /////////////////////////////////////////////////////////////////////

    /**
     * Config parameter that determines the accuracy for standard deviation
     * before stopping the simulation. If not defined, a negative value is used
     * which makes sure the observer does not stop the simulation
     *
     * @config
     */
    private static final String PAR_DELTA = "accuracy";

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // /////////////////////////////////////////////////////////////////////
    // Fields
    // /////////////////////////////////////////////////////////////////////

    /**
     * The name of this observer in the configuration. Initialized by the
     * constructor parameter.
     */
    private final String name;

    /**
     * Accuracy for standard deviation used to stop the simulation; obtained
     * from config property {@link #PAR_DELTA}.
     */
    private final double accuracy;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates a new observer reading configuration parameters.
     */
    public DemandVariation(String name) {
        this.name = name;
        accuracy = Configuration.getDouble(name + "." + PAR_DELTA, -1);
        pid = Configuration.getPid(name + "." + PAR_PROT);
    }

    // /////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////

    /**
     * Print statistics for an average aggregation computation. Statistics
     * printed are defined by {@link IncrementalStats#toString}. The current
     * timestamp is also printed as a first field.
     *
     * @return if the standard deviation is less than the given
     *         {@value #PAR_DELTA}.
     */
    public boolean execute() {

        for (int i = 0; i < Network.size(); i++) {

            FaaSForce protocol = (FaaSForce) Network.get(i)
                    .getProtocol(pid);
            for(String functionName : protocol.getValue().getDemands().keySet()) {
                double actualDemand = protocol.getValue().getDemands().get(functionName);
                double nextDemand = Math.max(0, actualDemand);// + (CommonState.r.nextDouble() - 0.5) * 2);
                protocol.getValue().updateDemand(functionName, nextDemand);
            }
        }
        return false;
    }
}