package org.openstreetmap.pbf2geojson.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SimpleWay implements Serializable {
	private static final long serialVersionUID = 8707652299789374342L;
	private int refListLength;
	private long[] refList;
	private long ref;
	private Map<String, Object> properties;
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleWay other = (SimpleWay) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (ref != other.ref)
			return false;
		if (refListLength != other.refListLength)
			return false;
		for(int i=0;i<this.getRefListLength();i++)
		{
			if(this.refList[i]!=other.refList[i])
				return false;
		}
		
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + (int) (ref ^ (ref >>> 32));
		result = prime * result + Arrays.hashCode(refList);
		result = prime * result + refListLength;
		return result;
	}
}
