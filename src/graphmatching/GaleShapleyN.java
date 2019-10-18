/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.util.ArrayList;
import java.util.HashMap;


public class GaleShapleyN extends Matcher {
	ArrayList<Student> students;
	ArrayList<Hospital> hospitals;
	
	@Override
	String acronym() {return "GS_N";}

	@Override
	Integer[] solve(Graph G, ArrayList<Integer> centers) {
		initStudentsAndHospitals(G, centers);
		initPrefs(G);
		doProposals();
		Integer[] res = new Integer[students.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = students.get(i).hospital.node.id;
		}
		return res;
	}

	private class Student {
		Node node;
		Hospital hospital;
		ArrayList<Hospital> prefs;
		int prefInd;
		
		Student(Node node) {
			this.node = node;
			hospital = null;
			prefInd = 0;			
		}

		@Override
		public String toString() {
			return "{" + node + ", hosp=" + hospital.node.id + ", prefInd=" + prefInd + '}';
		}
		
		void initPrefs(ArrayList<Hospital> prefs) {
			this.prefs = prefs;
		}
		
		Hospital nextHospital() {
			Hospital res = prefs.get(prefInd);
			prefInd++;
			return res;
		}
	}
	
	private class Hospital {
		Node node;
		BinaryHeap<Student> students;
		double[] dists;
		int quota;
		
		Hospital(Node center, int quota) {
			this.node = center;
			students = new BinaryHeap<>();
			this.quota = quota;

		}

		@Override
		public String toString() {
			return "{" + node + ", quota=" + quota + '}';
		}
		
		private boolean prefers(Student student1, Student student2) {
			double d1 = dists[student1.node.id];
			double d2 = dists[student2.node.id];
			return d1 < d2;
		}
		
		private Student lastStudent() {
			return students.getMin();
		}

		boolean shouldAdd(Student student) {
			return quota > 0 || prefers(student, lastStudent());
		}
		
		Student add(Student student) {
			students.add(student, -dists[student.node.id]);
			student.hospital = this;
			if (quota > 0) {
				quota--;
				return null;
			}
			else {
				Student lastStudent = students.extractMin();
				lastStudent.hospital = null;
				return lastStudent;
			}
		}		

	}

	private void initStudentsAndHospitals(Graph G, ArrayList<Integer> centers) {
		students = new ArrayList<> (G.numNodes);
		for (Node node : G.nodes) {
			students.add(new Student(node));
		}
		hospitals = new ArrayList<> (centers.size());
		HashMap<Integer, Integer> quotas = getQuotas(G.numNodes, centers);
		for (Integer cId : centers) {
			hospitals.add(new Hospital(G.nodes.get(cId), quotas.get(cId)));
		}
	}
	
	private void initPrefs(Graph G) {
		ArrayList<BinaryHeap<Hospital>> heaps = new ArrayList<> (students.size());
		for (Student student : students) {
			heaps.add(new BinaryHeap<Hospital> ());
		}
		for (Hospital H : hospitals) {
			Dijkstra.run(G, H.node);
			H.dists = Dijkstra.dists;
			for (int i = 0; i < H.dists.length; i++) {
				if (H.dists[i] != Double.MAX_VALUE) {
					heaps.get(i).add(H, H.dists[i]);
				}
			}
		}
		for (int i = 0; i < students.size(); i++) {
			students.get(i).initPrefs(heaps.get(i).extractAllSorted());
		}
	}

	private void doProposals() {
		ArrayList<Student> unmatched = new ArrayList<>(students);
		while (!unmatched.isEmpty()) {
			Student student = unmatched.get(unmatched.size()-1);
			Hospital hospital = student.nextHospital();
			if (hospital.shouldAdd(student)) {
				unmatched.remove(unmatched.size()-1);
				Student discarded = hospital.add(student);
				if (discarded != null) {
					unmatched.add(discarded);
				}
			}
		}
	}

	
	//validation stuff
	
	boolean validateMatching(Graph G, ArrayList<Integer> centers, Integer[] matching) {
		initMatching(G, centers, matching);
		return validateMatching();
	}

	private boolean validateMatching() {
		for (Student student : students) {
			double d1 = student.hospital.dists[student.node.id];
			for (Hospital hospital : student.prefs) {
				double d2 = hospital.dists[student.node.id];
				if (d1 <= d2) break;
				if (hospital.prefers(student, hospital.lastStudent())) {
					if (hospital.lastStudent().hospital != hospital) {
						System.err.println("Inconsistent matching");
					}
					System.err.println(
"Student "+student+" is matched to hospital "+student.hospital+" at dist \n"+
d1+" but he prefers hospital\n"+
hospital+" at dist "+d2+" and that hospital \n"+
"prefers him to "+hospital.lastStudent()+" at dist "+
hospital.dists[hospital.lastStudent().node.id]);

					return false;
				}
			}
		}
		return true;
	}
	
	private Hospital findHospital(int cId) {
		for (Hospital hospital : hospitals) {
			if (hospital.node.id == cId) return hospital;
		}
		return null;
	}
	
	void initMatching(Graph G, ArrayList<Integer> centers, Integer[] matching) {
		for (Integer i : matching) {
			assert i != null : "matching malformed";
		}
		initStudentsAndHospitals(G, centers);
		initPrefs(G);
		for (int i = 0; i < matching.length; i++) {
			Student S = students.get(i);
			Hospital H = findHospital(matching[i]);
			H.students.add(S, -H.dists[S.node.id]);
			S.hospital = H;
			assert H.quota > 0 : "matching malformed";
			H.quota--;
		}
	}
	
}
