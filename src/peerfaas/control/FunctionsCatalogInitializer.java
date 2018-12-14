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

import peerfaas.common.catalog.FunctionsCatalogFactory;
import peerfaas.protocol.FunctionsCatalogHolder;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.vector.SingleValue;

/**
 * Initialize an aggregation protocol using a peak distribution; only one peak
 * is allowed. Note that any protocol implementing
 * {@link SingleValue} can be initialized by this component.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.12 $
 */
public class FunctionsCatalogInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------


    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The number of distinct functions in the system.
     *
     * @config
     */
    private static final String PAR_ENTROPY = "entropy";

    /**
     * The capacity available to this catalog.
     *
     * @config
     */
    private static final String PAR_CAPACITY = "capacity";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /** Protocol identifier; obtained from config property {@link #PAR_ENTROPY}. */
    private final int entropy;

    /** Protocol identifier; obtained from config property {@link #PAR_CAPACITY}. */
    private final int capacity;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public FunctionsCatalogInitializer(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        entropy = Configuration.getInt(prefix + "." + PAR_ENTROPY);
        capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * TODO
    * @return always false
    */
    public boolean execute() {
        for(int i=0; i<Network.size(); ++i) {
            Node node = Network.get(i);
            FunctionsCatalogHolder prot = (FunctionsCatalogHolder) node.getProtocol(pid);
            FunctionsCatalogFactory factory = FunctionsCatalogFactory.getInstance();
            Linkable linkable =
                    (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
            prot.setValue(factory.createCatalog(node, capacity, entropy, pid));
        }
        return false;
    }
}
