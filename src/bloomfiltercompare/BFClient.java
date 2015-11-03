package bloomfiltercompare;

import simulator.Client;
import simulator.HashProvider;

public class BFClient extends Client {

	public BFClient(int cacheSize, int bloomFilterSize, int clientId) {
		super(cacheSize, clientId, bloomFilterSize);
	}
	
	public void updateBF(String data){
		HashProvider hash = new HashProvider(data, bloomFilterSize);
		int[] bitPositions = hash.getBitPositions();
		for(int i : bitPositions) {
			bloomFilter[i] = 1;
		}
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
	
	public boolean initializeClientCache(String[] data, int startIndex, int endIndex) {
		this.clientCache.populateCache(data, startIndex, endIndex);
		
		for(int index = startIndex; index < endIndex; index++) {			
			updateBF(data[index]);
		}
		return true;
	}
}
