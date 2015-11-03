package collabcaching;

import java.util.List;
import java.util.Random;

import simulator.Client;
import simulator.Server;

public abstract class Algorithm {
	
	protected int noOfClients;
	protected int clientCacheSize;
	protected int serverCacheSize;
	protected int diskSize;
	protected double localCacheAccessTime;
	protected double diskAccessTime;
	protected double remoteCacheAccessTime;
	protected int networkHops;
	protected int totalRequests;
	protected Client[] activeClients;
	protected Server activeServer;
	protected double accessTimePerRequest;
	protected double localHitPerRequest;
	protected double globalHitPerRequest;
	protected double cacheMissPerRequest;
	
	public Algorithm(int noOfClients, int clientCacheSize, int serverCacheSize, int diskSize, double localCacheAccessTime,
			double diskAccessTime, double remoteCacheAccessTime)
	{
		this.noOfClients = noOfClients;
		this.clientCacheSize = clientCacheSize;
		this.serverCacheSize = serverCacheSize;
		this.diskSize = diskSize;
		this.localCacheAccessTime = localCacheAccessTime;
		this.diskAccessTime = diskAccessTime;
		this.remoteCacheAccessTime = remoteCacheAccessTime;
	}
		
	public void execute(List<String> requestBlocks) {
		
		for(String requestedBlock : requestBlocks) {
			
			CachingAlgoClient client = ((CachingAlgoClient)activeClients[new Random().nextInt(noOfClients)]); 
			client.searchDataBlock(0, 0, 0, 0, 0, null, requestedBlock, false);
			
			this.accessTimePerRequest += client.getResponseTime();
			this.cacheMissPerRequest += client.getCacheMiss();
			this.localHitPerRequest += client.getLocalCacheHit();
			this.globalHitPerRequest += client.getGlobalCacheHit();
			this.networkHops += client.getNetworkHops();
		}
		
		this.accessTimePerRequest = this.accessTimePerRequest / requestBlocks.size();
		this.cacheMissPerRequest = this.cacheMissPerRequest / requestBlocks.size();
		this.localHitPerRequest = this.localHitPerRequest / requestBlocks.size();
		this.globalHitPerRequest = (this.globalHitPerRequest / requestBlocks.size()) * 100;
	}
	
	public double getAccessTimePerRequest() {
		return accessTimePerRequest;
	}
	
	public double getCacheMiss() {
		return cacheMissPerRequest;
	}
	
	public double getLocalCacheHit() {
		return localHitPerRequest;
	}
	
	public double getGlobalCacheHit() {
		return globalHitPerRequest;
	}
	
	public int getNetworkHops() {
		return networkHops;
	}
	
	public abstract void initializeAlgo(String[][] clientCaches, String[] serverCache, String[] serverDisk);
}
