package simulator;

public abstract class Client {

	protected int clientId;
	protected Cache clientCache;
	protected int cacheSize;
	protected int[] bloomFilter;
	protected int bloomFilterSize;
	
	public Client(int cacheSize, int clientId, int bloomFilterSize) {
		this.clientCache = new Cache(cacheSize);
		this.cacheSize = cacheSize;
		this.clientId = clientId;
		this.bloomFilter = new int[bloomFilterSize];
		this.bloomFilterSize = bloomFilterSize;
	}
	
	public int getClientId() {
		return this.clientId;
	}
	
	public Cache getServerCache() {
		return this.clientCache;
	}

	public int getCacheSize() {
		return this.cacheSize;
	}

	public int findDataBlockInCache(String data) {
		return this.clientCache.findDataBlock(data);
	}
	
	public DataBlock getDataBlockFromCache(int index) {
		return this.clientCache.getDataBlock(index);
	}

	public boolean equals(Object client) {
		return this.clientId == ((Client)client).getClientId();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public abstract boolean contains(String data);
	
	public boolean initializeClientCache(String[] data, int startIndex, int endIndex) {
		if(this.cacheSize != endIndex-startIndex) {
			return false;
		}
		this.clientCache.populateCache(data, startIndex, endIndex);
		return true;
	}
}
