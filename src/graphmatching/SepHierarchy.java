package graphmatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Nil
 */
class SepHierarchy extends NearestNeighborDS {
	static final private int A = 0;
	static final private int B = 1;
	static final private boolean DBG = false;
	
    Graph graph;

    private static int baseCaseCutoff = 50;    
	private boolean isBaseCase() {
		return graph.numNodes <= baseCaseCutoff;
	}
	
	private HashSet<Node>[] nodes;
	private HashMap<Node,Separator> nodesS;
	private HashSet<Node> sites;

	private ArrayList<Separator> separators;
	
    private SepHierarchy[] child;
	private ArrayList<Node>[] child2G;
	private ArrayList<Node> G2Child;

	@Override
	int size() {return sites.size();}
	@Override
	int numNodes() {return graph.numNodes;}
	@Override
	HashSet<Node> siteSet() {return sites;}
	@Override
	boolean contains(Node site) {return sites.contains(site);}
	@Override
	boolean isEmpty() {return sites.isEmpty();}
	@Override
	public String toString() {return toStringInternal("");}

	private String toStringInternal(String prefix) {
		if (isBaseCase()) return "";
		String res = prefix+nodes[A].size()+" "+nodesS.size()+
					 "("+Math.sqrt(graph.numNodes)+") "+nodes[B].size();
		return res+"\n"+child[A].toStringInternal(prefix+"    ")+
						child[B].toStringInternal(prefix+"    ");
	}
	
    SepHierarchy(Graph graph, HashSet<Node> sites) {
		if(DBG)System.err.println("init "+graph.numNodes+"-node graph");
		if (DBG && graph.numNodes < 30)System.err.println("with nodes "+graph.nodes);
		if(DBG && sites.size() < 10)System.err.println("with sites "+sites);
        this.graph = graph;
		for (Node site : sites) assert site.id < graph.numNodes; 
		this.sites = sites;
		if (DBG && isBaseCase()) System.err.println("base case");
        if (isBaseCase()) return;
		
		if(DBG)System.err.println("partition nodes");
		partitionNodes();		
		if(DBG)System.err.println("init separators");
		initSeparators();
		if(DBG)System.err.println("init children");
		initChildren();
    }	    

	SepHierarchy(SepHierarchy other, HashSet<Node> sites) {
		graph = other.graph;
		for (Node site : sites) assert site.id < graph.numNodes; 
		this.sites = sites;
		if (isBaseCase()) return;
		
		nodes = other.nodes;
		nodesS = new HashMap<>();
		separators = new ArrayList<>(other.separators.size());
		for (Separator sep : other.separators) {
			Separator newSep = new Separator(sep, sites);
			separators.add(newSep);
			nodesS.put(sep.node, newSep);
		}
		child2G = other.child2G;
		G2Child = other.G2Child;
		HashSet<Node>[] childCenters = new HashSet[2];
		for (int K = 0; K < 2; K++) {
			childCenters[K] = new HashSet<>();
		}
		for (Node site : sites) {
			for (int K = 0; K<2; K++) {
				if (nodes[K].contains(site)) {
					Node childSite = G2Child.get(site.id);
					assert childSite != null && childSite.id < other.child[K].graph.numNodes;
					childCenters[K].add(childSite);
				}
			}
		}
		child = new SepHierarchy[2];
		for (int K = 0; K<2; K++) {
			child[K] = new SepHierarchy(other.child[K], childCenters[K]);
		}
	}
	
