/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Nil
 */
class Dijkstra {
	static double[] dists;
	
	static void run(Graph G, Node s) {
		int n = G.numNodes;
        dists = new double[n];
        for (int i = 0; i < n; i++) {
			dists[i] = Double.MAX_VALUE;
        }
        dists[s.id] = 0.0;
        BinaryHeap<Node> Q = new BinaryHeap(G.nodes, dists);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
            for (Edge e : G.adjList.get(node.id)) {
                Node neighbor = e.getOther(node);
                double newDist = dists[node.id] + e.dist();
                if (newDist < dists[neighbor.id]) {
                    dists[neighbor.id] = newDist;
                    Q.reduceKey(neighbor, newDist);
                }
            }
        }
    } 

	static ArrayList<NodeDist> exploredNodes(Graph G, Node s) {
		int n = G.numNodes;
		ArrayList<NodeDist> res = new ArrayList<>(n);
        dists = new double[n];
        for (int i = 0; i < n; i++) {
			dists[i] = Double.MAX_VALUE;
        }
        dists[s.id] = 0.0;
        BinaryHeap<Node> Q = new BinaryHeap(G.nodes, dists);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
			res.add(new NodeDist(node, dists[node.id]));
            for (Edge e : G.adjList.get(node.id)) {
                Node neighbor = e.getOther(node);
                double newDist = dists[node.id] + e.dist();
                if (newDist < dists[neighbor.id]) {
                    dists[neighbor.id] = newDist;
                    Q.reduceKey(neighbor, newDist);
                }
            }
        }
		return res;
    } 

	/*
	Returns the closest node in 'targets' from 's'
	*/
	static NodeDist nearestGoal(Graph G, Node s, HashSet<Node> targets) {
		if (targets.isEmpty()) throw new RuntimeException("no target");
		int n = G.numNodes;
        dists = new double[n];
        for (int i = 0; i < n; i++) {
            dists[i] = Double.MAX_VALUE;
        }
        dists[s.id] = 0.0;
        BinaryHeap<Node> Q = new BinaryHeap(G.nodes, dists);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
			if (targets.contains(node)) {
				return new NodeDist(node, dists[node.id]);
			}
            for (Edge e : G.adjList.get(node.id)) {
                Node neighbor = e.getOther(node);
                double newDist = dists[node.id] + e.dist();
                if (newDist < dists[neighbor.id]) {
                    dists[neighbor.id] = newDist;
                    Q.reduceKey(neighbor, newDist);
                }
            }
        }
		//no target is reachable
		return new NodeDist(null, Double.MAX_VALUE);
	}
}
