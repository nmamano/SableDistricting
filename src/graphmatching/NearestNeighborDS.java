/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.HashSet;

/**
 *
 * @author Nil
 */
abstract class NearestNeighborDS {

	abstract int numNodes();
	abstract int size();
	abstract boolean isEmpty();
	abstract HashSet<Node> siteSet();
	abstract boolean contains(Node node);
	abstract Node query(Node center);
	abstract void remove(Node node);
	abstract void add(Node node);
	
}