    private void partitionNodes() {
        int n = graph.numNodes;
        ArrayList<Node> nodesX = nodesSortedBy('X');
        HashSet<Node> nodesAX = new HashSet<>(nodesX.subList(0, n/2));
        HashSet<Node> nodesBX = new HashSet<>(nodesX.subList(n/2, n));
        ArrayList<Edge> crossEdgesX = getCrossEdges(nodesAX, nodesBX);
		ArrayList<Node> nodesY = nodesSortedBy('Y');
        HashSet<Node> nodesAY = new HashSet<>(nodesY.subList(0, n/2));
        HashSet<Node> nodesBY = new HashSet<>(nodesY.subList(n/2, n));
		ArrayList<Edge> crossEdgesY = getCrossEdges(nodesAY, nodesBY);
		
		ArrayList<Edge> crossEdges;
		nodes = new HashSet[2];
		if (crossEdgesX.size() < crossEdgesY.size()) {
			nodes[A] = nodesAX;
			nodes[B] = nodesBX;
			crossEdges = crossEdgesX;
		} else {
			nodes[A] = nodesAY;
			nodes[B] = nodesBY;
			crossEdges = crossEdgesY;
		}
		
		nodesS = new HashMap<>();
        for (Edge e : crossEdges) {
			Node u = e.first, v = e.second;
            if (!nodesS.containsKey(u) && !nodesS.containsKey(v)) {
				if (nodes[A].contains(u)) {
					assert nodes[B].contains(v);
					nodesS.put(u, null);
					nodes[A].remove(u);
				} else {
					assert nodes[B].contains(u);
					assert nodes[A].contains(v);
					nodesS.put(v, null);
					nodes[A].remove(v);
				}
			}
        }
		assert !nodes[A].isEmpty() && !nodes[B].isEmpty();
		if (DBG && nodesS.size() < 20) System.err.println("nodesS: "+nodesS.keySet());
		if (DBG && nodes[A].size() < 20) System.err.println("nodesA: "+nodes[A]);
		if (DBG && nodes[B].size() < 20) System.err.println("nodesB: "+nodes[B]);
    }
            
    private ArrayList<Node> nodesSortedBy(char dim) {
        ArrayList<Node> sortedNodes = new ArrayList<> (graph.nodes);
		if (dim == 'X') {
			Collections.sort(sortedNodes, new Comparator<Node>(){
				@Override
				public int compare(Node n1, Node n2){
					if(n1.x == n2.x) return 0;
					return n1.x < n2.x ? -1 : 1;
				}
			});
			for (int i = 0; i < sortedNodes.size()-1; i++) {
				assert sortedNodes.get(i).x <= sortedNodes.get(i+1).x;
			}
		} else {
			assert dim == 'Y';
			Collections.sort(sortedNodes, new Comparator<Node>(){
				@Override
				public int compare(Node n1, Node n2){
					if(n1.y == n2.y) return 0;
					return n1.y < n2.y ? -1 : 1;
				}
			});		
			for (int i = 0; i < sortedNodes.size()-1; i++) {
				assert sortedNodes.get(i).y <= sortedNodes.get(i+1).y;
			}
		}
        return sortedNodes;
    }

    private ArrayList<Edge> getCrossEdges(HashSet<Node> left, HashSet<Node> right) {
        ArrayList<Edge> crossEdges = new ArrayList<>();
        for (Node u : graph.nodes) {
            for (Edge e : graph.adjList.get(u.id)) {
				Node v = e.getOther(u);
				if (v.id < u.id) continue; //to avoid repeated edges
				assert left.contains(u) || right.contains(u);
				assert left.contains(v) || right.contains(v);
                if ((left.contains(u) && right.contains(v)) ||
                    (left.contains(v) && right.contains(u))) {
                    crossEdges.add(e);
                }
            }
        }
        return crossEdges;
    }

	private void initSeparators() {
		separators = new ArrayList<>(sites.size());
		for (Node node : nodesS.keySet()) {
			Separator sep = new Separator(node, graph, sites);
			separators.add(sep);
			nodesS.put(node, sep);
		}
	}

