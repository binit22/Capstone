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
	
	public IABFServer(int cacheSize, int bloomFilterSize, long serverId) {
		super(cacheSize, serverId, bloomFilterSize);
	}
	
	public void populateBF(String data) {
		if(!bloomFilterContains(data)) {
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
	
	// Importance Aware Bloom Filter 2-C
	private int importanceFunction(String data) {
		if(data.length() < dataSizeLimit) {
			return (M/2);
		}
		return M;
	}
}
