package com.onesccount.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * 
 * @author Sathik
 *
 *         1Account Test.
 *
 *         1. WebCrawlerToFindJSName class takes a command line argument and
 *         searches the given string value in www.bing.com 2. Extracts the links
 *         from the result page and saved in a file 3. Downloads the source page
 *         as HTML from the links saved one by one 4. Crawls the web source page
 *         and looks for .js and saves them in a Map with number of usages 5.
 *         Prints the top 5 .js from the Map.
 * 
 *         No Third party libs used.
 * 
 */
public class Crawler {

	private static final String HREF_URL = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static final String SRC_W_JS = "(?:src)=(\"|').*?([\\w.]+\\.(?:js))\\1";
	static String directoryPath = "/java/bing/";

	static String url = "https://www.bing.com/search?q=";
	static String defaultArg = "WebCrawler";
	static int processors;

	static final String JS_RESULT_HTML = "jsResult.html";

	static List<URL> _hrefLinks = new ArrayList<URL>();

	static ConcurrentHashMap<String, Integer> JSNamesMap = new ConcurrentHashMap<String, Integer>();

	public static void main(String args[]) throws IOException {

		System.out.println("Please enter the search term :");

		Scanner sc = new Scanner(System.in); // creates instance of Scanner to read from console
		String arg = sc.nextLine().trim(); // read string
		sc.close();
		processors = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of processors available:" + processors);

		File file = new File(directoryPath);
		if (!file.isDirectory()) {
			new File(directoryPath).mkdirs();
		}

//		if no input given search for crawler
		arg = arg.length() > 0 ? arg : defaultArg;
		url = url + arg;

		System.out.println("Bing.com being searched for :" + arg);

		extractLinksFromSearchPage(getHtmlOfGivenUrl(url));

		System.out.println("Links Extracted are:");
		System.out.println("#################################");
		_hrefLinks.forEach(System.out::println);
		System.out.println("#################################");

		concurrentDownloadLinksAndCrawlJSName("D");
		concurrentDownloadLinksAndCrawlJSName("S");
		

		/** Enable the below for full list crawling **/
//		_hrefLinks.forEach(k -> {fetchWebPage(k.toString(),javaScriptResultFromLinks);
//								extractJavaScriptNamesFromLink(new File(javaScriptResultFromLinks));
//								});

		sortMapAndPrintTopFive();
	}

	/**
	 * Concurrent download and crawl using ExecutorService pool.
	 * @param action TODO
	 */
	private static void concurrentDownloadLinksAndCrawlJSName(String action) {
		ExecutorService pool = Executors.newFixedThreadPool(processors);

		long timeStartFuture = Calendar.getInstance().getTimeInMillis();
		int max_numOf_Linksto_process= _hrefLinks.size()>15?15:_hrefLinks.size();
		for (int i = 0; i < max_numOf_Linksto_process; i++) {
		pool.execute(action.equalsIgnoreCase("D")?new DownloadFileFromLinks(i):new scrapJSNamesFromDownloadedFile(i));
		}
		
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long timeEndFuture = Calendar.getInstance().getTimeInMillis();
		long timeNeededFuture = timeEndFuture - timeStartFuture;
		System.out.println("Parallel calculated in " + timeNeededFuture + " ms");
	}

	/**
	 * Sort the Map and print the top 5 .js names.
	 */
	public static void sortMapAndPrintTopFive() {
		Set<Entry<String, Integer>> set = JSNamesMap.entrySet();

		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		System.out.println("Top 5 JavaScript names used are :");
		System.out.println("#################################");
		if(list.size()>5) {
		list.subList(0, 5).forEach(System.out::println);
		}else {
			list.forEach(System.out::println);	
		}
		System.out.println("#################################");

	}

	/**
	 * Fetch Web page source for the given URL and save in a file.
	 * 
	 * @param webpage
	 * @param fileName
	 */
	public static void fetchWebPage(String webpage, String fileName) {
		try {
			// Create URL object
			URL url = new URL(webpage);
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			BufferedReader readr = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			// Enter filename in which you want to download
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			// read each line from stream till end
			String line;
			while ((line = readr.readLine()) != null) {
				writer.write(line);
			}
			readr.close();
			writer.close();
		}
		// Exceptions
		catch (MalformedURLException mue) {
			System.out.println("Malformed URL Exception raised");
		} catch (IOException ie) {
//			System.out.println("IOException raised");
//			ie.printStackTrace();
		}

	}

