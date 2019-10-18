/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author Nil
 */
public abstract class Matcher {

    abstract Integer[] solve(Graph G, ArrayList<Integer> centers);
	
    static boolean validate(Graph G, ArrayList<Integer> centers, Integer[] matching) {
        GaleShapleyN gs = new GaleShapleyN();
		return gs.validateMatching(G, centers, matching);
    }
    
    static boolean validate2(Graph G, ArrayList<Integer> centers, Integer[] matching){
    	System.out.println(Arrays.toString(centers.toArray()));
    	for(Integer center: centers){
    		for(Node node: G.nodes){
    			Integer center2 = matching[node.id];
    			if(dist(node, G.nodes.get(center)) < dist(node, G.nodes.get(center2))){
    				for(Node node2: G.nodes){
    					if(matching[node2.id].equals(center)){
    						if(dist(node, G.nodes.get(center)) < dist(node2, G.nodes.get(center))){
    							System.out.println("Node: " + node.id + " Center: " + center + " is a better pair");
    							System.out.println("Distance between them: " + dist(node, G.nodes.get(center)));
    							System.out.println("Distance between current node and its center: " + dist(node, G.nodes.get(center2)));
    							System.out.println("Distance between new center and a node belongs to it: " + dist(node2, G.nodes.get(center)));
    							return false;
    						}
    					}
    				}
    			}
    		}
    	}
    	return true;
    }
	
    static double dist(Node first, Node second) {
        double xDiff = first.x - second.x;
        double yDiff = first.y - second.y;
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }
    
	HashMap<Integer, Integer> getQuotas(int n, ArrayList<Integer> centers) {
		int k = centers.size();
		int quota = n / k;
		int remPoints = n - k*quota;
		HashMap<Integer, Integer> res = new HashMap<>();
		for(int i=0; i<k; i++){
			if(remPoints > 0){
				res.put(centers.get(i), quota+1);
				remPoints--;
			}
			else{
				res.put(centers.get(i), quota);
			}
		}
		return res;
	}
	
	static ArrayList<Integer> randomCenters(int n, int k) {
		assert k <= n;
		Random R = new Random(System.currentTimeMillis());
		HashSet<Integer> centers = new HashSet<>(k);
		while (centers.size() < k) {
			centers.add(R.nextInt(n));
		}
		return new ArrayList<> (centers);
	}

	static ArrayList<Integer> deterministicCenters(int n, int k) {
		assert k <= n;
		ArrayList<Integer> res = new ArrayList<>(k);
		for (int i = n-1; i >= n-k; i--) {
			res.add(i);
		}
		return res;
	}

	static Matcher getMatcher(String acronym) {
		if (acronym.equals(new GreedyDijkstra().acronym())) {
			return new GreedyDijkstra();
		} else if (acronym.equals(new GaleShapleyN().acronym())) {
			return new GaleShapleyN();
		} else if (acronym.equals(new GaleShapleyC().acronym())) {
			return new GaleShapleyC();
		} else if (acronym.equals(new NNChain(false).acronym())) {
			return new NNChain(false);
		} else if (acronym.equals(new NNChain(true).acronym())) {
			return new NNChain(true);
		} else {
			throw new RuntimeException("matcher not found");
		}
	}
	
	abstract String acronym();
}