	private void initChildren() {
		ArrayList<Node>[] childNodes = new ArrayList[2];
		child2G = new ArrayList[2];
		G2Child = new ArrayList<>(graph.numNodes);
		for (int i = 0; i < graph.numNodes; i++) G2Child.add(null);
		
		for (int K = 0; K < 2; K++) {
			childNodes[K] = new ArrayList<> (nodes[K].size());
			child2G[K] = new ArrayList<> (nodes[K].size());
			int id = 0;
			for (Node u : nodes[K]) {
				Node uChild = new Node(id, u.x, u.y);
				childNodes[K].add(uChild);
				child2G[K].add(u);
				G2Child.set(u.id, uChild);
				id++;
			}
		}
		
		for (Node u : graph.nodes) { //only asserts
			for (int K = 0; K < 2; K++) {
				if (nodes[K].contains(u)) {
					Node uChild = G2Child.get(u.id);
					assert uChild!=null && u.x==uChild.x && u.y==uChild.y;
					Node uParent = child2G[K].get(uChild.id);
					assert uParent != null && u.equals(uParent);
				}
			}
		}

		ArrayList<ArrayList<Edge>>[] childAdjList = new ArrayList[2];
		for (int K = 0; K < 2; K++) {
			childAdjList[K] = new ArrayList<>(nodes[K].size());
			for (int i = 0; i < nodes[K].size(); i++) {
				childAdjList[K].add(new ArrayList<Edge>());
			}
		}
		for (Node u : graph.nodes) {
			for (int K = 0; K < 2; K++) {
				if (nodes[K].contains(u)) {
					Node uChild = G2Child.get(u.id);
					assert uChild != null;
					for (Edge e : graph.adjList.get(u.id)) {
						Node v = e.getOther(u);
						if (v.id < u.id) continue; //to process each edge only once
						if (nodes[K].contains(v)) {
							Node vChild = G2Child.get(v.id);
							assert vChild != null;
							Edge eChild = new Edge(uChild, vChild);
							childAdjList[K].get(uChild.id).add(eChild);
							childAdjList[K].get(vChild.id).add(eChild);
						}
					}
				}
			}
		}
		
		Graph[] GChild = new Graph[2];
		HashSet<Node>[] childCenters = new HashSet[2];
		for (int K = 0; K < 2; K++) {
			GChild[K] = new Graph(childNodes[K], childAdjList[K]);
			childCenters[K] = new HashSet<>();
		}
		for (Node site : sites) {
			for (int K = 0; K<2; K++) {
				if (nodes[K].contains(site)) {
					Node childSite = G2Child.get(site.id);
					assert childSite != null && childSite.id < GChild[K].numNodes;
					childCenters[K].add(childSite);
				}
			}
		}
		child = new SepHierarchy[2];
		for (int K = 0; K<2; K++) {
			child[K] = new SepHierarchy(GChild[K], childCenters[K]);
		}
		
		for (Node site : siteSet()) { //only asserts
			for (int K = 0; K < 2; K++) {
				if (nodes[K].contains(site)) {
					assert child[K].contains(G2Child.get(site.id));
				}
			}
		}
		
		for (int K = 0; K < 2; K++) { //only asserts
			for (Node childSite : child[K].sites) {
				Node parentSite = child2G[K].get(childSite.id);
				assert sites.contains(parentSite);
			}
		}
	}

	@Override
    void add(Node newSite) {
		if (sites.contains(newSite)) return;
		sites.add(newSite);
		if (isBaseCase()) return;
		for (Separator sep : separators) {
			sep.addSite(newSite);
		}
		for (int K = 0; K < 2; K++) {
			if (nodes[K].contains(newSite)) {
				Node childSite = G2Child.get(newSite.id);
				assert childSite != null && childSite.id < child[K].graph.numNodes;
				child[K].add(childSite);
				assert child[K].contains(childSite);
			}
		}
	}
	
	@Override
    void remove(Node delSite) {
        if (!contains(delSite)) return;
		sites.remove(delSite);
		if (isBaseCase()) return;
		for (Separator sep : separators) {
			sep.remSite(delSite);
		}
		for (int K = 0; K < 2; K++) {
			if (nodes[K].contains(delSite)) {
				Node childSite = G2Child.get(delSite.id);
				assert childSite != null && childSite.id < child[K].graph.numNodes;
				assert child[K].contains(childSite);
				child[K].remove(childSite);				
			}
		}
	}
        
