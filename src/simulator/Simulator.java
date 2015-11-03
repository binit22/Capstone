package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import simulator.Helper.Algorithms;
import collabcaching.Algorithm;
import collabcaching.AlgorithmProvider;
import bloomfiltercompare.BloomFilter;
import bloomfiltercompare.BloomFilterImplementation;

public class Simulator {

	private int goalNo;
	private int minNoOfClients;
	private int maxNoOfClients;
	private int clientIncrementer;
	private int minCacheSize;
	private int maxCacheSize;
	private int cacheSizeIncrementer;
	private int diskSize;
	private int totalRequests;
	private double[] queryPercent;
	private double[] percentBFSize;
	private double localCacheAccessTime;
	private double diskAccessTime;
	private double remoteCacheAccessTime;
	private String inputFile;

	private String[][] clientCaches;
	private String[] serverCache;
	private String[] serverDisk;

	private List<String> data;

	protected Client[] clients;
	protected Server server;
	
	public Simulator() {
		this.data = new ArrayList<String>();
	}

	private boolean readFromConfigFile(String fileName){
		try{
			Properties prop = new Properties();
			prop.load(new FileInputStream(fileName));

			this.goalNo = Integer.parseInt(prop.getProperty("goal"));
			this.minNoOfClients = Integer.parseInt(prop.getProperty("minimumNunberOfClients"));
			this.maxNoOfClients = Integer.parseInt(prop.getProperty("maximumNumberOfClients"));
			this.clientIncrementer = Integer.parseInt(prop.getProperty("clientIncrementer"));
			this.minCacheSize = Integer.parseInt(prop.getProperty("minimumCacheSize"));
			this.maxCacheSize = Integer.parseInt(prop.getProperty("maximumCacheSize"));
			this.cacheSizeIncrementer = Integer.parseInt(prop.getProperty("cacheSizeIncrementer"));
			this.diskSize = Integer.parseInt(prop.getProperty("diskSize"));

			String[] queryPer = prop.getProperty("queryPercentWRTCacheSize").split(Helper.COMMA);
			queryPercent = new double[queryPer.length];
			for(int i = 0; i < queryPer.length; i++)
				queryPercent[i] = Double.parseDouble(queryPer[i]);

			String[] perBFSize = prop.getProperty("bfSizePercentWRTCacheSize").split(Helper.COMMA);
			percentBFSize = new double[perBFSize.length];
			for(int i = 0; i < perBFSize.length; i++)
				percentBFSize[i] = Double.parseDouble(perBFSize[i]);
			
			this.totalRequests = Integer.parseInt(prop.getProperty("totalRequests"));
			this.localCacheAccessTime = Double.parseDouble(prop.getProperty("localCacheAccessTime"));
			this.remoteCacheAccessTime = Double.parseDouble(prop.getProperty("remoteCacheAccessTime"));
			this.diskAccessTime = Double.parseDouble(prop.getProperty("diskAccessTime"));
			this.inputFile = prop.getProperty("inputFileName");

			return true;
		}
		catch(FileNotFoundException ex){
			System.err.println("File Not Found: " + fileName + Helper.COLON + ex.getMessage());
			ex.printStackTrace();
		}
		catch(IOException ex){
			System.err.println("Error Reading File: " + fileName + Helper.COLON + ex.getMessage());
			ex.printStackTrace();
		}
		catch(ClassCastException ex){
			System.err.println("Cannot Cast Class: " + ex.getMessage());
			ex.printStackTrace();
		}
		catch(Exception ex){
			System.err.println("Something Unexpected Occured: " + ex.getMessage());
			ex.printStackTrace();
		}
		return false;
	}

	private boolean readDataFromInputFile() {
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(new File(inputFile)));

			while ((line = br.readLine()) != null)
				this.data.add(line);

