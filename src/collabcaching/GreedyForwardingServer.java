package collabcaching;

import simulator.DataBlock;
import simulator.Helper;

public class GreedyForwardingServer extends CachingAlgoServer {

	public GreedyForwardingServer(int serverId, int cacheSize, int diskSize, double localCacheAccessTime,double diskAccessTime, double remoteCacheAccessTime){
		super(serverId, cacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
	}
	
	@Override
	public void updateServerCache(DataBlock dataBlock) {
		int minLRUCount = Helper.LRU_MAX_COUNT;
		int minLRUIndex = -1;
		
		for(int i = 0; i < cacheSize; i++) {
			if(blockLRUCountInCache[i] < minLRUCount) {
				minLRUCount = blockLRUCountInCache[i];
				minLRUIndex = i;
			}
		}
		
		if(minLRUCount > 0 && minLRUCount <= 10) {
			serverCache.updateCache(minLRUIndex, dataBlock);
			blockLRUCountInCache[minLRUIndex] = Helper.LRU_MAX_COUNT;
		}
	}
	
	@Override
	public boolean contains(String data) {
		return serverCache.findDataBlock(data) == -1;
	}
}
