package graphmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;


public class NNChain extends Matcher {
	static final private boolean DBG = false;
	boolean useCCs;
	
	@Override
	String acronym() {return useCCs ? "NNC_K" : "NNC_2";}
	
	NNChain(boolean useCCs) {
		this.useCCs = useCCs;
	}
	
	@Override
    Integer[] solve(Graph G, ArrayList<Integer> centers) {
		int n = G.numNodes;
		if(DBG)System.err.println("n: "+n);
		int k = centers.size();
		if(DBG)System.err.println("k: "+k);
		Integer[] res = new Integer[n];
		HashSet<Node> nodeSet = new HashSet<>(G.nodes);
		HashSet<Node> centerSet = new HashSet<>();
		for (Integer cId : centers) {
			centerSet.add(G.nodes.get(cId));
		}
		if(DBG && centerSet.size() < 10)System.err.println("centerSet: "+centerSet);
		HashMap<Integer, Integer> quotas = getQuotas(n, centers);
		if(DBG && quotas.size() < 10)System.err.println("quotas: "+quotas);
		NearestNeighborDS NNNodes;
		if (useCCs) NNNodes = new SepHierarchyCC(G, nodeSet);
		else NNNodes = new SepHierarchy(G, nodeSet);
//		System.err.println("============");
//		System.err.println(NNNodes);
//		System.err.println("============");
		if(DBG)System.err.println("NNNodes sites: "+NNNodes.size());
		if(DBG && NNNodes.numNodes() < 10)System.err.println("NNNnodes hierarchy: "+NNNodes);
		NearestNeighborDS NNCenters;
		if (useCCs) NNCenters = new SepHierarchyCC((SepHierarchyCC) NNNodes, centerSet);
		else NNCenters = new SepHierarchy((SepHierarchy) NNNodes, centerSet);
		if(DBG)System.err.println("NNCenters sites: "+NNCenters.size());
		if(DBG && NNCenters.size() < 10)System.err.println("NNCenters: "+NNCenters.siteSet());
		if(DBG && NNCenters.numNodes() < 10)System.err.println("NNCenters hierarchy: "+NNCenters);
		LinkedSet<Node> unmatchedCenters = new LinkedSet<>(centerSet);
		if(DBG && unmatchedCenters.size() < 10)System.err.println("unmatchedCenters: "+unmatchedCenters);
		Stack<Individual> S = new Stack<>();
		HashSet<Individual> indsInS = new HashSet<>();
		int iter = 0;
		while (!unmatchedCenters.isEmpty()) {
			if(DBG)System.err.println("iter "+iter);
			iter++;
			if(DBG && S.size() < 10)System.err.println("S: "+S);
			if (S.isEmpty()) {
				Individual ind = new Individual(unmatchedCenters.getAny(), true);
				S.add(ind);
				indsInS.add(ind);
				continue;
			}
			Individual p, q;
			Node center, node;
			p = S.peek();
			if (p.isCenter) {
				center = p.node;
				node = NNNodes.query(center);
				assert node != null;
				q = new Individual(node, false);
			} else {
				node = p.node;
				center = NNCenters.query(node);
				assert center != null;
				assert centerSet.contains(center);
				assert unmatchedCenters.contains(center);
				q = new Individual(center, true);
			}
			if (indsInS.contains(q)) {
				assert node.id < res.length : "out of bounds "+node.id+", "+res.length;
				res[node.id] = center.id;
				assert quotas.containsKey(center.id) : "no center "+center+" in "+centers;
				int quota = quotas.get(center.id);
				quota--;
				quotas.put(center.id, quota);

				NNNodes.remove(node);
				if (quota == 0) {
					NNCenters.remove(center);
					unmatchedCenters.remove(center);
				}				
				
				S.pop();
				indsInS.remove(p);
				if (!q.isCenter || quota == 0) {
					S.pop();
					indsInS.remove(q);
				}
			} else {
				S.add(q);
				indsInS.add(q);
			}
		}
		return res;
    }

	class Individual {
	
		@Override
		public String toString() {
			if (isCenter) return "center("+node+")";
			else return "node("+node+")";
		}
		
		Node node;
		boolean isCenter;
		Individual(Node node, boolean isCenter) {
			this.node = node;
			this.isCenter = isCenter;
		}
		
		@Override
		public int hashCode() {
			int hash = 7;
			hash = 41 * hash + node.hashCode();
			hash = 41 * hash + (isCenter ? 1 : 0);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final Individual other = (Individual) obj;
			if (!Objects.equals(this.node, other.node)) {
				return false;
			}
			return this.isCenter == other.isCenter;
		}
	}
	
}
