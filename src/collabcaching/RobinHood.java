package collabcaching;

import java.util.List;

public class RobinHood extends Algorithm{

	public RobinHood(int noOfClients, int clientCacheSize, int serverCacheSize, int diskSize, double localCacheAccessTime,
			double diskAccessTime, double remoteCacheAccessTime) {
		super(noOfClients, clientCacheSize, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
	}

	@Override
	public void initializeAlgo(String[][] clientCaches, String[] serverCache, String[] serverDisk) {
		activeClients = new RobinhoodClient[noOfClients];
		activeServer = new RobinhoodServer(1, serverCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);
		
		for(int i = 0; i < noOfClients; i++) {
			activeClients[i] = new RobinhoodClient(i, noOfClients, clientCacheSize, localCacheAccessTime, remoteCacheAccessTime, (RobinhoodServer)activeServer);
			activeClients[i].initializeClientCache(clientCaches[i], 0, clientCaches[i].length);
		}
		
		((RobinhoodServer)activeServer).initializeClientsInSystem((RobinhoodClient[])activeClients);
		((RobinhoodServer)activeServer).updateClientsContentsInSystem();
		
		activeServer.initializeServerCache(serverCache, 0, serverCache.length);
		activeServer.initializeServerDisk(serverDisk, 0, serverDisk.length);
		
		// update block's client list
		((RobinhoodServer)activeServer).updateAllBlockClientList();
	}
	
	@Override
	public void execute(List<String> requests) {
		super.execute(requests);
	}
}
