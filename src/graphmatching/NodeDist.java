/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

/**
 *
 * @author Nil
 */
class NodeDist {
	Node node;
	double dist;
	NodeDist(Node n, double d) {
		node = n; dist = d;
	}

	@Override
	public String toString() {
		return "{" + node + "," + dist + '}';
	}
	
}
