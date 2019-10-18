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
import java.util.Arrays;

/**
 *
 * @author Nil
 */
public class Benchmarker {
	ArrayList<Matcher> matchers;
	int numRuns;
	int maxSize;
	ArrayList<String> graphs;
	boolean validate;
	boolean DBG;
	double maxSeconds;
	boolean randCenters;
	
	void setup() {
		numRuns = 10;
		maxSize = 1000*10000;
		maxSize = -1;
		maxSeconds = 1200;
		DBG = true;
		validate = true;
		randCenters = true;
		matchers = new ArrayList<>();
		matchers.add(new GaleShapleyN());
		matchers.add(new GaleShapleyC());
		matchers.add(new GreedyDijkstra());
		matchers.add(new NNChain(true));
		graphs = new ArrayList<>();
		graphs.add("smallgrid");
		graphs.add("grid");
		graphs.add("biggrid");
		// graphs.add("loadtest");
		// graphs.add("DC");
	}
	
	void kExperiment() throws IOException {
		Timer totalTimer = new Timer();
		if (DBG) System.err.println("start experiment");
		for (String graphName: graphs) {
			if (DBG) System.err.println("start graph "+graphName);
			String fileName = GraphLoader.getFile(graphName, "kexp");
			File outFile = new File(fileName);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
				writer.write("k");
				for (Matcher matcher : matchers) {
					writer.write(", "+matcher.acronym());
				}
				writer.newLine();
				
				Graph G = GraphLoader.load(graphName);
				int k = 2;
				while (k <= G.numNodes/2 && (maxSize == -1 || k*G.numNodes <= maxSize)) {
					if (DBG) System.err.println("    start k "+k);
					if (totalTimer.sinceStart() > maxSeconds) {
						break;
					}
					double[] avgDurations = new double[matchers.size()];
					for (int i = 0; i < matchers.size(); i++) {
						avgDurations[i] = 0;
					}
					for (int j = 0; j < numRuns; j++) {
						if (totalTimer.sinceStart() > maxSeconds) {
							break;
						}
						if (DBG) System.err.print("    init centers");
						ArrayList<Integer> centers;
						if (randCenters) centers = Matcher.randomCenters(G.numNodes, k);
						else centers = Matcher.deterministicCenters(G.numNodes, k);
						if (DBG && centers.size() <= 10) System.err.println(" "+centers);
						else System.err.println();
						Matcher mat = new GreedyDijkstra();
						Integer[] correctRes = null;
						if (validate) {
							if (DBG) System.err.print("    "+mat.acronym()+" result to compare to");
							correctRes = mat.solve(G, centers);
							if (DBG) {
								if (correctRes.length <= 10) System.err.println(" "+Arrays.toString(correctRes));
								else System.err.println(" ---");
							}
							if (!Matcher.validate(G, centers, correctRes)) {
								System.err.println("Unstable");
								return;
							}
						}
						int i = 0;
						for (Matcher matcher : matchers) {
							if (DBG) System.err.print("        "+k+" "+matcher.acronym());
							if (totalTimer.sinceStart() > maxSeconds) {
								break;
							}
							Timer T = new Timer();
							Integer[] results = matcher.solve(G, centers);
							T.stop();
							if (DBG) System.err.println(" "+T.getStr());
							if (DBG && results.length <= 10) System.err.println("        "+Arrays.toString(results));
							if (validate) {
								if (!Arrays.equals(results, correctRes)) {
									if (!Matcher.validate(G, centers, results)) {
										System.err.println("Unstable");
										return;
									}
								}
							}
							avgDurations[i] += T.getDuration();
							i++;
						}
					}
					for (int i = 0; i < matchers.size(); i++) {
						avgDurations[i] /= numRuns;
					}
					writer.write(String.valueOf(k));
					for (int i = 0; i < matchers.size(); i++) {
						writer.write(", "+main.prettyStr(avgDurations[i]));
					}
					writer.newLine();
					k *=2;
				}
				if (totalTimer.sinceStart() > maxSeconds) {
					System.err.println("Ran out of time");
				}
			}
		}
	}
}
