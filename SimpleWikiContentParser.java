import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Searches the text contents of a wikipedia page looking for all inputted keywords.
 * This parser object will determine if the page contains all keywords.
 * Text matches are not strict and are case insensitive.
 * 
 * For example: "text" and "TEXT" will match and 
 * "Textbook" and "text" will also match.
 * 
 * All error handling related to connectivity and bad URLs is done internally.
 * @author Alex Shum
 */
public class SimpleWikiContentParser {
	private URL page;
	private URL contentPage;
	private String[] searchTerms;
	private boolean containsAllTerms;
	
	/**
	 * Creates a new content parser object.
	 * @param page The start URL page.  This is the full URL, not relative URL.
	 * @param searchTerms Array of keyword terms to search for.
	 */
	public SimpleWikiContentParser(String page, String[] searchTerms) {
		try {
			this.page = new URL(page);
			this.contentPage = urlTransform(this.page);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.searchTerms = searchTerms;
		containsAllTerms = false;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(this.contentPage.openStream()));
			String line;
			while((line = br.readLine()) != null) {
				if(containsAll(line)) {
					containsAllTerms = true;
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * After scanning through the text, this will tell if the page contains all terms.
	 * @return true if page contains all terms.  false otherwise.
	 */
	public boolean pageContainsAllTerms() {
		return(containsAllTerms);
	}
	
	/**
	 * Takes a wikipedia page and finds the corresponding raw text page.
	 * Takes the full URL not relative URL.
	 * @param fullURL URL of page.
	 * @return Raw text URL of page.
	 * @throws MalformedURLException If the resulting URL is malformed.
	 */
	private URL urlTransform(URL fullURL) throws MalformedURLException {
		String title = fullURL.getPath().replace("/wiki/", "");
		String path = "/w/index.php?title=" + title + "&action=raw";
		URL url = new URL(fullURL.getProtocol() + "://" + fullURL.getHost() + path);
		
		return(url);
	}
	
	/**
	 * For a line of HTML this will determine if the line contains all search terms. 
	 * @param html Line of HTML to search.
	 * @return true if line contains all search terms.  false otherwise.
	 */
	private boolean containsAll(String html) {
		html = html.toLowerCase();
		html = html.replaceAll("\\p{P}", " ");
		
		for(int i = 0; i < searchTerms.length; i++) {
			if(!html.contains(searchTerms[i].toLowerCase())) return(false);
		}
		
		return(true);
	}
}
