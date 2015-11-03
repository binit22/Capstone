package bloomfiltercompare;

import simulator.HashProvider;
import simulator.Server;

public class BFServer extends Server {

	public BFServer(int cacheSize, int bloomFilterSize,	int serverId) {
		super(cacheSize, serverId, bloomFilterSize, cacheSize);
	}

	public void populateBF(String data){
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			bloomFilter[i] = 1;
		}
	}
	
	public boolean initializeServerCache(String[] data, int startIndex, int endIndex) {
		this.serverCache.populateCache(data, startIndex, endIndex);
		
		for(int index = startIndex; index < endIndex; index++) {
			populateBF(data[index]);
		}
		return true;
	}
	
	public boolean contains(String data) {
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			if(bloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
}
