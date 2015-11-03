package collabcaching;

import simulator.DataBlock;
import simulator.Helper;

public class RobinhoodClient extends CachingAlgoClient {

	private static int reCirculationCount;

	public RobinhoodClient(int clientId, int noOfClients, int cacheSize, double localCacheAccessTime, double remoteCacheAccessTime, CachingAlgoServer server) {
		super(clientId, cacheSize, localCacheAccessTime, remoteCacheAccessTime, server);
		reCirculationCount = noOfClients;
	}

	public void updateCache(DataBlock singlet, int victimChunkId) {
		
		DataBlock victimChunk = clientCache.getDataBlock(victimChunkId);
		clientCache.updateCache(victimChunkId, singlet);
		((RobinhoodServer)cachingAlgoServer).updateBlockClientList(victimChunk);
		// update manager(global cache)
		cachingAlgoServer.updateServerCache(victimChunk);
	}

	public void updateClientCacheWithSinglet(DataBlock singlet) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		
		// finding LRU block that is not a singlet
		for (int i = 0; i < cacheSize; i++) {
			if (blockLRUCountInCache[i] < minLRUCount && !((RobinhoodServer)cachingAlgoServer).isSinglet(clientCache.getDataBlock(i), this)) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		// if all are singlets then replace LRU singlet
		if (minLRUIndex == -1) {
			minLRUCount = Helper.LRU_MAX_COUNT;
			for (int i = 0; i < cacheSize; i++) {
				if (blockLRUCountInCache[i] <= minLRUCount) {
					minLRUCount = blockLRUCountInCache[i];
					minLRUIndex = i;
				}
			}
		}
		clientCache.updateCache(minLRUIndex, singlet);
		((RobinhoodServer)cachingAlgoServer).updateBlockClientList(clientCache.getDataBlock(minLRUIndex));
		blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
	}

	@Override
	public void updateClientCache(DataBlock dataBlock) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		boolean isSingletForwarded = false;
		
		// finding LRU block
		for (int i = 0; i < cacheSize; i++) {
			if (blockLRUCountInCache[i] < minLRUCount) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		DataBlock blockLRU = clientCache.getDataBlock(minLRUIndex);
		
		if (minLRUCount > 0 && minLRUCount <= Helper.LRU_MAX_COUNT) {

			if (((RobinhoodServer)cachingAlgoServer).isSinglet(blockLRU, this)) {
				int victimChunkId = ((RobinhoodServer)cachingAlgoServer).getVictimChunk(this);
				
				if (victimChunkId == -1)
					isSingletForwarded = performNChance(blockLRU);
				else
					isSingletForwarded = performRobinhood(blockLRU, victimChunkId);
			}
			else {
				blockLRU.recirculationCount = RobinhoodClient.reCirculationCount;
			}
			
			clientCache.updateCache(minLRUIndex, dataBlock);
			blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
			
			if (!isSingletForwarded)
				cachingAlgoServer.updateServerCache(blockLRU);
			
			((RobinhoodServer)cachingAlgoServer).updateBlockClientList(dataBlock);
			((RobinhoodServer)cachingAlgoServer).updateBlockClientList(blockLRU);
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
//			ticksPerRequest += remoteCacheAccessTime;			
			if(requesterClient.getClientId() == this.clientId)
				localCacheHit++;
			else
				globalCacheHit++;
			
			if(blockLRUCountInCache[index] != Helper.LRU_MAX_COUNT) {
				blockLRUCountInCache[index]++;
			}
			
			if(clientCache.getDataBlock(index).recirculationCount > -1) {
				clientCache.getDataBlock(index).recirculationCount = RobinhoodClient.reCirculationCount; 
			}
			
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
		return findDataBlockInCache(data) != -1;
	}
	
	private boolean performNChance(DataBlock block) {
		// recently became singlet
		if(block.recirculationCount == -1) {
			block.recirculationCount = RobinhoodClient.reCirculationCount;
			((RobinhoodServer)cachingAlgoServer).forwardSinglet_NChance(block, this);
			return true;
		}
		else if(block.recirculationCount > 0) {
			block.recirculationCount--;
			if(block.recirculationCount != 0) {
				((RobinhoodServer)cachingAlgoServer).forwardSinglet_NChance(block, this);
				return true;
			}
		}
		return false;
	}
	
	private boolean performRobinhood(DataBlock victimChunk, int victimChunkId) {
		
		if (victimChunk.recirculationCount > 0) {
			victimChunk.recirculationCount--;
			((RobinhoodServer)cachingAlgoServer).victimClient.updateCache(victimChunk, victimChunkId);
			return true;
		}
		else if (victimChunk.recirculationCount < 0) {
			victimChunk.recirculationCount = RobinhoodClient.reCirculationCount;
			((RobinhoodServer)cachingAlgoServer).victimClient.updateCache(victimChunk, victimChunkId);
			return true;
		}
		return false;
	}
}
