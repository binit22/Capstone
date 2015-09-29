package simulator;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashProvider {

	private String dataBlock;
	private int filterSize;
	private MessageDigest md;
	
	public HashProvider(String dataBlock, int filterSize) {
		this.dataBlock = dataBlock;
		this.filterSize = filterSize;
	}
	
	private int hashMD5() {
		BigInteger result = null;
		try {
			md = MessageDigest.getInstance("MD5"); 
			md.update(dataBlock.getBytes(), 0, dataBlock.length());
			result = new BigInteger(1, md.digest());
			result = result.mod(new BigInteger(new Integer(filterSize).toString()));
		}
		catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return result.intValue();
	}
	
	private int hashSHA1() {
		BigInteger result = null;
		try {
			md = MessageDigest.getInstance("SHA1"); 
			md.update(dataBlock.getBytes(), 0, dataBlock.length());
			result = new BigInteger(1, md.digest());
			result = result.mod(new BigInteger(new Integer(filterSize).toString()));
		} 
		catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return result.intValue();
	}
	
	private int hashSHA256() {
		BigInteger result = null;
		try {
			md = MessageDigest.getInstance("SHA-256"); 
			md.update(dataBlock.getBytes(), 0, dataBlock.length());
			result = new BigInteger(1, md.digest());
			result = result.mod(new BigInteger(new Integer(filterSize).toString()));
		}
		catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return result.intValue();
	}
	
	private int hashSHA384() {
		BigInteger result = null;
		try {
			md = MessageDigest.getInstance("SHA-384"); 
			md.update(dataBlock.getBytes(), 0, dataBlock.length());
			result = new BigInteger(1, md.digest());
			result = result.mod(new BigInteger(new Integer(filterSize).toString()));
		}
		catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return result.intValue();
	}
	
	public int[] getBitPositions() {
		int[] positions = new int[4];
		
		positions[0] = hashMD5();
		positions[1] = hashSHA1();
		positions[2] = hashSHA256();
		positions[3] = hashSHA384();

		return positions;
	}
}
