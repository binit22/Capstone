package collabcaching;

import java.util.Random;

import simulator.DataBlock;
import simulator.Client;
import simulator.Helper;

public abstract class CachingAlgoClient extends Client {

	protected double localCacheAccessTime;
	protected double remoteCacheAccessTime;
	protected int networkHops;
	protected CachingAlgoServer cachingAlgoServer;
	protected DataBlock block;
	protected double responseTime;
	protected double localCacheHit;
	protected double globalCacheHit;
	protected double cacheMiss;
	protected int[] blockLRUCountInCache;

	public CachingAlgoClient(int clientId, int cacheSize, double localCacheAccessTime, double remoteCacheAccessTime, CachingAlgoServer server) {
		super(cacheSize, clientId, cacheSize);
		
		this.localCacheAccessTime = localCacheAccessTime;
		this.remoteCacheAccessTime = remoteCacheAccessTime;
		this.cachingAlgoServer = server;
		this.blockLRUCountInCache = new int[cacheSize];
	}

	public boolean initializeClientCache(String[] data, int startIndex, int endIndex) {
		super.initializeClientCache(data, startIndex, endIndex);

		Random random = new Random();
		for (int i = 0; i < cacheSize; i++) {
			this.blockLRUCountInCache[i] = random.nextInt(Helper.LRU_MAX_COUNT) + Helper.LRU_MIN_COUNT;
		}
		return true;
	}

	public void setEvaluationResults(DataBlock block, double responseTime, double cacheMiss, double localCacheHit, double globalCacheHit, int networkHops) {
		this.block = block;
		this.responseTime = responseTime;
		this.cacheMiss = cacheMiss;
		this.localCacheHit = localCacheHit;
		this.globalCacheHit = globalCacheHit;
		this.networkHops = networkHops;
	}

	public boolean searchDataBlock(double accessTimePerRequest, double cacheMiss, double localCacheHit, double globalCacheHit, int networkHops, 
			CachingAlgoClient requesterClient, String dataBlock, boolean requestedByServer) {

		networkHops++;
		int index = findDataBlockInCache(dataBlock);
		accessTimePerRequest += localCacheAccessTime;

		if (requesterClient == null)
			requesterClient = this;

		decrementLRUCountForEachBlockExcept(index);

		if (index != -1) {
//			accessTimePerRequest += remoteCacheAccessTime;
			if (requesterClient.getClientId() == this.clientId)
				localCacheHit++;
			else
				globalCacheHit++;

			if (blockLRUCountInCache[index] != Helper.LRU_MAX_COUNT)
				blockLRUCountInCache[index]++;
			
			requesterClient.setEvaluationResults(clientCache.getDataBlock(index), accessTimePerRequest, cacheMiss, localCacheHit, globalCacheHit, networkHops);
		}
		else {
			if (requestedByServer) {
				cachingAlgoServer.updateClientsContentsInSystem();
				return false;
			}
			accessTimePerRequest += remoteCacheAccessTime;
			return cachingAlgoServer.searchDataBlock(accessTimePerRequest, cacheMiss, localCacheHit, globalCacheHit, networkHops, requesterClient, dataBlock);
		}
		return true;
	}

	protected void decrementLRUCountForEachBlockExcept(int index) {
		
		for (int i = 0; i < blockLRUCountInCache.length; i++)
			if (i != index && blockLRUCountInCache[i] > 0)
				blockLRUCountInCache[i]--;
	}

	public abstract void updateClientCache(DataBlock block);

	public double getResponseTime() {
		return responseTime;
	}

	public double getLocalCacheHit() {
		return localCacheHit;
	}

	public double getGlobalCacheHit() {
		return globalCacheHit;
	}

	public double getCacheMiss() {
		return cacheMiss;
	}

	public int getNetworkHops() {
		return networkHops;
	}
}
