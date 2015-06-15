import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the PageRank of a graph.  The graph needs to be stored as a text file with 
 * each row containing exactly one edge.  Each edge needs to be represented as a space 
 * separated pair of vertex names.
 * 
 * The page rank constant BETA, which governs 1 - probability of "teleportation", is set to 0.85.  
 * Page rank is calculated until the L1 norm between iterations fall under a user specified limit. 
 * 
 * @author Alex Shum
 */
public class PageRank {
	private static final double BETA = 0.85;
	private double eps;   //page rank convergence
	private int numEdges; //number of edges
	private int numVert;  //number of vertices
	private int numIter;  //number of page rank iterations
	private Map<String, List<String>> AtoB; //out degree of vertices
	private Map<String, List<String>> BtoA; //in degree of vertices
	private Map<String, Double> pageRanks;  //page ranks of vertices
	private Set<String> nodeCounter;        //list of vertices
	
	/**
	 * Creates a new PageRank object.  This is used to find the pagerank
	 * of a graph represented as an edgelist in a text file.
	 * @param fileName Name of text file containing graph edge list.
	 * @param eps Convergence parameter for pagerank.
	 * @throws FileNotFoundException If text file containing graph cannot be found.
	 * @throws IOException If error reading a text file.
	 */
	public PageRank(String fileName, double eps) throws FileNotFoundException, IOException {
		this.eps = eps;
		numIter = 0;
		numEdges = 0;
		AtoB = new HashMap<String, List<String>>();
		BtoA = new HashMap<String, List<String>>();
		Set<String> nodeCounter = new HashSet<String>();
		
		FileReader fr = new FileReader(fileName);
		BufferedReader b = new BufferedReader(fr);
		
		String line = b.readLine();
		String nodes[];
		List<String> toList;
		List<String> fromList;
		while((line = b.readLine()) != null) { 
			numEdges++;
			nodes = line.toLowerCase().split(" ");
			
			//A->B
			if(!AtoB.containsKey(nodes[0])) {
				toList = new ArrayList<String>();
				toList.add(nodes[1]);
				
				AtoB.put(nodes[0], toList);
			} else {
				toList = AtoB.get(nodes[0]);
				toList.add(nodes[1]);
				
				AtoB.put(nodes[0], toList);
			}
			//B->A
			if(!BtoA.containsKey(nodes[1])) {
				fromList = new ArrayList<String>();
				fromList.add(nodes[0]);
				
				BtoA.put(nodes[1], fromList);
			} else {
				fromList = BtoA.get(nodes[1]);
				fromList.add(nodes[0]);
				
				BtoA.put(nodes[1], fromList);
			}
			nodeCounter.add(nodes[0]);
			nodeCounter.add(nodes[1]);
		}
		this.nodeCounter = nodeCounter;
		numVert = nodeCounter.size();
		b.close();
		
		pageRanks = calcPageRank();
	}
	
	/**
	 * Returns the pagerank of this vertex.
	 * @param vertexName Name of vertex to find pagerank.
	 * @return Pagerank of this vertex.
	 */
	public double pageRankOf(String vertexName) {
		vertexName = vertexName.toLowerCase();
		return(pageRanks.get(vertexName));
	}
	
	/**
	 * Returns number of vertices that link from this vertex.
	 * @param vertexName Name of vertex to find out-degree.
	 * @return Out-degree of this vertex.
	 */
	public int outDegreeOf(String vertexName) {
		vertexName = vertexName.toLowerCase();
		if(!AtoB.containsKey(vertexName)) return(0);
		return(AtoB.get(vertexName).size());
	}
	
	/**
	 * Returns number of vertices that link to this vertex.
	 * @param vertexName Name of vertex to find in-degree.
	 * @return In-degree of this vertex.
	 */
	public int inDegreeOf(String vertexName) {
		vertexName = vertexName.toLowerCase();
		if(!BtoA.containsKey(vertexName)) return(0);
		return(BtoA.get(vertexName).size());
	}
	
	/**
	 * Returns total number of edges in the graph.
	 * @return Number of edges in the graph.
	 */
	public int numEdges() {
		return(numEdges);
	}
	
	/**
	 * Returns total number of vertices in the graph.
	 * @return Number of vertices in the graph.
	 */
	public int numVertices() {
		return(numVert);
	}
	
	/**
	 * Gives the number of iterations for page rank until convergence.
	 * @return Number of iterations in page rank calculations.
	 */
	public int numIter() {
		return(numIter);
	}
	
