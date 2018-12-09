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

package peerfaas.protocol;

import peerfaas.common.FunctionsCatalog;
import peerfaas.common.FunctionsCatalogImpl;
import peersim.core.Protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * The task of this protocol is to store a single double value and make it
 * available through the {@link FunctionsCatalog} interface.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.6 $
 */
public class FunctionsCatalogHolder
implements Protocol
{

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------
	
/** Value held by this protocol */
protected FunctionsCatalog value = new FunctionsCatalogImpl();
	

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Does nothing.
 */
public FunctionsCatalogHolder(String prefix)
{
}

//--------------------------------------------------------------------------

/**
 * Clones the value holder.
 */
//TODO
public Object clone()
{
	FunctionsCatalogHolder svh=null;
	try { svh=(FunctionsCatalogHolder)super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	return svh;
}

//--------------------------------------------------------------------------
//methods
//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public FunctionsCatalog getValue()
{
	return value;
}

//--------------------------------------------------------------------------

/**
 * @inheritDoc
 */
public void setValue(FunctionsCatalog value)
{
	this.value = value;
}



}
