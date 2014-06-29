package org.openstreetmap.pbf2geojson;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class ConvertMultiThreadTest {

	@Test
	public void test() {
		try {
			ConvertMultiThread.main("src/test/resources/park_serra_de_estrella.osm.pbf", "/dev/stdout");
		} catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	}
	}

}
