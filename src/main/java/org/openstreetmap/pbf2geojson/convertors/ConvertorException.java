package org.openstreetmap.pbf2geojson.convertors;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ConvertorException extends RuntimeException {

	public ConvertorException() {
	}

	
	public ConvertorException(JsonProcessingException e) {
		}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3777779686549292809L;

}
