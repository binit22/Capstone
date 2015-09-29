package simulator;

public abstract class Client {

	protected long clientId;
	protected Cache cache;
	protected int cacheSize;
	protected int[] bloomFilter;
	protected int bloomFilterSize;
	
	public Client(int cacheSize, long clientId, int bloomFilterSize) {
		this.cache = new Cache(cacheSize);
		this.cacheSize = cacheSize;
		this.clientId = clientId;
		this.bloomFilter = new int[bloomFilterSize];
		this.bloomFilterSize = bloomFilterSize;
	}
	
	public long getClientId() {
		return this.clientId;
	}
	
	public Cache getServerCache() {
		return this.cache;
	}

	public int getCacheSize() {
		return this.cacheSize;
	}

	public int findDataBlockInCache(String data) {
		return this.cache.findDataBlock(data);
	}
	
	public DataBlock getDataBlockFromCache(int index) {
		return this.cache.getDataBlock(index);
	}

	public boolean equals(Object client) {
		return this.clientId == ((Client)client).getClientId();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean bloomFilterContains(String data) {
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			if(bloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
	public void initializeClientCache(String[] data, int startIndex, int endIndex) {
		this.cache.populateCache(data, startIndex, endIndex);
		
		for(int index = startIndex; index < endIndex; index++) {			
			updateBF(data[index]);
		}
	}

	public abstract void updateBF(String data);
}
