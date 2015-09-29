package simulator;

public class DataBlock {
	private String data;
	
	public DataBlock(String data) {
		this.data = data;
	}
	
	public String getData() {		
		return this.data;
	}
	
	public int compareTo(DataBlock dataBlock) {
		return this.data.compareTo(dataBlock.getData());
	}
	
	public boolean isEqual(String data) {
		return this.data.equals(data)? true: false;
	}
	
	public int hashCode() {
		return this.data.hashCode();
	}

	public String toString() {
		return this.data;
	}
}
