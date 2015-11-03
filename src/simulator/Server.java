package simulator;

public abstract class Server {

	protected int serverId;
	protected Cache serverCache;
	protected Cache disk;
	protected int cacheSize;
	protected int diskSize;
	protected int bloomFilterSize;
	protected int[] bloomFilter;
	
	public Server(int cacheSize, int serverId, int bloomFilterSize, int diskSize) {
		this.serverCache = new Cache(cacheSize);
		this.disk = new Cache(diskSize);
		this.cacheSize = cacheSize;
		this.diskSize = diskSize;
		this.serverId = serverId;
		this.bloomFilterSize = bloomFilterSize;
		this.bloomFilter = new int[bloomFilterSize];
	}

	public int getServerId() {
		return this.serverId;
	}

	public Cache getServerCache() {
		return this.serverCache;
	}

	public int getCacheSize() {
		return this.cacheSize;
	}

	public int findDataBlockInCache(String data) {
		return this.serverCache.findDataBlock(data);
	}

	public DataBlock getDataBlockFromCache(int index) {
		return this.serverCache.getDataBlock(index);
	}
	
	public DataBlock getDataBlockFromDisk(int index) {
		return this.disk.getDataBlock(index);
	}
	
	public boolean equals(Object obj) {
		return this.serverId == ((Server)obj).getServerId();
	}

	public int hashCode() {
		return super.hashCode();
	}

	public abstract boolean contains(String data);
	
	public boolean initializeServerCache(String[] data, int startIndex, int endIndex) {
		if(this.cacheSize != endIndex-startIndex){
			return false;
		}
		this.serverCache.populateCache(data, startIndex, endIndex);
		return true;
	}
	
	public boolean initializeServerDisk(String[] data, int startIndex, int endIndex) {
		if(this.diskSize != endIndex-startIndex){
			return false;
		}
		this.disk.populateCache(data, startIndex, endIndex);
		return true;
	}
}
