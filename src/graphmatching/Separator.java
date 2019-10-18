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
class Separator {
	Node node;
	double[] dists;
	BinaryHeap<Node> Q;

	Separator(Node node, Graph graph, HashSet<Node> sites) {
		this.node = node;
		Dijkstra.run(graph, node);
		dists = Dijkstra.dists;
		initQueue(sites);
	}

	Separator(Separator other, HashSet<Node> sites) {
		node = other.node;
		dists = other.dists;
		initQueue(sites);
	}

	private void initQueue(HashSet<Node> sites) {
		ArrayList<Node> qNodes = new ArrayList<>();
		ArrayList<Double> qDists = new ArrayList<>();
		for (Node u : sites) {
			if (dists[u.id] < Double.MAX_VALUE) {
				qNodes.add(u);
				qDists.add(dists[u.id]);
			}
		}
		Q = new BinaryHeap<>(qNodes, qDists);
	}

	void addSite(Node newSite) {
		if (!Q.contains(newSite)) {
			Q.add(newSite, dists[newSite.id]);
		}
	}

	void remSite(Node delSite) {
		Q.remove(delSite);
	}

	NodeDist closestSite() {
		if (Q.isEmpty()) return null;
		Node site = Q.getMin();
		double dist = dists[site.id];
		return new NodeDist(site, dist);
	}

	double getDist(Node q) {
		return dists[q.id];
	}

	@Override
	public String toString() {
		String res = "Separator " + node + "\ndists:";
		for (int i = 0; i < dists.length; i++) {
			res += i + ":" + main.prettyStr(dists[i]) + " ";
		}
		res += "\nQ:" + Q;
		return res;
	}
	
}