	@Override
    Node query(Node q) {
		if (isEmpty()) throw new RuntimeException("structure is empty");
        NodeDist answer = queryInternal(q);
		assert answer != null : "q is "+q+" and there are "+
				size()+" sites and "+graph.numNodes+" nodes";
        return answer.node;
    }
    
	//invariant: graph contains q and has at least one site
	//can return pair with null node and MAX dist if no site is not reachable
    private NodeDist queryInternal(Node q) {
		if(DBG)System.err.println("looking for "+q+" in "+graph.numNodes+"-node graph");
		if (isBaseCase()) {
			if(DBG)System.err.print("base case: ");
			NodeDist cand = Dijkstra.nearestGoal(graph, q, sites);
			assert cand.node == null || contains(cand.node);
			if(DBG){
				if(cand.node==null) System.err.println("return null");
				else System.err.println("return "+cand.node);
			}
			return cand;
		}
		
		assert nodes[A].contains(q) || nodes[B].contains(q) || nodesS.containsKey(q);

		NodeDist cand1 = new NodeDist(null, Double.MAX_VALUE);
		for (int K = 0; K < 2; K++) {
			if (nodes[K].contains(q) && !child[K].isEmpty()) {
				Node qChild = G2Child.get(q.id);
				assert qChild != null && qChild.id < child[K].graph.numNodes;
				cand1 = child[K].queryInternal(qChild);
				if (cand1.node != null) {
					cand1.node = child2G[K].get(cand1.node.id);
					assert cand1.node != null && sites.contains(cand1.node);
				}
			}
		}
		if(DBG){
			if(cand1.node==null) System.err.println("cand1 is null");
			else System.err.println("cand1 is "+cand1.node+" at dist "+cand1.dist);
		}
		
		NodeDist cand2 = new NodeDist(null, Double.MAX_VALUE);
        for (Separator sep : separators) {
			NodeDist closest = sep.closestSite();
			if (closest != null) {
				double dist = sep.getDist(q)+closest.dist;
				if (dist < cand2.dist) {
					cand2.dist = dist;
					cand2.node = closest.node;
				}
			}
        }
		if(DBG){
			if(cand2.node==null) System.err.println("cand2 is null");
			else System.err.println("cand2 is "+cand2.node+" at dist "+cand2.dist);
		}
		
		NodeDist best = bestCand(cand1, cand2);
		if (DBG) {
			if(best.node==null) System.err.println("return null");
			else System.err.println("return "+best.node);
		}
		
//		NodeDist actBest = Dijkstra.nearestGoal(graph,q,sites);
//		if (DBG && best.node != actBest.node && main.equals(best.dist, actBest.dist)) {
//			System.err.println("wrong query result: "+best+" ("+actBest+")");
//			if (sites.size() < 10) System.err.println("sites: "+sites);
//			if (graph.numNodes < 20) {
//				System.err.println("dists from q: ");
//				for (int i = 0; i < Dijkstra.dists.length; i++) {
//					System.err.print(i+":"+main.prettyStr(Dijkstra.dists[i])+" ");
//				}
//				System.err.println("\nseps:");
//				for (Separator sep : separators) {
//					System.err.println(sep);
//				}
//			}
//		}
//		assert best.node == actBest.node || main.equals(best.dist, actBest.dist) : "wrong query result "+best+" ("+actBest+")";
		return best;
    }
	
	private NodeDist bestCand(NodeDist cand1, NodeDist cand2) {
		if (cand1.node == null) return cand2;
		if (cand2.node == null) return cand1;
		return cand1.dist < cand2.dist ? cand1 : cand2;
	}
    
}

//optimizations:
//set base case size automatically