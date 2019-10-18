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
class SepHierarchyCC extends NearestNeighborDS {
	static final private boolean DBG = false;
	
    Graph graph;

    private static int baseCaseCutoff = 8;    
	private boolean isBaseCase() {
		return graph.numNodes <= baseCaseCutoff;
	}
	
	private ArrayList<HashSet<Node>> nodes;
	private HashMap<Node,Separator> nodesS;
	private HashSet<Node> sites;

	private ArrayList<Separator> separators;
	
    private ArrayList<SepHierarchyCC> child;
	private ArrayList<ArrayList<Node>> child2G;
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
		String res = prefix+String.valueOf(nodesS.size())+"("+Math.sqrt(graph.numNodes)+") "+nodes.size()+"\n";
		for (SepHierarchyCC hie : child) {
			res += hie.toStringInternal(prefix+"    ");
		}
		return res;
	}
	
    SepHierarchyCC(Graph graph, HashSet<Node> sites) {
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

	SepHierarchyCC(SepHierarchyCC other, HashSet<Node> sites) {
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
		ArrayList<HashSet<Node>> childCenters = new ArrayList<>();
		for (HashSet<Node> nodeSet : nodes) {
			childCenters.add(new HashSet<Node>());
		}
		for (Node site : sites) {
			for (int K = 0; K<nodes.size(); K++) {
				if (nodes.get(K).contains(site)) {
					Node childSite = G2Child.get(site.id);
					assert childSite != null && childSite.id < other.child.get(K).graph.numNodes;
					childCenters.get(K).add(childSite);
				}
			}
		}
		child = new ArrayList<>();
		for (int K = 0; K<nodes.size(); K++) {
			child.add(new SepHierarchyCC(other.child.get(K), childCenters.get(K)));
		}
	}
	
    private void partitionNodes() {
        int n = graph.numNodes;
        ArrayList<Node> nodesX = nodesSortedBy('X');
        HashSet<Node> left = new HashSet<>(nodesX.subList(0, n/2));
        HashSet<Node> right = new HashSet<>(nodesX.subList(n/2, n));
        ArrayList<Edge> crossEdgesX = getCrossEdges(left, right);
		ArrayList<Node> nodesY = nodesSortedBy('Y');
        HashSet<Node> down = new HashSet<>(nodesY.subList(0, n/2));
        HashSet<Node> up = new HashSet<>(nodesY.subList(n/2, n));
		ArrayList<Edge> crossEdgesY = getCrossEdges(down, up);
		HashSet<Node> nodesA;
		ArrayList<Edge> crossEdges;
		if (crossEdgesX.size() < crossEdgesY.size()) {
			nodesA = left;
			crossEdges = crossEdgesX;
		} else {
			nodesA = down;
			crossEdges = crossEdgesY;
		}
		nodesS = new HashMap<>();
        for (Edge e : crossEdges) {
			Node u = e.first, v = e.second;
            if (!nodesS.containsKey(u) && !nodesS.containsKey(v)) {
				if (nodesA.contains(u)) {
					nodesS.put(u, null);
				} else {
					nodesS.put(v, null);
				}
			}
        }
		nodes = graph.reachableNodes(nodesS.keySet());
		if (DBG) for (HashSet<Node> nodeSet : nodes) System.err.println(nodeSet);
		for (HashSet<Node> nodeSet : nodes) assert !nodeSet.isEmpty();
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
		} else {
			assert dim == 'Y';
			Collections.sort(sortedNodes, new Comparator<Node>(){
				@Override
				public int compare(Node n1, Node n2){
					if(n1.y == n2.y) return 0;
					return n1.y < n2.y ? -1 : 1;
				}
			});		
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
		ArrayList<ArrayList<Node>> childNodes = new ArrayList<>();
		child2G = new ArrayList<>();
		G2Child = new ArrayList<>(graph.numNodes);
		for (int i = 0; i < graph.numNodes; i++) G2Child.add(null);
		
		for (int K = 0; K < nodes.size(); K++) {
			childNodes.add(new ArrayList<Node> (nodes.get(K).size()));
			child2G.add(new ArrayList<Node> (nodes.get(K).size()));
			int id = 0;
			for (Node u : nodes.get(K)) {
				Node uChild = new Node(id, u.x, u.y);
				childNodes.get(K).add(uChild);
				child2G.get(K).add(u);
				G2Child.set(u.id, uChild);
				id++;
			}
		}
		
		ArrayList<ArrayList<ArrayList<Edge>>> childAdjList = new ArrayList<>();
		for (int K = 0; K < nodes.size(); K++) {
			childAdjList.add(new ArrayList<ArrayList<Edge>>(nodes.get(K).size()));
			for (int i = 0; i < nodes.get(K).size(); i++) {
				childAdjList.get(K).add(new ArrayList<Edge>());
			}
		}
		
		for (int K = 0; K < nodes.size(); K++) {
			for (Node u : nodes.get(K)) {
				Node uChild = G2Child.get(u.id);
				for (Edge e : graph.adjList.get(u.id)) {
					Node v = e.getOther(u);
					if (v.id < u.id) continue; //to process each edge only once
					if (nodes.get(K).contains(v)) {
						Node vChild = G2Child.get(v.id);
						assert vChild != null;
						Edge eChild = new Edge(uChild, vChild);
						childAdjList.get(K).get(uChild.id).add(eChild);
						childAdjList.get(K).get(vChild.id).add(eChild);
					}
				}
			}
		}

		ArrayList<Graph> GChild = new ArrayList<>();
		ArrayList<HashSet<Node>> childCenters = new ArrayList<>();
		for (int K = 0; K < nodes.size(); K++) {
			GChild.add(new Graph(childNodes.get(K), childAdjList.get(K)));
			childCenters.add(new HashSet<Node>());
		}
		
		for (Node site : sites) {
			for (int K = 0; K<nodes.size(); K++) {
				if (nodes.get(K).contains(site)) {
					Node childSite = G2Child.get(site.id);
					assert childSite != null && childSite.id < GChild.get(K).numNodes;
					childCenters.get(K).add(childSite);
					break;
				}
			}
		}
		child = new ArrayList<>();
		for (int K = 0; K<nodes.size(); K++) {
			child.add(new SepHierarchyCC(GChild.get(K), childCenters.get(K)));
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
		for (int K = 0; K < nodes.size(); K++) {
			if (nodes.get(K).contains(newSite)) {
				Node childSite = G2Child.get(newSite.id);
				assert childSite != null && childSite.id < child.get(K).graph.numNodes;
				child.get(K).add(childSite);
				assert child.get(K).contains(childSite);
				return;
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
		for (int K = 0; K < nodes.size(); K++) {
			if (nodes.get(K).contains(delSite)) {
				Node childSite = G2Child.get(delSite.id);
				assert childSite != null && childSite.id < child.get(K).graph.numNodes;
				assert child.get(K).contains(childSite);
				child.get(K).remove(childSite);
				return;
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
    
	//invariant: graph contains q and has at least one reachable site
    private NodeDist queryInternal(Node q) {
		if(DBG)System.err.println("looking for "+q+" in "+graph.numNodes+"-node graph");
		if (isBaseCase()) {
			if(DBG)System.err.print("base case: ");
			NodeDist cand = Dijkstra.nearestGoal(graph, q, sites);
			assert cand.node != null && contains(cand.node);
			if(DBG)System.err.println("return "+cand.node);
			return cand;
		}

		NodeDist cand1 = new NodeDist(null, Double.MAX_VALUE);
		for (int K = 0; K < nodes.size(); K++) {
			if (nodes.get(K).contains(q)) {
				if (!child.get(K).isEmpty()) {
					Node qChild = G2Child.get(q.id);
					assert qChild != null && qChild.id < child.get(K).graph.numNodes;
					cand1 = child.get(K).queryInternal(qChild);
					cand1.node = child2G.get(K).get(cand1.node.id);
				}
				break;
			}
		}
		if(DBG){
			if(cand1.node==null) System.err.println("cand1 is null");
			else System.err.println("cand1 is "+cand1.node+" at dist "+cand1.dist);
		}
		
		NodeDist cand2 = new NodeDist(null, Double.MAX_VALUE);
        for (Separator sep : separators) {
			NodeDist closest = sep.closestSite();
            double dist = sep.getDist(q)+closest.dist;
            if (dist < cand2.dist) {
                cand2.dist = dist;
                cand2.node = closest.node;
            }
        }
		assert cand2.node != null;
		if(DBG)System.err.println("cand2 is "+cand2.node+" at dist "+cand2.dist);
		
		NodeDist best;
		if (cand1.node == null) best = cand2;
		else best = cand1.dist < cand2.dist ? cand1 : cand2;;
		assert best.node != null;
		if (DBG) System.err.println("return "+best.node);
		return best;
		
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
    }
    
}

//optimizations:
//set base case size automatically