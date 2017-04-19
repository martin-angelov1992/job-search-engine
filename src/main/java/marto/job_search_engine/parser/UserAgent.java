package marto.job_search_engine.parser;

import java.io.IOException;

import org.jsoup.Jsoup;

import marto.job_search_engine.parser.exceptions.ResponseException;

public class UserAgent {

	private Document document;

	private static final int RETRY_COUNT = 25;

	private static volatile int errorCount;

	public void visit(String link) throws ResponseException {
		org.jsoup.nodes.Document jsoupDoc = null;
	
		for (int i = 0; i < RETRY_COUNT; ++i) {
			try {
				jsoupDoc = Jsoup.connect(link).get();
				break;
			} catch (IOException e) {
				// Retry
			}
		}

		if (jsoupDoc == null) {
			System.err.println("Unable to get document from "+link);

			synchronized(UserAgent.class) {
				++errorCount;
			}
		}

		document = new Document(jsoupDoc);
	}

	public Document getDoc() {
		return document;
	}

	public static synchronized int getErrorCount() {
		return errorCount;
	}
}