	/**
	 * Returns the pages with top k pagerank.
	 * @param k Number of pages.
	 * @return Array of pages with top k pagerank.
	 */
	public String[] topKPageRank(int k) {
		List<String> L = new ArrayList<String>(pageRanks.keySet());
		Collections.sort(L, new RankDegreeComparator());
		String[] topK = new String[k];
		
		for(int i = 0; i < k; i++) {
			topK[i] = L.get(i);
		}
		return(topK);
	}
	
	/**
	 * Returns the pages with top k in-degree.
	 * @param k Number of pages.
	 * @return Array of pages with top k in-degree.
	 */
	public String[] topKInDegree(int k) {
		List<String> L = new ArrayList<String>(AtoB.keySet());
		Collections.sort(L, new InDegreeComparator());
		String[] topK = new String[k];
		
		for(int i = 0; i < k; i++) {
			topK[i] = L.get(i);
		}
		return(topK);
	}
	
	/**
	 * Returns the pages with top k out-degree.
	 * @param k Number of pages.
	 * @return Array of pages with top k out-degree.
	 */
	public String[] topKOutDegree(int k) {
		List<String> L = new ArrayList<String>(BtoA.keySet());
		Collections.sort(L, new OutDegreeComparator());
		String[] topK = new String[k];
		
		for(int i = 0; i < k; i++) {
			topK[i] = L.get(i);
		}
		return(topK);
	}
	
	/**
	 * Ranks vertices based on in-degree.
	 * @author Alex Shum
	 */
	public class InDegreeComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return(inDegreeOf(o2) - inDegreeOf(o1));
		}
	}
	
	/**
	 * Ranks vertices based on out-degree.
	 * @author Alex Shum
	 */
	public class OutDegreeComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {	
			return(outDegreeOf(o2) - outDegreeOf(o1));
		}
	}
	
	/**
	 * Ranks vertices based on page rank.
	 * @author Alex Shum
	 */
	public class RankDegreeComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			if(pageRankOf(o2) > pageRankOf(o1)) return(1);
			else if(pageRankOf(o2) < pageRankOf(o1)) return(-1);
			else return(0);
		}
	}
	
	/**
	 * Runs page rank algorithm until convergence.
	 * @return Page rank of vertices as a map from vertex name to page rank scores.
	 */
	private Map<String, Double> calcPageRank() {
		Map<String, Double> pr = new HashMap<String, Double>();
		for(String s : nodeCounter) {
			pr.put(s, 1.0 / numVert);
		}
		
		double diff = Double.MAX_VALUE;
		Map<String, Double> nextIter;
		while(diff > eps) {
			nextIter = singleIterationCalcPageRank(pr);
			diff = diff(nextIter, pr);
			pr = nextIter;
			numIter++;
		}
		return(pr);
	}
	
	/**
	 * Runs a single iteration of the page rank algorithm.
	 * @param pr Previous iteration of page rank as a map from vertex name to page rank score.
	 * @return Next iteration of page rank as a map from vertex name to page rank score.
	 */
	private Map<String, Double> singleIterationCalcPageRank(Map<String, Double> pr) {
		Map<String, Double> nextIter = new HashMap<String, Double>();
		for(String s : nodeCounter) {
			nextIter.put(s, (1 - BETA) / numVert);
		}
		
		double P;
		List<String> out;
		for(String s : nodeCounter) {
			if(!AtoB.containsKey(s)) { //no outlinks
				for(String t : nodeCounter) {
					P = nextIter.get(t) + BETA * pr.get(s) / numVert;
					nextIter.put(t, P);
				}
			} else { //outlinks
				out = AtoB.get(s);
				for(String t : out) {
					P = nextIter.get(t) + BETA * pr.get(s) / out.size();
					nextIter.put(t, P);
				}
			}
		}
		return(nextIter);
	}
	
	/**
	 * Finds the L1 norm of difference between iterations of page rank vectors.
	 * @param nextIter Next iteration of page rank.
	 * @param prevIter Previous iteration of page rank.
	 * @return L1 norm of difference between page rank vectors.
	 */
	private double diff(Map<String, Double> nextIter, Map<String, Double> prevIter) {
		double sum = 0;
		for(String s : nodeCounter) {
			sum += Math.abs(nextIter.get(s) - prevIter.get(s));
		}
		return(Math.sqrt(sum));
	}
}
