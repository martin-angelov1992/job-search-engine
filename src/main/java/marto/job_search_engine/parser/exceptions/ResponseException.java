package marto.job_search_engine.parser.exceptions;

import java.io.IOException;

public class ResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResponseException(String text, IOException e) {
		super(text, e);
	}

}
