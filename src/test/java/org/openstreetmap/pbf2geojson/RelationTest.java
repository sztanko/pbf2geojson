package org.openstreetmap.pbf2geojson;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class RelationTest {

	//@Test
	public void test() {
		try {
			ConvertMultiThread.main("src/test/resources/timanaya_park.pbf", "/dev/stdout");
		} catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	}
	}
	
	//@Test
	public void test2() {
		try {
			ConvertMultiThread.main("src/test/resources/3241023_full.osm.pbf", "/dev/stdout");
		} catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	}
	}

	@Test
	public void test3() {
		try {
			ConvertMultiThread.main("src/test/resources/1534625_full.osm.pbf", "/dev/stdout");
		} catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	}
	}
	
}
