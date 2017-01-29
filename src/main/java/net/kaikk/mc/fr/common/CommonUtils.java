package net.kaikk.mc.fr.common;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class CommonUtils {
	public static UUID toUUID(byte[] bytes) {
		if (bytes.length != 16) {
			throw new IllegalArgumentException();
		}
		int i = 0;
		long msl = 0;
		for (; i < 8; i++) {
			msl = (msl << 8) | (bytes[i] & 0xFF);
		}
		long lsl = 0;
		for (; i < 16; i++) {
			lsl = (lsl << 8) | (bytes[i] & 0xFF);
		}
		return new UUID(msl, lsl);
	}
	
	public static byte[] UUIDtoByteArray(UUID uuid) {
		return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
	}

	public static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		StringBuilder sb = new StringBuilder();

		for(;i<arrayString.length;i++){
			sb.append(arrayString[i]);
			sb.append(' ');
		}

		if (sb.length()!=0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	public static short getUtcYear() {
		return (short) Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR);
	}
	
	public static int epoch() {
		return (int) (System.currentTimeMillis()/1000);
	}
	

	public static int ipv4ToInt(String address) {
		String[] p = address.split("[.]");
	    return (Integer.parseInt(p[0])<<24) | (Integer.parseInt(p[1])<<16) | (Integer.parseInt(p[2])<<8) | (Integer.parseInt(p[3]));
	}

	public static String shortStackTrace() {
		StringBuilder sb = new StringBuilder();
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		for(int i = 2; i<5 && i<ste.length; i++) {
			String className;
			try {
				className = Class.forName(ste[i].getClassName()).getSimpleName();
			} catch (ClassNotFoundException e) {
				className = ste[i].getClassName();
			}
			
			sb.append(className + "." + ste[i].getMethodName() +
		            (ste[i].isNativeMethod() ? "(Native Method)" :
		                (ste[i].getFileName() != null && ste[i].getLineNumber() >= 0 ?
		                 "(" + ste[i].getFileName() + ":" + ste[i].getLineNumber() + ")" :
		                 (ste[i].getFileName() != null ?  "("+ste[i].getFileName()+")" : "(Unknown Source)"))));
			sb.append(" <- ");
		}
		return sb.toString();
	}

	public static String timeToString(int time) {
		List<String> strs = new ArrayList<String>();

		if (time<0) {
			time*=-1;
		}

		// seconds
		int secs = time % 60;
		if (secs!=0||time==0) {
			strs.add(secs+" second"+(secs!=1?"s":""));
		}
		if (time<60) {
			return mergeTimeStrings(strs);
		}

		// minutes
		int tmins = (time-secs) / 60;
		int mins = tmins % 60;
		if (mins!=0) {
			strs.add(mins+" minute"+(mins!=1?"s":""));
		}
		if (tmins<60) {
			return mergeTimeStrings(strs);
		}

		// hours
		int thours = (tmins-mins) / 60;
		int hours = thours % 24;
		if (hours!=0) {
			strs.add(hours+" hour"+(hours!=1?"s":""));
		}
		if (thours<24) {
			return mergeTimeStrings(strs);
		}

		// days
		int tdays = (thours-hours) / 24;
		if (tdays!=0) {
			strs.add(tdays+" day"+(tdays!=1?"s":""));
		}

		return mergeTimeStrings(strs);
	}

	private static String mergeTimeStrings(List<String> strs) {
		StringBuilder sb = new StringBuilder();
		for (int i = strs.size()-1; i >=0; i--) {
			sb.append(strs.get(i));
			sb.append(' ');
		}
		
		int lastChar = sb.length()-1;
		if (lastChar>=0) {
			sb.deleteCharAt(lastChar);
		}
		
		return sb.toString();
	}
}