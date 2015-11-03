package collabcaching;

import java.util.List;

public class GreedyForwarding extends Algorithm {

	public GreedyForwarding(int noOfClients, int clientCacheSize, int serverCacheSize, int diskSize, double localCacheAccessTime,
			double diskAccessTime, double remoteCacheAccessTime) {
		super(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
	}

	@Override
	public void initializeAlgo(String[][] clientCaches, String[] serverCache, String[] disk) {
		activeClients = new GreedyForwardingClient[noOfClients];
		activeServer = new GreedyForwardingServer(1, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
		
		for(int i = 0; i < noOfClients; i++) {
			activeClients[i] = new GreedyForwardingClient(i, clientCacheSize, localCacheAccessTime, remoteCacheAccessTime, (GreedyForwardingServer)activeServer);
			activeClients[i].initializeClientCache(clientCaches[i], 0, clientCaches[i].length);
		}
		
		((GreedyForwardingServer)activeServer).initializeClientsInSystem((GreedyForwardingClient[])activeClients);
		((GreedyForwardingServer)activeServer).updateClientsContentsInSystem();
		
		activeServer.initializeServerCache(serverCache, 0, serverCache.length);
		activeServer.initializeServerDisk(disk, 0, disk.length);
	}
	
	@Override
	public void execute(List<String> requests) {
		super.execute(requests);
	}
}
