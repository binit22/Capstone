package simulator;

public abstract class Server {

	protected long serverId;
	protected Cache cache;
	protected int cacheSize;
	protected int bloomFilterSize;
	protected int[] bloomFilter;
	
	public Server(int cacheSize, long serverId, int bloomFilterSize) {
		this.cache = new Cache(cacheSize);
		this.cacheSize = cacheSize;
		this.serverId = serverId;
		this.bloomFilterSize = bloomFilterSize;
		this.bloomFilter = new int[bloomFilterSize];
	}

	public long getServerId() {
		return this.serverId;
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
	
	public boolean equals(Object obj) {
		return this.serverId == ((Server)obj).getServerId();
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
	
	public void initializeServerCache(String[] data, int startIndex, int endIndex) {
		this.cache.populateCache(data, startIndex, endIndex);
		
		for(int index = startIndex; index < endIndex; index++) {
			populateBF(data[index]);
		}
	}
	
	public abstract void populateBF(String data);
}
