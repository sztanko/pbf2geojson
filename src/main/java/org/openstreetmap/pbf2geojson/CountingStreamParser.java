package org.openstreetmap.pbf2geojson;

import java.util.List;

import lombok.Data;

import org.openstreetmap.pbf.BinaryParser;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public class CountingStreamParser extends BinaryParser {

	@Data
	public static class Stats {
		private long nodeCount;
		private long wayCount;
		private long nodeRefCount;
		private long relationCount;
	}

	private final Stats stats;

	public CountingStreamParser(Stats stats) {
		this.stats = stats;
	}

	@Override
	protected void parseDense(final DenseNodes denseNodes) {
		stats.setNodeCount(stats.getNodeCount() + denseNodes.getIdCount());
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		stats.setNodeCount(stats.getNodeCount() + nodes.size());
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		stats.setRelationCount(stats.getRelationCount() + rel.size());
	}

	@Override
	protected void parseWays(List<Way> ways) {
		int sum = ways.stream().mapToInt(w -> w.getRefsCount()).sum();
		stats.setWayCount(stats.getWayCount() + ways.size());
		stats.setNodeRefCount(stats.getNodeRefCount() + sum);
	}

	@Override
	protected void parse(HeaderBlock header) {

	}

	@Override
	public void complete() {

	}

}
