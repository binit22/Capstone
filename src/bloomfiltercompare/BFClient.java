package bloomfiltercompare;

import simulator.Client;
import simulator.HashProvider;

public class BFClient extends Client {

	public BFClient(int cacheSize, int bloomFilterSize, long clientId) {
		super(cacheSize, clientId, bloomFilterSize);
	}
	
	public void updateBF(String data){
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			bloomFilter[i] = 1;
		}
	}
}
