package bloomfiltercompare;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import simulator.HashProvider;
import simulator.Server;

public class IABFServer extends Server{

	private final int dataSizeLimit = 50;
	private final int M = 5;
	private final int P = 7;
	
	public IABFServer(int cacheSize, int bloomFilterSize, int serverId) {
		super(cacheSize, serverId, bloomFilterSize, cacheSize);
	}
	
	public void populateBF(String data) {
		if(!contains(data)) {
			List<Integer> pIndexList = new ArrayList<Integer>();

			while(pIndexList.size() != P) {
				Random random = new Random();
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
				if(bloomFilter[i] < importanceFunction(data))
					bloomFilter[i] = importanceFunction(data);
			}
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
	
	// Importance Aware Bloom Filter 2-C
	private int importanceFunction(String data) {
		if(data.length() < dataSizeLimit) {
			return (M/2);
		}
		return M;
	}
}
