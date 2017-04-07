package marto.job_search_engine.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Elements implements Iterable<Element> {

	private List<Element> elements;

	Elements(org.jsoup.select.Elements jsoupElements) {
		elements = new ArrayList<>(jsoupElements.size());

		for (org.jsoup.nodes.Element jsoupElement : jsoupElements) {
			elements.add(new Element(jsoupElement));
		}
	}

	@Override
	public Iterator<Element> iterator() {
		return elements.iterator();
	}
}