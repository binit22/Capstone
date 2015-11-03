package collabcaching;

import simulator.DataBlock;
import simulator.Helper;

public class GreedyForwardingClient extends CachingAlgoClient {

	public GreedyForwardingClient(int clientId, int cacheSize, double localCacheAccessTime, double remoteCacheAccessTime, CachingAlgoServer server) {
		super(clientId, cacheSize, localCacheAccessTime, remoteCacheAccessTime, server);
	}
	
	@Override
	public void updateClientCache(DataBlock dataBlock) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		
		for(int i = 0; i < cacheSize; i++) {
			if(blockLRUCountInCache[i] < minLRUCount) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		if(minLRUCount > 0 && minLRUCount <= Helper.LRU_MAX_COUNT) {
			DataBlock block = clientCache.getDataBlock(minLRUIndex);
			clientCache.updateCache(minLRUIndex, dataBlock);
			blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
			cachingAlgoServer.updateServerCache(block);
		}
	}
	
	@Override
	public boolean contains(String data) {
		return clientCache.findDataBlock(data) == -1;
	}
}
