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

package peerfaas.common;

import java.util.Map;

/**
* The implementor class has a single parameter. This interface
* provides access to that parameter.
*/
public interface FunctionsCatalog {

    public Map<String, Double> getDemands();

    public void setDemands(Map<String,Double> value);

    /**
    * Returns the value of the parameter hold by the implementor
    * of this interface.
    */
    public Map<String, Double> getUtilities();

    /**
    * Modifies the value of the parameter hold by the implementor
    * of this interface.
    */
    public void setUtilities(Map<String, Double> value);

    /**
    * Returns the value of the parameter hold by the implementor
    * of this interface.
    */
    public Map<String, Long> getShares();

    /**
    * Modifies the value of the parameter hold by the implementor
    * of this interface.
    */
    public void setShares(Map<String, Long> value);

    void updateShares();
    void normalizeUtilities(double maxUtility);
    void printCatalog();
}

