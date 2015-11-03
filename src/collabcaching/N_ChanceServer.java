package collabcaching;

import java.util.Random;

import simulator.DataBlock;
import simulator.Helper;

public class N_ChanceServer extends CachingAlgoServer {

	public N_ChanceServer(int serverId, int cacheSize, int diskSize, double localCacheAccessTime, double diskAccessTime, double remoteCacheAccessTime){
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
			Random random = new Random();
			serverCache.updateCache(minLRUIndex, dataBlock);
			blockLRUCountInCache[minLRUIndex] =  random.nextInt(Helper.LRU_MAX_COUNT) + Helper.LRU_MIN_COUNT;
		}
	}
	
	public void forwardSinglet(DataBlock singlet, CachingAlgoClient forwardingClient) {
		Random random = new Random();
		int clientId = -1;
		int forwardingClientId = forwardingClient.getClientId();
		int noOfClientsCovered = 0;
		
		while(noOfClientsCovered < noOfClients) {
			clientId = random.nextInt(noOfClients);
			
			if(clientId != forwardingClientId)
				break;
			
			noOfClientsCovered++;
		}
		
		if(clientId != forwardingClientId) {
			((N_ChanceClient)cachingAlgoClients[clientId]).updateClientCacheWithSinglet(singlet);
			super.updateClientsContentsInSystem();
		}
	}
	
	public boolean isSinglet(DataBlock block, N_ChanceClient requesterClient) {
		
		for(CachingAlgoClient client : cachingAlgoClients)
			if(requesterClient.getClientId() != client.getClientId() && client.findDataBlockInCache(block.getData()) != -1)
				return false;
		
		return true;
	}
	
	@Override
	public boolean contains(String data) {
		return findDataBlockInCache(data) != -1;
	}
}
