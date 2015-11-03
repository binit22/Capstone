package collabcaching;

import simulator.DataBlock;
import simulator.Helper;

public class N_ChanceClient extends CachingAlgoClient {

	private static int recirculationCount;
	
	public N_ChanceClient(int clientId, int noOfClients, int cacheSize, double localCacheAccessTime, double remoteCacheAccessTime, CachingAlgoServer server) {
		super(clientId, cacheSize, localCacheAccessTime, remoteCacheAccessTime, server);
		recirculationCount = noOfClients;
	}

	public void updateClientCacheWithSinglet(DataBlock singlet) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		
		// finding LRU block that is not a singlet
		for(int i = 0; i < cacheSize; i++) {
			if(blockLRUCountInCache[i] < minLRUCount && !((N_ChanceServer)cachingAlgoServer).isSinglet(clientCache.getDataBlock(i), this)) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		// if all are singlets then replace LRU singlet
		if(minLRUIndex == -1) {
			minLRUCount = Helper.LRU_MAX_COUNT;
			for(int i = 0; i < cacheSize; i++) {
				if(blockLRUCountInCache[i] <= minLRUCount) {
					minLRUCount = blockLRUCountInCache[i];
					minLRUIndex = i;
				}
			}
		}
		clientCache.updateCache(minLRUIndex, singlet);
		blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
	}
	
	@Override
	public void updateClientCache(DataBlock data) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		boolean isSingletForwarded = false;

		// finding LRU block
		for(int i = 0; i < cacheSize; i++) {
			if(blockLRUCountInCache[i] < minLRUCount) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		if(minLRUCount > 0 && minLRUCount <= Helper.LRU_MAX_COUNT) {
			DataBlock dataBlock = clientCache.getDataBlock(minLRUIndex);
			
			if(((N_ChanceServer)cachingAlgoServer).isSinglet(dataBlock, this)) {
				
				// recently became singlet
				if(dataBlock.recirculationCount == -1) {
					dataBlock.recirculationCount = N_ChanceClient.recirculationCount;
					((N_ChanceServer)cachingAlgoServer).forwardSinglet(dataBlock, this);
					isSingletForwarded = true;
				}
				else if(dataBlock.recirculationCount > 0) {
					dataBlock.recirculationCount--;
					if(dataBlock.recirculationCount != 0) {
						((N_ChanceServer)cachingAlgoServer).forwardSinglet(dataBlock, this);
						isSingletForwarded = true;
					}
				}
			}
			else{
				// helps to recirculate if becomes singlet in future
				dataBlock.recirculationCount = -1;
			}
			
			clientCache.updateCache(minLRUIndex, data);
			blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
			
			if(!isSingletForwarded) {
				// updating manager
				cachingAlgoServer.updateServerCache(dataBlock);
			}
		}
	}

	@Override
	public boolean searchDataBlock(double accessTimePerRequest, double cacheMiss, double localCacheHit, double globalCacheHit, int networkHops, 
			CachingAlgoClient requesterClient, String dataBlock, boolean requestedByServer) {
		
		networkHops++;		
		int index = findDataBlockInCache(dataBlock);
		accessTimePerRequest += localCacheAccessTime;
		
		if(requesterClient == null)
			requesterClient = this;

		decrementLRUCountForEachBlockExcept(index);
		
		if(index != -1) {
//			accessTimePerRequest += remoteCacheAccessTime;
			if(requesterClient.getClientId() == this.clientId)
				localCacheHit++;
			else
				globalCacheHit++;
			
			if(blockLRUCountInCache[index] != Helper.LRU_MAX_COUNT)
				blockLRUCountInCache[index]++;
			
			if(clientCache.getDataBlock(index).recirculationCount > -1)
				clientCache.getDataBlock(index).recirculationCount = N_ChanceClient.recirculationCount; 
			
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

	@Override
	public boolean contains(String data) {
		return (findDataBlockInCache(data) != -1);
	}
}