	/**
	 * Crawl JavaScript names from the given Web page Source.
	 * 
	 * @param javaScriptResultFromLinks
	 */
	static void extractJavaScriptNamesFromLink(File javaScriptResultFromLinks) {
		Pattern linkPattern = Pattern.compile(SRC_W_JS, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		try { 
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(javaScriptResultFromLinks), StandardCharsets.UTF_8));
			String line;
			while ((line = in.readLine()) != null) {
				Matcher pageMatcher = linkPattern.matcher(line);
				while (pageMatcher.find()) {
					// group 2 has the .js name
					String jsName = pageMatcher.group(2);
					// Keep the count of JSNames for finding the top 5
					if (!JSNamesMap.containsKey(jsName)) { // putIfAbsent can be used if we do not need the count to be
															// increased
						JSNamesMap.put(jsName, 1);
					} else {
						JSNamesMap.put(jsName, JSNamesMap.get(jsName) + 1);
					}
				}
			}

			in.close();
			javaScriptResultFromLinks.delete();
		} catch (Exception e) {
//			e.printStackTrace();
		}

	}

	/**
	 * Get the WebPageSource as String.
	 * 
	 * @param webpage
	 * @return
	 */
	private static String getHtmlOfGivenUrl(String webpage) {
		StringWriter sw = new StringWriter();
		try {
			// Create URL object
			URL url = new URL(webpage);
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

			BufferedReader readr = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line;
			while ((line = readr.readLine()) != null) {
				sw.append(line).append("\n");
			}
			readr.close();
		}
		// Exceptions
		catch (MalformedURLException mue) {
			System.out.println("Malformed URL Exception raised");
		} catch (IOException ie) {
			ie.printStackTrace();
			System.out.println("IOException raised");
		}
		return sw.getBuffer().toString();
	}

	/**
	 * Extract the HREF Links from the main search result and avoid duplicates.
	 * 
	 * @param fileName
	 * @throws MalformedURLException
	 *
	 */
	private static void extractLinksFromSearchPage(String htmlString)
			throws MalformedURLException {
		Pattern linkPattern = Pattern.compile(HREF_URL, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		try {
			Reader reader = new StringReader(htmlString);
			
			HTMLEditorKit.Parser parser = new ParserDelegator();

			parser.parse(reader, new HTMLEditorKit.ParserCallback() {
				public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
					if (t == HTML.Tag.A) {
						String link = (String) a.getAttribute(HTML.Attribute.HREF);
						if (link != null) {
							try {
								Matcher pageMatcher = linkPattern.matcher(link);
								while (pageMatcher.find()) {
									URL tempUrl = new URL(pageMatcher.group());
									// avoid Duplication of the same host
									if (!_hrefLinks.stream().filter(o -> o.getHost().equals(tempUrl.getHost())).findFirst()
											.isPresent()) {
										_hrefLinks.add(tempUrl);
									}
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}, true);
		
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Down load Web page source from the links.
	 * 
	 * @author sathik
	 *
	 */
	public static class DownloadFileFromLinks implements Runnable {
		int _index;
		public DownloadFileFromLinks(int index) {
			_index = index;	
		}
		@Override
		public void run() {			
				System.out.println(
						"[crawling.... ]" + Crawler._hrefLinks.get(_index) + "  for .js in :" + Crawler.directoryPath + _index + Crawler.JS_RESULT_HTML);
				Crawler.fetchWebPage(Crawler._hrefLinks.get(_index).toString(), Crawler.directoryPath + _index + Crawler.JS_RESULT_HTML);
				
			}
		}
	/**
	 * Down load Web page source from the links.
	 * 
	 * @author sathik
	 *
	 */
	public static class scrapJSNamesFromDownloadedFile implements Runnable {
		int _index;
		public scrapJSNamesFromDownloadedFile(int index) {
			_index = index;	
		}
		@Override
		public void run() {			
				
				System.out.println(
						"[Scraping for JS.... ]" + Crawler._hrefLinks.get(_index) + "  for .js in :" + Crawler.directoryPath + _index + Crawler.JS_RESULT_HTML);
				Crawler.extractJavaScriptNamesFromLink(new File(Crawler.directoryPath + _index + Crawler.JS_RESULT_HTML));
			}
		}
}

