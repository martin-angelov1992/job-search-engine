package marto.job_search_engine.parser;

import java.util.Iterator;

import marto.job_search_engine.parser.exceptions.NotFound;

public class Element {

	private org.jsoup.nodes.Element jsoupElement;

	Element(org.jsoup.nodes.Element jsoupElement) {
		setJSoupElement(jsoupElement);
	}

	void setJSoupElement(org.jsoup.nodes.Element jsoupElement) {
		this.jsoupElement = jsoupElement;
	}

	public Element findFirst(String elementToFind) throws NotFound {
		Iterator<Element> elements = findEach(elementToFind).iterator();

		if (!elements.hasNext()) {
			throw new NotFound();
		}

		return elements.next();
	}

	public Elements findEach(String elementToFind) {
		Elements elements = new Elements(jsoupElement.select(elementToFind));

		return elements;
	}

	public String getAt(String string) {
		return jsoupElement.attr(string);
	}

	public String innerHTML() {
		return jsoupElement.html();
	}

}
