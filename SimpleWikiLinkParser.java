import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Searches the html contents of a wikipedia page to grab in-text links (relative links).
 * This will not return any external links, sidebar links or reference links.
 * 
 * This will try to follow the nicencess policies on wikipedia's robots.txt.
 * This will also ignore any subsection links (links with "#"), any links with "&"
 * and any links with ":".  Finally, this will also not record any of wikipedia's redlinks,
 * or any pages related to the wikifoundation.
 * 
 * Links returned will be relative wikipedia links: "/wiki/title_of_article"
 * 
 * All error handling related to connectivity and bad URLs is done internally.
 * @author Alex Shum
 */
public class SimpleWikiLinkParser {
	private URL startPage;
	private Set<String> links;
	
	/**
	 * Creates a new link parser object.  Uses full URL not relative URL.
	 * @param startPage Full URL of page to get links from.
	 */
	public SimpleWikiLinkParser(String startPage) {
		try {
			this.startPage = new URL(startPage);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		links = new LinkedHashSet<String>();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(this.startPage.openStream()));
			String line;
			boolean startRead = false;
			while((line = br.readLine()) != null) {		
				if(line.contains("<p>")) {
					startRead = true;
				}
				if(startRead && line.contains("<a href=") ) {
					links.addAll(findAllURLs(line));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This will return a set of the in-text links located on the startPage.  
	 * Links will be relative wikipedia links: "/wiki/title_of_article".
	 * @return Set of in-text wikipedia links.
	 */
	public Set<String> getLinks() {
		return(links);
	}
		
	/**
	 * Finds all URLs from a line of html.  
	 * Ignores links that are restricted: 
	 * robots.txt restricted, external, "#", ":", "&" and redlinks.
	 * @param html Line of html to find URLs.
	 * @return List of URLs in this line of html.
	 */
	private List<String> findAllURLs(String html) {
		List<String> urls = new ArrayList<String>();
		int startPos = 0;
		int endPos = 0;
		
		boolean ignoreLine;
		while(startPos >= 0) {
			startPos = html.indexOf("<a href=", startPos);
			if(startPos >= 0) {
				startPos = html.indexOf("\"", startPos) + 1;
				endPos = html.indexOf("\"", startPos);
				
				ignoreLine = false;
				for(String i : NOT_ALLOWED) {
					if(html.substring(startPos, endPos).contains(i)) ignoreLine = true;
				}
				
				if(!ignoreLine) {
					urls.add(html.substring(startPos, endPos));
				}
			}
		} 
		return(urls);
	}
	
	//restricted links
	@SuppressWarnings("serial")
	private static final List<String> NOT_ALLOWED = new ArrayList<String>() {{
		add("trap"); add("/wiki/Special");
		add("/wiki/Wikipedia:Articles_for_deletion");
		add("/wiki/Wikipedia:Votes_for_deletion");
		add("/wiki/Wikipedia:Pages_for_deletion");
		add("/wiki/Wikipedia:Miscellany_for_deletion");
		add("/wiki/Wikipedia:Miscellaneous_deletion");
		add("/wiki/Wikipedia:Copyright_problems");
		add("/wiki/Wikipedia:Protected_titles");
		add("/wiki/Wikipedia:WikiProject_Spam");
		add("/wiki/MediaWiki:Spam-blacklist");
		add("/wiki/MediaWiki_talk:Spam-blacklist");
		add("/wiki/Portal:Prepared_stories");
		add("/wiki/Wikibooks:Votes_for_deletion");
		add("/wiki/Wikipedia:Requests_for_arbitration");
		add("redlink=1;");
		add("/wiki/Main_Page");
		add(".org"); add(".net"); add(".com");
		add("#"); add(":"); add("&");
	}};
}
