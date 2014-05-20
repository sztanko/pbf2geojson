package org.openstreetmap.pbf2geojson.data;

import java.util.Map;

import lombok.Data;
import crosby.binary.Osmformat.Relation.MemberType;

@Data
public class SimpleRelation {
private Map<String, Object> properties;
private Member[] members;
private String type;
private long ref;
@Data
public static class Member{
	long ref;
	MemberType type;
	String role;
}
}
