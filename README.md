# WikipediaPageRank
The world's simplest Wikipedia crawler and page ranker.

To crawl a set of pages and create a graph:
```
String[] searchTerms = {"term1", "term2"};
int max_nodes = 1000;
String outputFileName = "file_name.txt"
WikiCrawler w = new WikiCrawler("/wiki/title_of_start_page", searchTerms,  max_nodes, outputFileName);
w.crawl();
```

Calculate page rank of above graph:
```
double eps = 0.05; //pagerank convergence criteria
PageRank p = new PageRank(outputFileName, eps);
String[] top100 = p.topKPageRank(100); //top 100 page rank
```
