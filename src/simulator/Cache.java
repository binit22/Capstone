package simulator;

public class Cache {
	private DataBlock[] cache;

	public Cache(int cacheSize) {
		this.cache = new DataBlock[cacheSize];
	}

	public DataBlock getDataBlock(int index) {
		return cache[index];
	}
	
	public void populateCache(String[] data, int startIndex, int endIndex) {
		int i = 0;
		for(int index = startIndex; index < endIndex; index++)
			this.cache[i++] = new DataBlock(data[index]);
	}

	public int findDataBlock(String data) {
		for(int i = 0; i < this.cache.length; i++) {
			if(this.cache[i].isEqual(data))
				return i;
		}
		return -1;
	}
}
