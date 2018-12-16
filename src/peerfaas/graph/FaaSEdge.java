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

import java.util.logging.Logger;

public class FaaSEdge {
  private static Logger log =
      Logger.getLogger(FaaSEdge.class.getName());
  private static int edgeCount = 0;

  private double capacity;
  private double weight;
  private int id;

  public FaaSEdge() {
    capacity = 1.0;
    weight = 1.0;
    id = edgeCount++;
  }

  public FaaSEdge(double c, double w) {
    this();
    this.capacity = c;
    this.weight = w;
  }

  public int getID() { return id; }

  public double getCapacity() { return capacity; }
  public void setCapacity(double c) { capacity = c; }
  public double getWeight() { return weight; }
  public void setWeight(double w) { weight = w; }

  public String toString() {
    return "E" + id;
  }

}
