package org.openstreetmap.pbf2geojson.data;

import java.util.List;
import java.util.Map;

import crosby.binary.Osmformat.Relation.MemberType;
import lombok.Data;

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
