package simulator;

import java.util.HashSet;
import java.util.Set;

public class DataBlock {
	private String data;
	public int recirculationCount;
	public boolean isCached;
	public Set<Integer> clientList;
	
	public DataBlock(String data) {
		this.data = data;
		this.recirculationCount = -1;
		this.clientList = new HashSet<Integer>();
//		isCached = false;
	}
	
	public String getData() {		
		return this.data;
	}
	
	public int compareTo(DataBlock dataBlock) {
		return this.data.compareTo(dataBlock.getData());
	}
	
	/*public boolean isEqual(String data) {
		return this.data.equals(data)? true: false;
	}*/
	
	public int hashCode() {
		return this.data.hashCode();
	}

	public String toString() {
		return this.data;
	}
}
