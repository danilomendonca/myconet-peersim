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


package peerfaas.graph;

import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import fungus.HyphaData;
import org.apache.commons.collections15.Factory;
import peerfaas.common.visualization.VertexTypeFilter;

import java.util.*;
import java.util.logging.Logger;

public class FaaSGraph extends DirectedSparseGraph<FaaSNode,FaaSEdge> {
  private static List<VertexTypeFilter> filters =
      new ArrayList<VertexTypeFilter>();

  private static Logger log =
      Logger.getLogger(FaaSGraph.class.getName());

  public static VertexTypeFilter getFilter(int t) {
    log.fine("looking for filter " + t);
    // If predicates aren't created yet, create and cache
    while (t >= filters.size()) {
      filters.add(new VertexTypeFilter(filters.size()));
    }
    return filters.get(t);
  }

  // Get List of subgraphs; subgraph i consists only of nodes of type i
  public List<Graph<FaaSNode,FaaSEdge>> getTypeGraphs() {
    List<Graph<FaaSNode,FaaSEdge>> ret =
        new ArrayList<Graph<FaaSNode,FaaSEdge>>(HyphaData.numTypes);
    for (int t = 0; t < HyphaData.numTypes; t++) {
      VertexTypeFilter f = getFilter(t);
      ret.add(f.transform(this));
    }
    log.finer("Returning " + ret.size() + " type graphs");
    return ret;
  }

  public static Factory<Forest<FaaSNode,FaaSEdge>> typeForestFactory =
      new Factory<Forest<FaaSNode,FaaSEdge>>() {
        public Forest<FaaSNode,FaaSEdge> create() {
          return new DelegateForest<FaaSNode,FaaSEdge>();
        }
      };

  /*public static Factory<UndirectedGraph<MycoNode,MycoEdge>>
    undirectedGraphFactory =  new Factory<UndirectedGraph<MycoNode,MycoEdge>>() {
    public UndirectedGraph<MycoNode,MycoEdge> create() {
    return new UndirectedSparseMultigraph<MycoNode,MycoEdge>();
    }
    };

    public static Factory<MycoEdge> undirectedEdgeFactory =
    new Factory<MycoEdge>() {
    public MycoEdge create() {
    return new MycoEdge();
    }
    };

    public static UndirectedGraph<MycoNode,MycoEdge> toUndirected(MycoGraph g) {
    return DirectionTransformer.toUndirected(g,
    undirectedGraphFactory,
    undirectedEdgeFactory,
    false);
    }

    public UndirectedGraph<MycoNode,MycoEdge> toUndirected() {
    return toUndirected(this);
    }*/

  public static Forest<FaaSNode,FaaSEdge> getMinimumSpanningForest(Graph<FaaSNode, FaaSEdge> g) {
    //log.finer("Trying to find MST for " + g);
    MinimumSpanningForest<FaaSNode,FaaSEdge> msf =
        new MinimumSpanningForest<FaaSNode,FaaSEdge>(g,
                                                     typeForestFactory.create(),
                                                     null);
    return msf.getForest();
  }

  public Forest<FaaSNode,FaaSEdge> getMinimumSpanningForest() {
    return getMinimumSpanningForest(this);
  }

  public Set<Set<FaaSNode>> findConnectedComponents() {
    Set<Set<FaaSNode>> components = new HashSet<Set<FaaSNode>>();
    Set<FaaSNode> unseen = new HashSet<FaaSNode>(this.getVertices());
    Queue<FaaSNode> queue = new LinkedList<FaaSNode>();

    Set<FaaSNode> workingComponent = null;
    FaaSNode current;
    while ( (! unseen.isEmpty()) || (! queue.isEmpty()) ) {
      if (queue.isEmpty()) {
        // Queue an arbitary unvisited node
        FaaSNode n  = unseen.iterator().next();
        queue.offer(n);
        unseen.remove(n);
        // Start new component
        workingComponent = new HashSet<FaaSNode>();
        components.add(workingComponent);
      }
      current = queue.remove();
      workingComponent.add(current);
      for (FaaSEdge neighbor : current.getHyphaLink().getNeighbors()) {
        if (unseen.contains(neighbor)) {
          queue.offer(neighbor);
          unseen.remove(neighbor);
        }
      }
    }
    return components;
  }
}
