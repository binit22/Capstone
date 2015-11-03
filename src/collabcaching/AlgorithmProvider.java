package collabcaching;

import simulator.Helper.Algorithms;

public class AlgorithmProvider {
	
	public static Algorithm createAlgorithm(Algorithms algorithm, int noOfClients, int clientCacheSize, int serverCacheSize,
			int diskSize, double localCacheAccessTime, double diskAccessTime, double remoteCacheAccessTime) {
		
		Algorithm algo = null;
		switch(algorithm) {
		case GreedyForwarding:
			algo = new GreedyForwarding(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime,	remoteCacheAccessTime);
			break;
		case NChance:
			algo = new N_Chance(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
			break;
		case Robinhood:
			algo = new RobinHood(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
			break;
		default:
			break;
		}
		return algo;
	}
}
