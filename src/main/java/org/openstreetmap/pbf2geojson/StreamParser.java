package org.openstreetmap.pbf2geojson;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf2geojson.convertors.ConvertorUtils;
import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.convertors.IncrementalInt;
import org.openstreetmap.pbf2geojson.convertors.IncrementalLong;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleRelation.Member;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;


public class StreamParser extends BinaryParser {
	PrintWriter out;
	Storage storage;
	GeoJSONConvertor convertor;
	WayClassifier classifier;
	boolean nodesDone;
	/*
	public static ThreadLocal<char[]> charTargets = new ThreadLocal<char[]>(){
		@Override
		protected char[] initialValue() {
			return new char[300000];
		}
		
	};
	public StringBuilder builder = new StringBuilder(300000);
	public static ThreadLocal<StringBuilder> builders = new ThreadLocal<StringBuilder>(){
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder(10000);
		}
		
	};
	*/
	public StreamParser(final PrintWriter out, final Storage storage,
			final GeoJSONConvertor convertor, final WayClassifier classifier) {
		super();
		this.out = out;
		this.storage = storage;
		this.convertor = convertor;
		this.classifier = classifier;
		this.nodesDone=false;
	}

	@Override
	protected void parse(HeaderBlock header) {
	}

	@Override
	protected void parseDense(final DenseNodes denseNodes) {
		long lastId = 0, lastLat = 0, lastLon = 0;
		int pos = 0;

		final int size = denseNodes.getLatCount();
		final List<Integer> keyvals = denseNodes.getKeysValsList();

		for (int i = 0; i < size; i++) {
					// sc.start();
			lastLon += denseNodes.getLon(i);
			lastLat += denseNodes.getLat(i);
			lastId += denseNodes.getId(i);
			// sc.endSilent("calculating lastlonids", 0);

			Map<String, Object> curMap = new HashMap<String, Object>(5);

			while (pos < keyvals.size() && keyvals.get(pos) != 0) {
				String k = this.getStringById(keyvals.get(pos++));
				String v = this.getStringById(keyvals.get(pos++));
				curMap.put(k, v);
			}
			pos++;
		
			float lon = (float) this.parseLon(lastLon);
			float lat = (float) this.parseLat(lastLat);

			storage.setNode(lastId, lon, lat);
			if (classifier.isInteresting(curMap)) {
				SimpleNode n = new SimpleNode(lon, lat, lastId, curMap);
				String s = convertor.convertNode(n);
				n = null;
				this.writeNoException(s);
			}
		
		}

	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		nodes.stream().map(this::fromNode).map(storage::setNode)
				.filter(n -> classifier.isInteresting(n.getProperties()))
				.map(convertor::convertNode)// .sequential()
				.forEach(this::writeNoException);

	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		if (rel.size() == 0)
			return;
		rel.stream().map(this::fromRelation).map(storage::setRelation)
		// .filter(classifier::isInteresting)
				.map(convertor::convertRelation)
				// .sequential()
				.forEach(this::writeNoException);

		// log.info("There are "+rel.size()+ " relations in this block");
		// rel.get(0).
	}

	@Override
	protected void parseWays(List<Way> ways) {
		if (ways.size() == 0)
			return;
		if(!nodesDone)
		{
			nodesDone=true;
			this.storage.finalizeNodes();
		}
		
		int maxWays = ways.stream().mapToInt(s-> s.getRefsCount()).max().getAsInt();
		final SimpleWay target = new SimpleWay(maxWays+1, new long[maxWays+1], 0, new HashMap<String, Object>());
		final char[] charTarget = new char[maxWays*30];
		
		StringBuilder builder = new StringBuilder(maxWays*30);
		//char[] charTarget = charTargets.get();
		ways.stream()
		.map(w -> this.copyFromWay(w, target))
		//.map(this::fromWay)
		.map(storage::setWay)
				.filter(classifier::isInteresting)
				.map(convertor::parseWay)
				.map(g -> ConvertorUtils.wayToString(g, builder))
				.map(buf ->{builder.append('\n'); buf.getChars(0, builder.length(), charTarget, 0); return charTarget;})
				.forEach(chars -> this.writeNoException(chars, builder.length()));

	}

	protected void writeNoException(String str) {
		out.println(str);
	}
	protected void writeNoException(char[] buf, int length) {
		//buf.chars().forEach(out::print);
		out.write(buf,0, length);
		//print(buf);
		//(buf, length);
	}
	
/*
	protected Map<String, Object> getPropsForPosition(IncrementalLong position,
			List<Integer> keyvals) {
		Map<String, Object> curMap = new HashMap<String, Object>();
		int i = (int) position.getCount();
		while (i < keyvals.size() && keyvals.get(i) != 0) {
			String k = this.getStringById(keyvals.get(i++));
			String v = this.getStringById(keyvals.get(i++));
			curMap.put(k, v);
		}
		i++;
		position.incr(i - position.getCount());
		return curMap;
	}*/

	protected SimpleNode fromDenseNode(DenseNodes denseNodes, int i,
			Map<String, Object> props) {

		return new SimpleNode((float) this.parseLon(denseNodes.getLon(i)),
				(float) this.parseLat(denseNodes.getLat(i)),
				denseNodes.getId(i), props);
	}

	protected SimpleNode fromNode(Node node) {
		SimpleNode sn = new SimpleNode((float) this.parseLon(node.getLon()),
				(float) this.parseLat(node.getLat()), node.getId(),
				ConvertorUtils.getProperties(node.getKeysList(),
						node.getValsList(), this::getStringById));
		return sn;
	}

	protected SimpleWay fromWay(Way way) {
		final IncrementalLong refLong = new IncrementalLong();
		long[] coordinates = way.getRefsList().stream()
				.mapToLong(refLong::incr).toArray();
		Map<String, Object> props = ConvertorUtils.getProperties(
				way.getKeysList(), way.getValsList(), this::getStringById);
		SimpleWay w = new SimpleWay(coordinates.length, coordinates, way.getId(), props);
		return w;
	}
	
	protected SimpleWay copyFromWay(Way way, SimpleWay target) {
		final IncrementalLong refLong = new IncrementalLong();
		final IncrementalInt index = new IncrementalInt(-1);
		final long[] coordinates = target.getRefList();
		way.getRefsList().stream().
				forEach(ref -> coordinates[index.incr(1)]=refLong.incr(ref.longValue()));
				//.mapToLong(refLong::incr).toArray();
		target.setRefListLength(index.getCount()+1);
		ConvertorUtils.getProperties(
				way.getKeysList(), way.getValsList(), this::getStringById, target.getProperties());
		target.setRef(way.getId());
		return target;
	}

	protected SimpleRelation fromRelation(Relation relation) {
		Map<String, Object> props = ConvertorUtils.getProperties(
				relation.getKeysList(), relation.getValsList(),
				this::getStringById);
		long id = 0;
		final Member[] members = new Member[relation.getMemidsCount()];
		for (int i = 0; i < relation.getMemidsCount(); i++) {
			id += relation.getMemids(i);
			Member m = new Member();
			m.setRef(id);
			m.setType(relation.getTypes(i));
			m.setRole(this.getStringById(relation.getRolesSid(i)));
			members[i] = m;
		}
		SimpleRelation rel = new SimpleRelation();
		rel.setMembers(members);
		rel.setProperties(props);
		rel.setType((String) props.get("type"));
		return rel;
	}

	public void complete() {
		// this.out.close();

	}

}
