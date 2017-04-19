package marto.job_search_engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import marto.job_search_engine.parser.Element;
import marto.job_search_engine.parser.Elements;
import marto.job_search_engine.parser.UserAgent;
import marto.job_search_engine.parser.exceptions.NotFound;
import marto.job_search_engine.parser.exceptions.ResponseException;

public class Scrape {

	private Set<String> ignoreCategoryTypes;

	private Storage storage;

	private static final String WEBSITE_URL = "http://www.jobtiger.bg";

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(20);

	public static void main(String[] args) throws InterruptedException {
		new Scrape().run();

		executor.shutdown();

		if (UserAgent.getErrorCount() > 0) {
			System.err.println("Error count: " + UserAgent.getErrorCount());
		}
	}

	public void run() {
		storage = new Storage();

		ignoreCategoryTypes = new HashSet<>();
		ignoreCategoryTypes.add("ТИП");
		ignoreCategoryTypes.add("ВАЛИДНА ОТ");

		UserAgent userAgent = new UserAgent();

		int page = 1;

		boolean hasMorePages = true;

		while (hasMorePages) {
			String link = "http://www.jobtiger.bg/obiavi-za-rabota/?_page=" + page;

			hasMorePages = handleListingsPage(link, userAgent, page);

			++page;
		}
	}

	public boolean handleListingsPage(String link, UserAgent agent, final int page) {
		return crawlListingsPage(link, agent, page);
	}

	public boolean crawlListingsPage(String link, UserAgent agent, final int page) {
		System.out.println("Handling page: "+link);
		try {
			agent.visit(link);
		} catch (ResponseException e) {
			System.err.printf("Error visiting link: %s\n", link);
			e.printStackTrace();
			return true;
		}

		try {
			Element element = agent.getDoc().findFirst("div.NotExistJobs");

			if (element != null) {
				return false;
			}
		} catch (NotFound e) {
			// Do nothing. This is not last page.
		}

		Element jobListing;

		try {
			jobListing = agent.getDoc().findFirst("div#JobsListContainer");
		} catch (NotFound e) {
			System.err.printf("Unable to find JobsListContainer element for page: ", link);
			e.printStackTrace();
			return true;
		}

		Elements listElements;
		try {
			listElements = jobListing.findFirst("ul").findEach("li");
		} catch (NotFound e1) {
			System.err.printf("Some needed elements were not found for page: ", link);
			e1.printStackTrace();
			return true;
		}

		for (Element listElement : listElements) {
			Runnable r = () -> {
				try {
					handleJobOffer(listElement.findFirst("a").getAt("href"), new UserAgent(), page);
				} catch (NotFound e) {
					System.err.println("Unable to get link for job offer.");
					e.printStackTrace();
				}
			};

			executor.submit(r);
		}

		return true;
	}

	private void handleJobOffer(String link, UserAgent agent, int page) {
		link = makeFullLink(link);
		try {
			agent.visit(link);
		} catch (ResponseException e) {
			e.printStackTrace();
			System.err.printf("Unable to visit job offer: %s", link);
		}

		String[] parts = link.split("jobtiger.bg/");
		String remaining = parts[1];
		parts = remaining.split("/");

		if (parts[0].equals("obiavi-za-rabota")) {
			handleKnownFormat(link, agent);
		} else {
			handleUnknownFormat(agent, link);
		}

		System.out.println("Handled: " + link + " from page " + page);
	}

	public String makeFullLink(String link) {
		if (link.startsWith("http://") || link.startsWith("https://")) {
			return link;
		}

		return WEBSITE_URL+link;
	}

	private void handleUnknownFormat(UserAgent agent, String link) {
		String text = stripHtml(agent.getDoc().innerHTML());

		String[] parts = link.split("/");

		int lastIndex = parts.length-1;

		if (parts[lastIndex].equals("")) {
			lastIndex -= 1;
		}

		String company = parts[lastIndex-1].replace("-", " ");
		String jobTitle = parts[lastIndex].replace("-", " ");

		storage.processJobWithUnknownFormat(text, company, jobTitle);
	}

	private void handleKnownFormat(String jobLink, UserAgent agent) {
		Elements elements;
		String jobTitle = null;
		String companyName = null;
		String jobText = null;
		Map<String, String> categoryTypes = new HashMap<>();

		try {
			jobTitle = agent.getDoc().findFirst("div.JobContent").findFirst("div.Title").innerHTML().trim();
		} catch (NotFound e1) {
			e1.printStackTrace();
			System.err.println("Unable to get job title.");
		}

		try {
			companyName = agent.getDoc().findFirst("div.CompanyBlock").findFirst("div.Info").findFirst("div.Name").innerHTML().trim();
		} catch (NotFound e) {
			System.err.println("Unable to extract company name");
			e.printStackTrace();
		}

		try {
			elements = agent.getDoc().findFirst("div#Nav").findFirst("nav").findFirst("ul").findEach("li");
		} catch (NotFound e) {
			System.err.printf("Unable to get category elements for: %s", jobLink);
			e.printStackTrace();
			return;
		}

		try {
			jobText = agent.getDoc().findFirst("div.JobContent").findFirst("div.Description").innerHTML().trim();
		} catch (NotFound e) {
			System.err.printf("Unable to get job text for: %s", jobLink);
			e.printStackTrace();
		}

		for (Element element : elements) {
			String categoryType;
			String categoryTypeValue;
			try {
				categoryType = element.findFirst("div.Title").innerHTML().trim();
				categoryTypeValue = element.findFirst("div.Value").innerHTML().trim();
			} catch (NotFound e) {
				System.err.printf("Unable to get category type elements for: %s", jobLink);
				e.printStackTrace();
				continue;
			}

			if (ignoreCategoryTypes.contains(categoryType)) {
				continue;
			}

			categoryTypes.put(categoryType, categoryTypeValue);
		}

		storage.processJobWithKnownFormat(categoryTypes, companyName, jobTitle, stripHtml(jobText));
	}

	private String stripHtml(String html) {
		return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
	}
}