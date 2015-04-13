import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Crawls a wikipedia page and generates a graph starting at user input starting page and 
 * only including pages with user input keywords.  The graph will be recorded as a the number
 * of edges followed by a list of edges in the textfile.  Each line represents an edge and each
 * edge is a spare separated pair of vertex names.  
 * 
 * This process will continue until the maximum number of pages are collected where the maximum
 * is user specificed.  Once this limit has been reached, edges in the graph will continue to be generated
 * but only between previously recorded vertices.  Any URLs to previously unseen pages will be ignored 
 * once the max page limit has been reached.  
 * 
 * The above insures a hard upper limit on the number of vertices and prevents the situation where
 * there are edges to new pages that were not completely crawled.
 * @author Alex Shum
 */
public class WikiCrawler {
	private static final String BASE_URL = "http://en.wikipedia.org";
	private LinkedList<String> toVisit; //link queue
	private Set<String> visitedURL; //visited URL that does not contain search terms
	private Set<String> visitedUsefulURL; //visited URL that contains search terms
	private List<String> edges;
	
	private String seedURL; //start url
	private String[] keywords; //search words
	private int max; //max number of pages
	private int numCrawled; //number of pages that contain search words
	private int pagesRequested;
	private String fileName; 
	
	/**
	 * Creates a new WikiCrawler Object to crawl wikipedia pages and generate a graph file.
	 * @param seedURL Start URL, must be relative wikipedia url: "/wiki/Title_of_Article".
	 * @param keywords Array of search terms to find.
	 * @param max Maximum number of unique pages.
	 * @param fileName Name of file to record graph edges.
	 */
	public WikiCrawler(String seedURL, String[] keywords, int max, String fileName) {
		toVisit = new LinkedList<String>();
		visitedURL = new HashSet<String>();
		visitedUsefulURL = new HashSet<String>();
		edges = new LinkedList<String>();
		
		this.seedURL = seedURL;
		this.keywords = keywords;
		this.max = max;
		numCrawled = 0;
		pagesRequested = 0;
		this.fileName = fileName;
	}
	
	/**
	 * Starts the wikipedia page crawling process starting from the seedURL.
	 * @throws InterruptedException if Thread.sleep has any issues.
	 */
	public void crawl() throws InterruptedException {
		//check if seed URL contains your search terms
		SimpleWikiContentParser c = new SimpleWikiContentParser(BASE_URL + seedURL, keywords);
		if(c.pageContainsAllTerms()) {
			toVisit.add(seedURL);
			pagesRequested++;
			numCrawled++;
		}
		visitedUsefulURL.add(seedURL);

		//begin crawl
		boolean keepCrawling = true;
		while(keepCrawling) {
			keepCrawling = crawlNext();
		}
		
		//record results
		try {
			writeToFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Crawls the next page in the queue.  Collects new links until max limit reached; afterwards
	 * this will continue to add edges to the graph only between previously seen vertices.
	 * 
	 * This function will automatically wait 2 seconds every 200 page requests.
	 * 
	 * @warning Error handling is handled by SimpleWikiLinkParser and SimpleWikiContentParser.
	 * 			Any issues with bad links or connectivity will result in trying the next available URL.
	 * 
	 * @return true if there are any links left to crawl in the queue.  false otherwise.
	 * @throws InterruptedException if Thread.sleep has any issues.
	 */
	public boolean crawlNext() throws InterruptedException {
		if(toVisit.isEmpty()) return(false);
		String fromPage = toVisit.poll();
		
		System.out.println("fromPage: " + BASE_URL + fromPage); //TODO
		SimpleWikiLinkParser s = new SimpleWikiLinkParser(BASE_URL + fromPage);
		Set<String> newLinks = s.getLinks();
		pagesRequested++;
		
		SimpleWikiContentParser c;
		for(String l : newLinks) {	
			System.out.println("collected: " + numCrawled + ", Left in Queue: " + toVisit.size() + " toPage: " + BASE_URL + l); //TODO
			
			if(visitedUsefulURL.contains(l) && !fromPage.equalsIgnoreCase(l)) { //previously seen this page and it has keyword matches
				edges.add(fromPage + " " + l); 
			} else if(visitedURL.contains(l) || fromPage.equalsIgnoreCase(l)) { //previously seen this page, no keyword matches
				//do nothing
			} else if(numCrawled < max) { //new unseen page and still need to crawl more pages
				c = new SimpleWikiContentParser(BASE_URL + l, keywords);
				if(pagesRequested % 200 == 0) Thread.sleep(2000);
				pagesRequested++;
				
				if(c.pageContainsAllTerms()) {
					toVisit.add(l);
					edges.add(fromPage + " " + l);
					visitedUsefulURL.add(l);
					numCrawled++;
				} else {
					visitedURL.add(l);
				}
			} else { //done getting new pages
				//do nothing
			}
		}
		return(true);		
	}
	
	/**
	 * Writes the number of edges and list of edges down to the text file.
	 * @param fileName Name of text file.
	 * @throws IOException If file cannot be opened, created or written to.
	 */
	private void writeToFile(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(max + System.lineSeparator());
		
		for(String s : edges) {
			bw.write(s + System.lineSeparator());
		}
		bw.close();
	}
}
