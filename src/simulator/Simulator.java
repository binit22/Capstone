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

import bloomfiltercompare.BloomFilter;
import bloomfiltercompare.BloomFilterImplementation;

public class Simulator {

	private int noOfClients;
	private int minCacheSize;
	private int maxCacheSize;
	private int cacheSizeIncrementer;
	private int goalNo;
	private String inputFile;
	private double[] queryPercent;
	private double[] percentBFSize;
	
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
			this.noOfClients = Integer.parseInt(prop.getProperty("numberOfClients"));
			this.minCacheSize = Integer.parseInt(prop.getProperty("minimumCacheSize"));
			this.maxCacheSize = Integer.parseInt(prop.getProperty("maximumCacheSize"));
			this.cacheSizeIncrementer = Integer.parseInt(prop.getProperty("cacheSizeIncrementer"));
			this.inputFile = prop.getProperty("inputFileName");
			
			String[] queryPer = prop.getProperty("queryPercentWRTCacheSize").split(Helper.COMMA);
			queryPercent = new double[queryPer.length];
			for(int i = 0; i < queryPer.length; i++)
				queryPercent[i] = Double.parseDouble(queryPer[i]);
			
			String[] perBFSize = prop.getProperty("bfSizePercentWRTCacheSize").split(Helper.COMMA);
			percentBFSize = new double[perBFSize.length];
			for(int i = 0; i < perBFSize.length; i++)
				percentBFSize[i] = Double.parseDouble(perBFSize[i]);

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
			// perform comparison of cooperative caching algorithms
		}
		else{
			System.err.println("Goal can be 1 or 2 only!");
		}
	}

	private void compareBloomFilters() {

		BloomFilter bf = new BloomFilterImplementation(1, noOfClients);
		BloomFilter iabf = new BloomFilterImplementation(2, noOfClients);
		
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
		int noOfClients = this.noOfClients;
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
		int noOfClients = this.noOfClients;
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
