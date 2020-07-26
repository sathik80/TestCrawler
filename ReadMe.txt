Please find my comments #inline
Task Web crawler
The command-line program should do the following:
0) Read a string (search term) from standard input
1) Get a Bing result page for the search term
2) Extract main result links from the page
3) Download the respective pages and extract the names of javascript libraries used in them
4) Print top 5 most used libraries to standard output
#The above steps are done and the program prints the tops 5 .js names
Bonus steps - # Within the given time I could not complete the bonus steps
- write tests or think about the approach for testing your code
 #The test Plan approach would be
Testing plan
Objective: to find top 5 js lib
Required: Bing url, search term
attributes: web pages extracted, web crawl for .js
Test cases
	1 - bing website availability check
	2 - search criteria consistence check
	3 – links are downloaded correctly
	4 - js lib filter works correctly

- think about / implement Java concurrency utilities to speed up certain tasks
# used ExecutorService Fixed ThreadPool, checked the CPU Cores and processors available, used ConcurrentHashMap
- think about / implement deduplication algorithms for the same Javascript libraries with different Names
# for this we need to visit the lib link and check the  comment at the top for instance this lib https://unpkg.com/docsearch.js@2.4.1/dist/cdn/docsearch.min.js has the comment
/*! docsearch 2.4.1 | © Algolia | github.com/algolia/docsearch */
We can use a key value pair for each lib found and remove a duplicate .js with a different name.

Notes
- use whatever approach you think is the best and most efficient, you don’t need to create elaborate or complex parsing algorithms
# I followed simple approach as described in task. Used JDK HTMLEditorKit for HTML parsing of A HREF and, stored HTML content in files for Javascript names scraping.
- you can skip a step if it's too hard (and mock data for the next step) 
#Hardcoded few strings 
- if something is not clear or can be done in multiple ways, describe why you chose your approach
# I followed simple approach as described in task.
- use a minimum of 3rd party libraries if possible, preferred is just JDK , however you can mention libraries with which a certain task would be more efficient and/or easier to write
# No third party libraries are used. For HTML parsing and web crawling there are quite a few third party libs available some are opensource and free and some are paid
java based HTML parser that support visiting and parsing the HTML pages.
•	Jsoup
•	Jaunt API
•	HtmlCleaner
•	JTidy
•	NekoHTML
•	TagSoup
•	Crawler4j



