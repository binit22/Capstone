package bloomfiltercompare;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bloomfiltercompare.BFClient;
import bloomfiltercompare.BFServer;

import simulator.Client;
import simulator.Server;

public class BloomFilterImplementation implements BloomFilter {

	private int noOfClients;
	private int bloomFilterType;

	private long noOfFalsePositives;

	private Client[] clients;
	private Server server;

	public BloomFilterImplementation(int filterType, int noOfClients){
		this.noOfClients = noOfClients;
		this.bloomFilterType = filterType;
		this.noOfFalsePositives = 0;
	}

	public int getFilterType(){
		return this.bloomFilterType;
	}

	public long getFalsePositives(){
		return this.noOfFalsePositives;
	}

	public void setFalsePositives(long count) {
		this.noOfFalsePositives = count;
	}

	public void initialize(int clientCacheSize, int serverCacheSize, int bloomFilterSize, String[] data) {

		if(bloomFilterType == 1){
			clients = new Client[noOfClients];
			
			for (int i = 0; i < noOfClients; i++) {
				clients[i] = new BFClient(clientCacheSize, bloomFilterSize, i);
				clients[i].initializeClientCache(data, (i * clientCacheSize), (clientCacheSize * (i + 1)));
			}

			server = new BFServer(serverCacheSize, bloomFilterSize, 1);
			server.initializeServerCache(data, (noOfClients * clientCacheSize), ((noOfClients * clientCacheSize) + serverCacheSize));
		}

		if(bloomFilterType == 2){
			clients = new Client[noOfClients];
			
			for (int i = 0; i < noOfClients; i++) {				
				clients[i] = new IABFClient(clientCacheSize, bloomFilterSize, i);
				clients[i].initializeClientCache(data, (i * clientCacheSize), (clientCacheSize * (i + 1)));
			}

			server = new IABFServer(serverCacheSize, bloomFilterSize, 1);
			server.initializeServerCache(data, (noOfClients * clientCacheSize), ((noOfClients * clientCacheSize) + serverCacheSize));
		}
	}

	public void calculateFalsePositives(List<String> queries) {
		Random random = new Random();

		for(String query : queries) {
			List<Integer> clientsVisited = new ArrayList<Integer>();

			while(clientsVisited.size() != noOfClients) {
				int index = random.nextInt(noOfClients);

				if(!clientsVisited.contains(index)) {
					
					if(clients[index].bloomFilterContains(query)) {
						
						if(clients[index].findDataBlockInCache(query) == -1) {
							noOfFalsePositives += 1;
						} else {
							break;
						}
					}
					clientsVisited.add(index);
				}
			}

			if(clientsVisited.size() == noOfClients) {
				if(server.bloomFilterContains(query)) {
					if(server.findDataBlockInCache(query) == -1) {
						noOfFalsePositives += 1;
					}
				}
			}
		}
	}
}
