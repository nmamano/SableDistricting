/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class GaleShapleyC extends Matcher {
	ArrayList<Student> students;
	ArrayList<Hospital> hospitals;
	
	ArrayList<Hospital> unmatched;
	
	@Override
	String acronym() {return "GS_C";}

	@Override
	Integer[] solve(Graph G, ArrayList<Integer> centers) {
		initStudentsAndHospitals(G, centers);
		doProposals();
		Integer[] res = new Integer[students.size()];
		for (int i = 0; i < res.length; i++) {
			Hospital H = hospitals.get(students.get(i).hospitalId);
			res[i] = H.node.id;
		}
		return res;
	}

	private class Student {
		Node node;
		int hospitalId;
		double[] dists;	
		Student(Node node, int k) {
			this.node = node;
			hospitalId = -1;
			dists = new double[k];
		}
		
		boolean shouldAccept(Hospital H) {
			if (hospitalId == -1) {
				return true;
			}
			return dists[H.id] < dists[hospitalId];
		}
	}
	
	private class Hospital {
		int id;
		Node node;
		HashSet<Integer> studentIds;
		int quota;
		ArrayList<Student> prefs;
		int prefInd;
	
		Hospital(int id, Node center, int quota, int n) {
			this.id = id;
			this.node = center;
			studentIds = new HashSet<>(quota);
			this.quota = quota;
			prefs = new ArrayList<>(n);
			prefInd = 0;			
		}

		Student nextStudent() {
			Student res = prefs.get(prefInd);
			prefInd++;
			return res;
		}
	}

	private void initStudentsAndHospitals(Graph G, ArrayList<Integer> centers) {
		int n = G.numNodes;
		int k = centers.size();
		students = new ArrayList<> (n);
		for (Node node : G.nodes) {
			students.add(new Student(node, k));
		}
		
		hospitals = new ArrayList<> (k);
		HashMap<Integer, Integer> quotas = getQuotas(n, centers);
		int i = 0;
		for (Integer cId : centers) {
			Hospital H = new Hospital(i, G.nodes.get(cId), quotas.get(cId), n);
			hospitals.add(H);
			for (NodeDist nd : Dijkstra.exploredNodes(G, H.node)) {
				Student S = students.get(nd.node.id);
				H.prefs.add(S);
				S.dists[i] = nd.dist;
			}
			i++;
		}
	}

	private void doProposals() {
		unmatched = new ArrayList<>(hospitals);
		while (!unmatched.isEmpty()) {
			Hospital H = unmatched.get(unmatched.size()-1);
			Student S = H.nextStudent();
			if (S.shouldAccept(H)) {
				match(S, H);
			}
		}
	}

	void match(Student S, Hospital H) {
		int oldHospId = S.hospitalId;
		S.hospitalId = H.id;
		H.studentIds.add(S.node.id);
		H.quota -= 1;
		if (H.quota == 0) unmatched.remove(unmatched.size()-1);

		if (oldHospId != -1) {
			Hospital oldH = hospitals.get(oldHospId);
			oldH.studentIds.remove(S.node.id);
			oldH.quota += 1;
			if (oldH.quota == 1) unmatched.add(oldH);
		}
	}	

}
