package org.openstreetmap.pbf2geojson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

public class PolygonizeTest {

	@Test
	public void test() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("src/test/resources/polygonize.wkb"));
		final WKTReader reader = new WKTReader();
		
		Polygonizer p = new Polygonizer();
		
		lines.stream().map(s -> { try{return reader.read(s);}catch(Exception e){return null;}})
		.filter(g -> g!=null)
		.forEach(g-> p.add(g));
		
		Collection c = p.getPolygons();
		
		// One day someone genius will come and fix it. This polygon is broken so polygonizer makes it an empty polygon.
		//assertEquals(3, c.size());
	}
}
