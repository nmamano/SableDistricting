/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Nil
 */
class Graph {

	ArrayList<Node> nodes;
	ArrayList<ArrayList<Edge>> adjList;
	public int numNodes;

	Graph(ArrayList<Node> nodes, ArrayList<ArrayList<Edge>> adjList) {
		this.nodes = nodes;
		this.adjList = adjList;
		numNodes = nodes.size();
	}

	int getNumEdges() {
		int numEdges = 0;
		for (ArrayList<Edge> adjs : adjList) {
			numEdges += adjs.size();
		}
		return numEdges/2;
	}

	@Override
	public String toString() {
		return numNodes+" nodes:" + nodes + ", adjList=" + adjList;
	}

	Graph biggestConnectedComponent() {
		ArrayList<ArrayList<Node>> CCs = new ArrayList<>();
		HashSet<Node> visited = new HashSet<>();
		for (Node node : nodes) {
			if (!visited.contains(node)) {
				HashSet<Node> reachedNodes = rechableNodesFrom(node, visited);
				CCs.add(new ArrayList<>(reachedNodes));
			}
		}
		int maxSize = 0;
		ArrayList<Node> maxCC = null;
		for (ArrayList<Node> CC : CCs) {
			if (CC.size() > maxSize) {
				maxSize = CC.size();
				maxCC = CC;
			}
		}

		ArrayList<Node> nodesCC = new ArrayList<>(maxSize);
		HashMap<Node,Node> ccMap = new HashMap<>();
		int i = 0;
		for (Node node : maxCC) {
			Node nodeCC = new Node(i, node.x, node.y);
			i++;
			nodesCC.add(nodeCC);
			ccMap.put(node, nodeCC);
		}

		ArrayList<ArrayList<Edge>> adjListCC = new ArrayList<>();
		for (Node u : nodesCC) adjListCC.add(new ArrayList<Edge>());
		for (Node u : maxCC) {
			Node uCC = ccMap.get(u);
			for (Edge e : adjList.get(u.id)) {
				Node v = e.getOther(u);
				if (u.id < v.id) {
					Node vCC = ccMap.get(v);
					Edge eCC = new Edge(uCC, vCC);
					adjListCC.get(uCC.id).add(eCC);
					adjListCC.get(vCC.id).add(eCC);
				}
			}
		}
		return new Graph(nodesCC, adjListCC);
	}

	private HashSet<Node> rechableNodesFrom(Node s, HashSet<Node> visited) {
		ArrayList<Node> toVisit = new ArrayList<>();
		HashSet<Node> newlyVisited = new HashSet<>();
		toVisit.add(s);
		while (!toVisit.isEmpty()) {
			Node u = toVisit.get(toVisit.size()-1);
			toVisit.remove(toVisit.size()-1);
			visited.add(u);
			newlyVisited.add(u);
			for (Edge e : adjList.get(u.id)) {
				Node v = e.getOther(u);
				if (!visited.contains(v)) {
					toVisit.add(v);
				}
			}
		}
		return newlyVisited;
	}

	void normalizeCoordinates() {
		double smallestX = Double.MAX_VALUE, smallestY = Double.MAX_VALUE;
		for (Node node : nodes) {
			if (node.x < smallestX) smallestX = node.x;
			if (node.y < smallestY) smallestY = node.y;
		}
		for (Node node : nodes) {
			node.x -= smallestX;
			node.y -= smallestY;
		}
	}

	ArrayList<HashSet<Node>> reachableNodes(Set<Node> alreadyVisited) {
		ArrayList<HashSet<Node>> res = new ArrayList<>();
		HashSet<Node> visited = new HashSet<>(alreadyVisited);
		for (Node node : nodes) {
			if (!visited.contains(node)) {
				HashSet<Node> reachedNodes = rechableNodesFrom(node, visited);
				res.add(reachedNodes);
			}
		}
		return res;
	}
}
