package chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.print.DocFlavor.READER;

import tuplespaces.TupleSpace;

//STATUS: [“STATUS”, String:[channel1:rows, channel2:rows, …, channeln:rows,]]
//CHANNEL: [String:channel_name, “CHANNEL”, int:listeners, int:position, String:message]
//WRITE: [string channel_name, "WRITE", int nextwrite]
//INFO: [string channel_name, "INFO", int numbers, int firstread,int lastread]


public class ChatServer {
	// Add stuff here.
	
	public static final String CHANNEL = "CHANNEL";
	public static final String WRITE = "WRITE";
	public static final String INFO = "INFO";
	public static final String STATUS = "STATUS"; 

	
	private final TupleSpace tSpace;
	
	private HashMap<String, String> channels = new HashMap<String, String>(); //<name,size>
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		// TODO: Implement ChatServer(TupleSpace, int, String[]);
		tSpace = t;
		
		for(String channel:channelNames)
		{
			if (addChannel(channel, rows)) 
			{
				// initialize channel tuples
				tSpace.put(channel,WRITE,"0");
				tSpace.put(channel,INFO,"0","0","-1");
			}
		}
		
		t.put(STATUS,channelMapToString());
	}

	public ChatServer(TupleSpace t) {
		// TODO: Implement ChatServer(TupleSpace);
		tSpace = t;
		
		String[] tupleStrings = t.read(STATUS,null);
		channelMapFromString(tupleStrings[1]);
	}

	public String[] getChannels() {
		// TODO: Implement ChatServer.getChannels();
		String[] tupleStrings = tSpace.read(STATUS,null);
		
		//merge with current channels in hashmap
		mergeChannels(tupleStrings[1]);
		
//		tSpace.put(STATUS,channelMapToString());
		
		return getChannelFromChanelMap();
	}

	public void writeMessage(String channel, String message) {
		// TODO: Implement ChatServer.writeMessage(String, String);
		
		// get the next write position and disable all other servers to write
		String[] tuple = tSpace.get(channel,WRITE,null);
		int write = Integer.parseInt(tuple[2]);
		
		//check if write is the last element in buffer
		// wait until the oldest element is read by all listeners
		int rows = Integer.parseInt( channels.get(channel));
		
		int oldest = 0;
		if (write >= rows)  // need to remove the oldest one
		{
			oldest = write - rows;
			String old = Integer.toString(oldest);
			// wait until the oldest message read by all listeners and remove it
			tSpace.get(channel,CHANNEL,"0",old,null);  
			oldest++;
		}
		
		tuple = tSpace.get(channel,INFO,null,null,null); // get number of connections of this channel
		// put number of connections, i.e. listeners left unread, to this position
		tSpace.put(channel,CHANNEL,tuple[2],Integer.toString(write),message); 
				
		// update next write position
		System.out.println(String.format("PUT: %s, %s, %s", channel,WRITE,Integer.toString(write+1)));
		tSpace.put(channel,INFO,tuple[2],Integer.toString(oldest),Integer.toString(write));
		tSpace.put(channel,WRITE,Integer.toString(write+1));
	}

	public ChatListener openConnection(String channel) {
		// TODO: Implement ChatServer.openConnection(String);
		
		// get the next write position and disable all other servers to write
		String[] tuple = tSpace.get(channel,WRITE,null);
		int write = Integer.parseInt(tuple[2]);
		
		//find the first read position of channel buffer		
		String[] connStrings = tSpace.read(channel, INFO, null, null, null);
		int readMin = Integer.parseInt(connStrings[3]);
		int readMax = Integer.parseInt(connStrings[4]);
				
		// add unread listener to every element in the channel buffer
		for (int i = readMin; i < readMax+1; i++) {
			tuple = tSpace.get(channel, CHANNEL, null, Integer.toString(i),null);
			int listeners = Integer.parseInt(tuple[2]);
			tSpace.put(channel, CHANNEL, Integer.toString(listeners + 1),Integer.toString(i),tuple[4]);
		}
		
		
		// update number of connections
		tuple = tSpace.get(channel, INFO, null,null,null);
		int connections = Integer.parseInt(tuple[2]);
		tSpace.put(channel, INFO, Integer.toString(connections + 1),tuple[3],tuple[4]);
		
		// enable other servers
		tSpace.put(channel,WRITE,Integer.toString(write));
		
		return new ChatListener(channel, tSpace);
	}
	
	public String channelMapToString(){
		String s = "";
		for (String channel : channels.keySet()) {
			s += channel + ":" + channels.get(channel) + ",";
		}
		return s;
	}
	
	public void channelMapFromString(String s){
		channels.clear();
		if (s.contains(",")) 
		{
			String[] items = s.split(",");
			for (String item : items) 
			{
				String[] parts = item.split(":");
				channels.put(parts[0], parts[1]);
			}
		}	
	}
	
	public boolean addChannel(String ch, int rows) 
	{
		if (channels.containsKey(ch)) 
		{
			//channel already there
			return false;
		} else {
			//add new channels to tuple space
			channels.put(ch, Integer.toString(rows));
			return true;
		}
	}
	
	public String[] getChannelFromChanelMap() {
		Set<String> set = channels.keySet();
		if (set.isEmpty()) return null;
				
		String[] s = new String[set.size()];
		int i = 0;
		for (String ch : channels.keySet()) 
		{
			s[i] = ch;
			i++;
		}
		return s;
	}
	
	public void mergeChannels(String channelString){
		if (channelString.contains(",")) 
		{
			String[] items = channelString.split(",");
			for (String item : items) 
			{
				String[] parts = item.split(":");
				String channel = parts[0];
				String rows = parts[1];
				
				if (!channels.containsKey(channel)) {
					channels.put(channel, rows);
				} 
			}
		}	
		
		
	}
	
}
