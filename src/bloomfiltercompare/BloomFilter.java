package bloomfiltercompare;

import java.util.List;

public interface BloomFilter {

	public void setFalsePositives(long count);
	
	public long getFalsePositives();
	
	public void initialize(int clientCacheSize, int serverCacheSize, int bloomFilterSize, String[] data);
	
	public void calculateFalsePositives(List<String> requests);
}
