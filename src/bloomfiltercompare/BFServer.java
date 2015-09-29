package bloomfiltercompare;

import simulator.HashProvider;
import simulator.Server;

public class BFServer extends Server {

	public BFServer(int cacheSize, int bloomFilterSize,	long serverId) {
		super(cacheSize, serverId, bloomFilterSize);
	}

	public void populateBF(String data){
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			bloomFilter[i] = 1;
		}
	}
}
