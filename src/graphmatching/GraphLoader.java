/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphmatching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Nil
 */
class GraphLoader {
	private static final String BASE_FOLDER = "graph_matching/graphs/";
	private static final String DIMACS_EXT = "tmp";
	private static final String CLEANED_EXT = "txt";
	private static final String INFO_EXT = "info";


	static String getFile(String graphName, String ext) {
		return BASE_FOLDER+graphName+"/"+graphName+"."+ext;
	}
	
	static Graph load(String graphName) throws IOException {
		if (!fileExists(getFile(graphName, CLEANED_EXT))) {
			createCleanedFile(graphName);
		}
		if (!fileExists(getFile(graphName, INFO_EXT))) {
			createInfoFile(graphName);
		}
		return loadGraphFile(getFile(graphName, CLEANED_EXT));
	}

	static String fileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
	}

	private static void createCleanedFile(String graphName) throws IOException {
		if (!fileExists(getFile(graphName, DIMACS_EXT))) {
			throw new RuntimeException("graph not found");
		}
		Graph G = loadGraphFile(getFile(graphName, DIMACS_EXT));
		G = G.biggestConnectedComponent();
		G.normalizeCoordinates();
		storeGraph(G, getFile(graphName, CLEANED_EXT));
	}

	private static Graph loadGraphFile(String fileName) throws IOException {
		ArrayList<Node> nodes;
		ArrayList<ArrayList<Edge>> adjList;

		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String currLine = reader.readLine();
		int numNodes = Integer.parseInt(currLine);
		nodes = new ArrayList<>(numNodes);
		for (int i = 0; i < numNodes; i++) {
			currLine = reader.readLine();
			String[] strNums = currLine.split("\\s");
			Integer id = Integer.parseInt(strNums[0]);
			double x = Double.parseDouble(strNums[1]);
			double y = Double.parseDouble(strNums[2]);
			nodes.add(new Node(id, x, y));
		}
		currLine = reader.readLine();
		int numEdges = Integer.parseInt(currLine);
		adjList = new ArrayList<>(numNodes);
		for (int i = 0; i < numNodes; i++) {
			adjList.add(new ArrayList<Edge>());
		}
		for (int i = 0; i < numEdges; i++) {
			currLine = reader.readLine();
			String[] strNums = currLine.split("\\s");
			int nodeId1 = Integer.parseInt(strNums[0]);
			int nodeId2 = Integer.parseInt(strNums[1]);
			Edge e = new Edge(nodes.get(nodeId1), nodes.get(nodeId2));
			adjList.get(nodeId1).add(e);
			adjList.get(nodeId2).add(e);
			if (fileExtension(fileName).equals(DIMACS_EXT)) {
				reader.readLine(); //skip line with edge properties
			}
		}
		return new Graph(nodes, adjList);
	}

	private static void storeGraph(Graph G, String fileName) throws IOException {
		File outFile = new File(fileName);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			writer.write(String.valueOf(G.numNodes));
			writer.newLine();
			for (Node n : G.nodes) {
				writer.write(n.id + " " + n.x + " " + n.y);
				writer.newLine();
			}
			writer.write(String.valueOf(G.getNumEdges()));
			writer.newLine();
			for (Node u : G.nodes) {
				for (Edge e : G.adjList.get(u.id)) {
					Node v = e.getOther(u);
					if (u.id < v.id) {
						writer.write(u.id + " " + v.id);
						writer.newLine();
					}
				}
			}
		}
	}

	private static boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists() && file.isFile();
	}
	
	private static void createInfoFile(String graphName) throws IOException {
		Graph G = loadGraphFile(getFile(graphName, CLEANED_EXT));
		String fileName = getFile(graphName, INFO_EXT);
		File outFile = new File(fileName);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			writer.write(graphName);
			writer.newLine();
			writer.write(String.valueOf(G.numNodes)+" nodes");
			writer.newLine();
			writer.write(String.valueOf(G.getNumEdges())+" edges");
			writer.newLine();
		}
	}



}
