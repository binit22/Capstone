package bloomfiltercompare;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import simulator.Client;
import simulator.HashProvider;

public class IABFClient extends Client{

	private final int dataSizeLimit = 50;
	private final int M = 7;
	private final int P = 10;

	public IABFClient(int cacheSize, int bloomFilterSize, int clientId) {
		super(cacheSize, clientId, bloomFilterSize);
	}

	public void updateBF(String data) {
		if(!contains(data)) {
			Random random = new Random();
			List<Integer> pIndexList = new ArrayList<Integer>();
			
			while(pIndexList.size() != P) {
				int index = random.nextInt(bloomFilterSize);
				if(!pIndexList.contains(index)) {
					pIndexList.add(index);
				}
			}
			for(int index : pIndexList) {
				if(bloomFilter[index] >= 1) {
					bloomFilter[index]--;
				}
			}
			HashProvider hash = new HashProvider(data, bloomFilterSize);
			int[] bitPositions = hash.getBitPositions();
			for(int i : bitPositions) {
				int impValue = importanceFunction(data);
				if(bloomFilter[i] < impValue)
					bloomFilter[i] = impValue;
			}
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
	
	// Importance Aware Bloom Filter 2-C
	private int importanceFunction(String data) {
		if(data.length() < dataSizeLimit) {
			return (M/2);
		}
		return M;
	}
}
