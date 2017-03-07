package marto.job_search_engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

public class Scrape {

	private Set<String> ignoreCategoryTypes;
	public static void main(String[] args) throws InterruptedException {
		new Scrape().run();
	}

	public void run() {
		ignoreCategoryTypes = new HashSet<>();
		ignoreCategoryTypes.add("ТИП");
		ignoreCategoryTypes.add("ВАЛИДНА ОТ");

		UserAgent userAgent = new UserAgent();

		for (int i = 1; i < 100; ++i) {
			String link = "http://www.jobtiger.bg/obiavi-za-rabota/?_page=" + i;

			handleListingsPage(link, userAgent);
		}
	}

	public void handleListingsPage(String link, UserAgent agent) {
		Runnable r = () -> {
			crawlListingsPage(link, agent);
		};

		Thread thread = new Thread(r);
		thread.start();
	}

	public void crawlListingsPage(String link, UserAgent agent) {
		try {
			agent.visit(link);
		} catch (ResponseException e) {
			System.err.printf("Error visiting link: %s", link);
			e.printStackTrace();
			return;
		}

		Element jobListing;

		try {
			jobListing = agent.doc.findFirst("<div id=\"JobsListContainer\"");
		} catch (NotFound e) {
			System.err.printf("Unable to find JobsListContainer element for page: ", link);
			e.printStackTrace();
			return;
		}

		Elements listElements;
		try {
			listElements = jobListing.findFirst("<ul>").findEach("<li>");
		} catch (NotFound e1) {
			System.err.printf("Some needed elements were not found for page: ", link);
			e1.printStackTrace();
			return;
		}

		for (Element listElement : listElements) {
			try {
				handleJobOffer(listElement.findFirst("<a>").getAt("href"), agent);
			} catch (NotFound e) {
				System.err.println("Unable to get link for job offer.");
				e.printStackTrace();
			}
		}
	}

	private void handleJobOffer(String link, UserAgent agent) {
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
			handleUnknownFormat(agent);
		}
	}

	private void handleUnknownFormat(String jobLink, UserAgent agent) {

	}

	private void handleKnownFormat(String jobLink, UserAgent agent) {
		Elements elements;
		String jobTitle = null;
		String companyName = null;
		Map<String, String> categoryTypes = new HashMap<>();

		try {
			jobTitle = agent.doc.findFirst("<div class=\"JobContent\">").getFirst("<div class=\"Title\">").innerHTML().trim();
		} catch (NotFound e1) {
			e1.printStackTrace();
			System.err.println("Unable to get job title.");
		}

		try {
			companyName = agent.doc.findFirst("CompanyBlock").findFirst("Info").findFirst("Name");
		} 

		try {
			elements = agent.doc.findFirst("<div id=\"Nav\"").findFirst("<nav>").findFirst("<ul>").findEach("<li>");
		} catch (NotFound e) {
			System.err.printf("Unable to get category elements for: %s", jobLink);
			e.printStackTrace();
			return;
		}

		for (Element element : elements) {
			String categoryType;
			String categoryTypeValue;
			try {
				categoryType = element.findFirst("<div class=\"Title\">").innerHTML().trim();
				categoryTypeValue = element.findFirst("<div class=\"Value\">").innerHTML().trim();
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

		processJobWithKnownFormat(categoryTypes);
	}

	private void processJobWithKnownFormat(Map<String, String> categoryTypes, String company, String jobTitle, String text) {
		
	}

	private void processJobWithUnknownFormat(String company, String text) {
		
	}
}