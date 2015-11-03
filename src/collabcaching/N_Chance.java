package collabcaching;

import java.util.List;

public class N_Chance extends Algorithm {

	public N_Chance(int noOfClients, int clientCacheSize, int serverCacheSize, int diskSize, double localCacheAccessTime,
			double diskAccessTime, double remoteCacheAccessTime) {
		super(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
	}

	@Override
	public void initializeAlgo(String[][] clientCaches, String[] serverCache, String[] disk) {
		activeClients = new N_ChanceClient[noOfClients];
		activeServer = new N_ChanceServer(1, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
		
		for(int i = 0; i < noOfClients; i++) {
			activeClients[i] = new N_ChanceClient(i, noOfClients, clientCacheSize, localCacheAccessTime, remoteCacheAccessTime, (N_ChanceServer)activeServer);
			activeClients[i].initializeClientCache(clientCaches[i], 0, clientCaches[i].length);
		}
		
		((N_ChanceServer)activeServer).initializeClientsInSystem((N_ChanceClient[])activeClients);
		((N_ChanceServer)activeServer).updateClientsContentsInSystem();
		
		activeServer.initializeServerCache(serverCache, 0, serverCache.length);
		activeServer.initializeServerDisk(disk, 0, disk.length);
	}
	
	@Override
	public void execute(List<String> requests) {
		super.execute(requests);
	}
}