			return true;
		}
		catch (FileNotFoundException ex) {
			System.err.println("File Not Found: " + this.inputFile + Helper.COLON + ex.getMessage());
			ex.printStackTrace();
		}
		catch (IOException ex) {
			System.err.println("Error Reading File: " + this.inputFile + Helper.COLON + ex.getMessage());
			ex.printStackTrace();
		}
		catch(Exception ex){
			System.err.println("Something Unexpected Occured: " + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException ex) {
					System.err.println("File could not be closed: " + this.inputFile + Helper.COLON + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	private void initiateGoal() {
		if(this.goalNo == 1) {
			compareBloomFilters();
		}
		else if(this.goalNo == 2) {
			compareCoopCachingAlgo();
		}
		else{
			System.err.println("Goal can be 1 or 2 only!");
		}
	}

	private void compareBloomFilters() {

		BloomFilter bf = new BloomFilterImplementation(1, maxNoOfClients);
		BloomFilter iabf = new BloomFilterImplementation(2, maxNoOfClients);

		File outputFile = null;
		FileWriter fWriter = null;
		try {
			outputFile = new File(this.inputFile + Helper.UNDERSCORE +"output.csv");
			fWriter = new FileWriter(outputFile, true);

			System.out.println("Experiment 1 Starts!");
			compareWRTNoOfQueryRequests(bf, iabf, queryPercent, fWriter);
			System.out.println("Experiment 1 Ends!");

			fWriter.write(Helper.NEW_LINE);

			System.out.println("Experiment 2 Starts!");
			compareWRTBloomFilterSize(bf, iabf, percentBFSize, fWriter);
			System.out.println("Experiment 2 Ends!");
		}
		catch(IOException ex){
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
		finally{
			if(fWriter != null)
				try {
					fWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void compareWRTNoOfQueryRequests(BloomFilter bf, BloomFilter iabf, double[] queryPercent, FileWriter fWriter) throws IOException {

		int cacheSize = this.minCacheSize;
		int noOfClients = this.maxNoOfClients;
		fWriter.write("Cache size");
		for(int i = 0; i < queryPercent.length; i++)
			fWriter.write(Helper.COMMA + "Total Cache Size" + Helper.SLASH + "No Of Queries" + Helper.COMMA + "Bloom Filter" + Helper.COMMA + "Importance Aware Bloom Filter");
		fWriter.write(Helper.NEW_LINE);

		while (cacheSizeIncrementer > 0 && cacheSize <= maxCacheSize) {
			int cacheSizeInSystem = (noOfClients + 1) * cacheSize;

			bf.initialize(cacheSize, cacheSize, cacheSize, data.toArray(new String[data.size()]));
			iabf.initialize(cacheSize, cacheSize, cacheSize, data.toArray(new String[data.size()]));

			fWriter.write(new Integer(cacheSize).toString());

			for (double percent : queryPercent) {
				int noOfQueries = (int)(percent * (double)cacheSizeInSystem);

				List<String> membershipQueries = generateQueries(noOfQueries);

				bf.calculateFalsePositives(membershipQueries);
				iabf.calculateFalsePositives(membershipQueries);

				fWriter.write(Helper.COMMA + cacheSizeInSystem + Helper.SLASH + noOfQueries * noOfClients);
				fWriter.write(Helper.COMMA + bf.getFalsePositives() + Helper.COMMA + iabf.getFalsePositives());

				bf.setFalsePositives(0);
				iabf.setFalsePositives(0);
			}
			fWriter.write(Helper.NEW_LINE);
			cacheSize += cacheSizeIncrementer;
		}
	}

	private void compareWRTBloomFilterSize(BloomFilter bf, BloomFilter iabf, double[] percentBFSize, FileWriter fWriter) throws IOException {

		int cacheSize = this.minCacheSize;
		int noOfClients = this.maxNoOfClients;
		fWriter.write("Cache size");
		for(int i = 0; i < queryPercent.length; i++)
			fWriter.write(Helper.COMMA + "Total Cache Size" + Helper.SLASH + "Total BF Size" + Helper.COMMA + "Bloom Filter" + Helper.COMMA + "Importance Aware Bloom Filter");
		fWriter.write(Helper.NEW_LINE);

		while (cacheSizeIncrementer > 0 && cacheSize <= maxCacheSize) {
			int noOfQueries = (noOfClients + 1) * cacheSize;

			List<String> membershipQueries = generateQueries(noOfQueries);

			fWriter.write(new Integer(cacheSize).toString());

			for (double percent : percentBFSize) {
				int bloomFilterSize = (int)(percent * (double)cacheSize);

				bf.initialize(cacheSize, cacheSize, bloomFilterSize, data.toArray(new String[data.size()]));
				iabf.initialize(cacheSize, cacheSize, bloomFilterSize, data.toArray(new String[data.size()]));

				bf.calculateFalsePositives(membershipQueries);
				iabf.calculateFalsePositives(membershipQueries);

				fWriter.write(Helper.COMMA + noOfQueries + Helper.SLASH + (noOfClients + 1) * bloomFilterSize);
				fWriter.write(Helper.COMMA + bf.getFalsePositives() + Helper.COMMA + iabf.getFalsePositives());

				bf.setFalsePositives(0);
				iabf.setFalsePositives(0);
			}
			fWriter.write(Helper.NEW_LINE);
			cacheSize += cacheSizeIncrementer;
		}
	}

	private List<String> generateQueries(int noOfQueries) {

		List<String> queries = new ArrayList<String>();
		Random random = new Random();

		while (queries.size() != noOfQueries) {
			int index = random.nextInt(data.size());
			String queryData = data.get(index);
			queries.add(queryData);
		}
		return queries;
	}

	private void compareCoopCachingAlgo() {

		File fileOut = null;
		FileWriter fWriter = null;
		try {
			fileOut = new File(this.inputFile + Helper.UNDERSCORE +"caching_output.csv");
			fWriter = new FileWriter(fileOut, true);

			fWriter.write("Total Clients" + Helper.COMMA + "Greedy Forwarding" + Helper.COMMA + "N-Chance" + Helper.COMMA + "Robinhood" + Helper.COMMA + 
					"Total Clients" + Helper.COMMA + "Greedy Forwarding" + Helper.COMMA + "N-Chance" + Helper.COMMA + "Robinhood" + Helper.COMMA + 
					"Total Clients" + Helper.COMMA + "Greedy Forwarding" + Helper.COMMA + "N-Chance" + Helper.COMMA + "Robinhood" + Helper.COMMA + 
					"Total Clients" + Helper.COMMA + "Greedy Forwarding" + Helper.COMMA + "N-Chance" + Helper.COMMA + "Robinhood");

			System.out.println("Cooperative Caching Algorithm Comparison Starts");
			
			int noOfClients = minNoOfClients;
			while(clientIncrementer > 0 && noOfClients <= maxNoOfClients) {
				
				performCachingAlgoComparison(noOfClients, fWriter);
				noOfClients += clientIncrementer;
			}

			fWriter.write(Helper.NEW_LINE + Helper.NEW_LINE);
			System.out.println("Cooperative Caching Algorithm Comparison Ends");
		} 
		catch(IOException ex){
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		} 
		catch(Exception ex){
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		} 
		finally {
			try {
				if (fWriter != null)
					fWriter.close();
			} 
			catch(IOException ex){
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
			catch(Exception ex){
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

	private void performCachingAlgoComparison(int noOfClients, FileWriter writer) throws IOException {
		List<String> queries = generateQueries(minCacheSize, minCacheSize,	diskSize, noOfClients);

		String ticks = "";
		String local = "";
		String global = "";
		String hops = "";

		for(Algorithms algorithm : Algorithms.values()) {
			double ticksPerRequest = 0.0;
		 // double cacheMiss = 0.0;
			double localCacheHit = 0.0;
			double globalCacheHit = 0.0;
			int networkHops = 0;

			Algorithm algo = AlgorithmProvider.createAlgorithm(algorithm, noOfClients, minCacheSize,
					minCacheSize, diskSize, localCacheAccessTime, diskAccessTime, remoteCacheAccessTime);

			if (algo != null) {
				algo.initializeAlgo(clientCaches, serverCache, serverDisk);
				algo.execute(queries);

				ticksPerRequest += algo.getAccessTimePerRequest();
				localCacheHit += algo.getLocalCacheHit();
				globalCacheHit += algo.getGlobalCacheHit();
				networkHops += algo.getNetworkHops();
			 // cacheMiss += ca.getCacheMiss();
			}
			ticks += Helper.COMMA + String.format(Helper.FORMAT, ticksPerRequest);
			local += Helper.COMMA + String.format(Helper.FORMAT, localCacheHit);
			global += Helper.COMMA + String.format(Helper.FORMAT, globalCacheHit) + Helper.MOD;
			hops += Helper.COMMA + String.valueOf(networkHops);
		}
		writer.write(Helper.NEW_LINE + noOfClients + ticks + Helper.COMMA + noOfClients + local + 
				Helper.COMMA + noOfClients + global + Helper.COMMA + noOfClients + hops);
	}

	private List<String> generateQueries(int clientCacheSize, int serverCacheSize, int diskSize, int noOfClients) {
		
		this.clientCaches = new String[noOfClients][clientCacheSize];
		this.serverCache = new String[serverCacheSize];
		this.serverDisk = new String[diskSize];
		
		int index = 0;
		List<DataBlock> dataList = new ArrayList<DataBlock>();
		List<String> requests = new ArrayList<String>();
		
		Random rand = new Random();

		while (dataList.size() != diskSize) {
			DataBlock dataBlock = new DataBlock(data.get(rand.nextInt(data.size())));
			
			if (!dataList.contains(dataBlock)) {
				dataList.add(dataBlock);
				requests.add(dataBlock.getData());
				serverDisk[index++] = dataBlock.getData();
			}
		}

		for (int i = 0; i < noOfClients; i++) {
			index = 0;
			dataList.clear();
			
			while (dataList.size() != clientCacheSize) {
				DataBlock dataBlock = new DataBlock(serverDisk[rand.nextInt(diskSize)]);
				
				if (!dataList.contains(dataBlock)) {
					dataList.add(dataBlock);
					requests.add(dataBlock.getData());
					clientCaches[i][index++] = dataBlock.getData();
				}
			}
		}

		index = 0;
		dataList.clear();
		while (dataList.size() != serverCacheSize) {
			DataBlock dataBlock = new DataBlock(serverDisk[rand.nextInt(diskSize)]);
			
			if (!dataList.contains(dataBlock)) {
				dataList.add(dataBlock);
				requests.add(dataBlock.getData());
				serverCache[index++] = dataBlock.getData();
			}
		}
		totalRequests = requests.size();
		return requests;
	}

	public static void main(String[] args){

		if(args.length == 1) {
			Simulator simulator = new Simulator();

			if(!simulator.readFromConfigFile(args[0])){
				System.err.println("Error Reading Config File!");
			}
			else if(!simulator.readDataFromInputFile()){
				System.err.println("Error Reading Input Data File!");
			}
			else{
				simulator.initiateGoal();
			}
		}
		else{
			System.err.println("Please enter one command line argument =>: \"java Simulator config_filename\"");
		}
	}
}
