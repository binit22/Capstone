package collabcaching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import simulator.DataBlock;
import simulator.Helper;
import simulator.Server;

public abstract class CachingAlgoServer extends Server {

	protected int noOfClients;
	protected double localCacheAccessTime;
	protected double remoteCacheAccessTime;
	protected double diskAccessTime;
	protected int networkHops;
	protected int[] blockLRUCountInCache;
	protected List<Set<Integer>> clientsDataSetList;
	protected CachingAlgoClient[] cachingAlgoClients;
	
	public CachingAlgoServer(int serverId, int cacheSize, int diskSize, double localCacheAccessTime, double diskAccessTime, double remoteCacheAccessTime){
		super(cacheSize, serverId, cacheSize, diskSize);
		
		this.localCacheAccessTime = localCacheAccessTime;
		this.diskAccessTime = diskAccessTime;
		this.remoteCacheAccessTime = remoteCacheAccessTime;
		this.clientsDataSetList = new ArrayList<Set<Integer>>();
		this.blockLRUCountInCache = new int[cacheSize];
	}
	
	public boolean initializeServerCache(String[] data, int startIndex, int endIndex) {
		super.initializeServerCache(data, startIndex, endIndex);
		
		Random random = new Random();
		for(int i = 0; i < cacheSize; i++)
			this.blockLRUCountInCache[i] = random.nextInt(Helper.LRU_MAX_COUNT) + Helper.LRU_MIN_COUNT;

		return true;
	}

	public void initializeClientsInSystem(CachingAlgoClient[] cachingAlgoClients) {
		
		noOfClients = cachingAlgoClients.length;
		this.cachingAlgoClients = new CachingAlgoClient[noOfClients];
		
		for(int i = 0; i < noOfClients; i++) {
			this.clientsDataSetList.add(new HashSet<Integer>());
			this.cachingAlgoClients[i] = cachingAlgoClients[i];
		}
	}
	
	public void updateClientsContentsInSystem() {
		
		for(int i = 0; i < noOfClients; i++) {
			CachingAlgoClient cachingAlgoClient = cachingAlgoClients[i];
			Set<Integer> clientDataSet = clientsDataSetList.get(i);
			
			for(int j = 0; j < cachingAlgoClient.getCacheSize(); j++)
				clientDataSet.add(cachingAlgoClient.getDataBlockFromCache(j).getData().hashCode());
		}
	}
	
	public abstract void updateServerCache(DataBlock dataBlock);
	
	public boolean searchDataBlock(double accessTimePerRequest, double cacheMiss, double localCacheHit, double globalCacheHit, 
			int networkHops, CachingAlgoClient requesterClient, String dataBlock) {
		
		networkHops++;

		for(int i = 0; i < noOfClients; i++) {
			if(requesterClient.getClientId() != cachingAlgoClients[i].getClientId() && clientsDataSetList.get(i).contains(dataBlock.hashCode())) {
				accessTimePerRequest += remoteCacheAccessTime;
				if(cachingAlgoClients[i].searchDataBlock(accessTimePerRequest, cacheMiss, localCacheHit, globalCacheHit, networkHops, requesterClient, dataBlock, true))
					return true;
			}
		}
		
		accessTimePerRequest += localCacheAccessTime;
		int index = findDataBlockInCache(dataBlock);
		
		decrementLRUCountForEachBlockExcept(index);
		
		if(index != -1) {
			globalCacheHit += 1;
			accessTimePerRequest += remoteCacheAccessTime;
			
			if(blockLRUCountInCache[index] != Helper.LRU_MAX_COUNT)
				blockLRUCountInCache[index]++;
			
			requesterClient.setEvaluationResults(serverCache.getDataBlock(index), accessTimePerRequest, cacheMiss, localCacheHit, globalCacheHit, networkHops);
			return true;
		}
		
		cacheMiss++;
		index = disk.findDataBlock(dataBlock);
		
		if(index != -1) {
			accessTimePerRequest += diskAccessTime;
			requesterClient.setEvaluationResults(disk.getDataBlock(index), accessTimePerRequest, cacheMiss, localCacheHit, globalCacheHit, networkHops);
			requesterClient.updateClientCache(disk.getDataBlock(index));
		}
		return true;
	}
	
	protected void decrementLRUCountForEachBlockExcept(int index) {
		
		for (int i = 0; i < blockLRUCountInCache.length; i++)
			if (i != index && blockLRUCountInCache[i] > 0)
				blockLRUCountInCache[i]--;
	}
}
