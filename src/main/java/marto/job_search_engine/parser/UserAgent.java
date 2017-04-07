package marto.job_search_engine.parser;

import java.io.IOException;

import org.jsoup.Jsoup;

import marto.job_search_engine.parser.exceptions.ResponseException;

public class UserAgent {

	private Document document;

	public void visit(String link) throws ResponseException {
		org.jsoup.nodes.Document jsoupDoc;
		try {
			jsoupDoc = Jsoup.connect(link).get();
		} catch (IOException e) {
			throw new ResponseException("Unable to open "+link, e);
		}

		document = new Document(jsoupDoc);
	}

	public Document getDoc() {
		return document;
	}
}