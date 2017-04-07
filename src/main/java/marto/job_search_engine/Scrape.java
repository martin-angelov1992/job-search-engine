package marto.job_search_engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import marto.job_search_engine.parser.Element;
import marto.job_search_engine.parser.Elements;
import marto.job_search_engine.parser.UserAgent;
import marto.job_search_engine.parser.exceptions.NotFound;
import marto.job_search_engine.parser.exceptions.ResponseException;

public class Scrape {

	private Set<String> ignoreCategoryTypes;

	private Storage storage;

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
			jobListing = agent.getDoc().findFirst("<div id=\"JobsListContainer\"");
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
			handleUnknownFormat(agent, link);
		}
	}

	private void handleUnknownFormat(UserAgent agent, String link) {
		String text = stripHtml(agent.getDoc().innerHTML());

		String[] parts = link.split(" ");

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
			jobTitle = agent.getDoc().findFirst("<div class=\"JobContent\">").findFirst("<div class=\"Title\">").innerHTML().trim();
		} catch (NotFound e1) {
			e1.printStackTrace();
			System.err.println("Unable to get job title.");
		}

		try {
			companyName = agent.getDoc().findFirst("CompanyBlock").findFirst("Info").findFirst("Name").innerHTML().trim();
		} catch (NotFound e) {
			System.err.println("Unable to extract company name");
			e.printStackTrace();
		}

		try {
			elements = agent.getDoc().findFirst("<div id=\"Nav\"").findFirst("<nav>").findFirst("<ul>").findEach("<li>");
		} catch (NotFound e) {
			System.err.printf("Unable to get category elements for: %s", jobLink);
			e.printStackTrace();
			return;
		}

		try {
			jobText = agent.getDoc().findFirst("<div class=\"JobContent\">").findFirst("<div class=\"Description\">").innerHTML().trim();
		} catch (NotFound e) {
			System.err.printf("Unable to get job text for: %s", jobLink);
			e.printStackTrace();
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

		storage.processJobWithKnownFormat(categoryTypes, companyName, jobTitle, stripHtml(jobText));
	}

	private String stripHtml(String html) {
		return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
	}
}