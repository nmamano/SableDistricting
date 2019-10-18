/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import com.google.caliper.Runner;
/**
 *
 * @author Nil
 */
public class main {

	/**
	 * @param args the command line arguments
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {

		basicRun("biggrid", "CG", 6);
		basicRun("grid", "CG", 6);
		basicRun("smallgrid", "CG", 3);
		Benchmarker bm = new Benchmarker();
		bm.setup();
		bm.kExperiment();

	}

	static void basicRun(String graphName, String algo, int k) throws IOException{
		Graph G;
		System.out.println("Init graph "+graphName);
		G = GraphLoader.load(graphName);
		System.out.println("Adding "+k+" centers");
		ArrayList<Integer> centers = Matcher.randomCenters(G.numNodes, k);
		System.out.println("Running "+algo);
		Matcher matcher = Matcher.getMatcher(algo);
		Integer[] results = matcher.solve(G, centers);
		System.out.println("Validity: " + Matcher.validate(G, centers, results));
		storeResults(graphName, centers, results);
	}

	static void storeResults(String graphName, ArrayList<Integer> centers, Integer[] results) throws IOException {
		int k = centers.size();
		String fileName = GraphLoader.getFile(graphName, k+".out");
		File outFile = new File(fileName);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			for (int i = 0; i < centers.size(); i++) {
				if (i != 0) {
					writer.newLine();
				}
				writer.write("Center " + centers.get(i).toString());
			}
			for (Integer result : results) {
				writer.newLine();
				writer.write(result.toString());
			}
		}
	}
		
	static String prettyStr(double d) {
		if (d == Double.MAX_VALUE) return "INF";
		String s = String.valueOf(d);
		if (!s.contains(".")) return s;
		String sInt = s.substring(0, s.indexOf('.'));
		String sFrac = s.substring(s.indexOf('.')+1);
		boolean fracIsZero = true;
		for (int i = 0; i < sFrac.length(); i++) {
			if (sFrac.charAt(i) != '0') fracIsZero = false;
		}
		if (fracIsZero) return sInt;
		sFrac = sFrac.substring(0, Math.min(sFrac.length(), 6));
		return sInt+'.'+sFrac;
	}
	
	
	static boolean equals(double d1, double d2) {
		double larger = d2 > d1 ? d2 : d1;
		double smaller = d2 > d1 ? d1 : d2;
		double diff = larger - smaller;
		double epsilon = 1.19209290E-07;
		return diff <= epsilon;
	}
    
}