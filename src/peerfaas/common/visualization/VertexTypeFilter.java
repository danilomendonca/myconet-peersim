/* Copyright (c) 2014, Paul L. Snyder <paul@pataprogramming.com>,
 * Daniel Dubois, Nicolo Calcavecchia.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * Any later version. It may also be redistributed and/or modified under the
 * terms of the BSD 3-Clause License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package peerfaas.common.visualization;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import peerfaas.graph.FaaSEdge;
import peerfaas.graph.FaaSNode;

public class VertexTypeFilter extends VertexPredicateFilter<FaaSNode,FaaSEdge> {
  public int type;

  public VertexTypeFilter(int type) {
    super(new TypePredicate(type));
    this.type = type;
  }
}
