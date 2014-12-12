package chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.print.DocFlavor.READER;

import tuplespaces.TupleSpace;

//STATE: [“STATE”, String:[channel1:number, channel2:number, …, channeln:number,]>]
//CHANNEL: [String:channel_name, “CHANNEL”, int:listeners, int:position]
//WRITE: [string channel_name, "WRITE", int nextwrite]
//CONN: [string channel_name, "CONN", int numbers]
//MESSAGE: [String:channel_name, “MESSAGE”, int:position, String:message]
//READ: [String:channel_name, “READ”,int nextread]


public class ChatServer {
	// Add stuff here.
	
	private final String CHANNEL = "CHANNEL";
	private final String WRITE = "WRITE";
	private final String CONN = "CONN";
	private final String STATUS = "STATUS"; 
	private final String MESSAGE = "MESSAGE";
	private final String READ = "READ";
	
	private final TupleSpace tSpace;
	
	private HashMap<String, String> channels = new HashMap<String, String>(); //<name,size>
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		// TODO: Implement ChatServer(TupleSpace, int, String[]);
		tSpace = t;
		
		////  maybe not necessary
		String[] tupleStrings = t.get(STATUS,null);
		channelMapFromString(tupleStrings[2]);
		////
		
		for(String channel:channelNames)
		{
			if (addChannel(channel, rows)) 
			{
				// initialize channel tuples
				tSpace.put(channel,WRITE,"0");
				tSpace.put(channel,CONN,"0");
				tSpace.put(channel,READ,"-1");
			}
		}
		
		t.put(STATUS,channelMapToString());
	}

	public ChatServer(TupleSpace t) {
		// TODO: Implement ChatServer(TupleSpace);
		tSpace = t;
	}

	public String[] getChannels() {
		// TODO: Implement ChatServer.getChannels();
		String[] tupleStrings = tSpace.get(STATUS,null);
		
		//merge with current channels in hashmap
		mergeChannels(tupleStrings[2]);
		
		tSpace.put(STATUS,channelMapToString());
		
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
		
		if (write >= rows)  // need to remove the oldest one
		{
			String old = Integer.toString(write-rows);
			tSpace.get(channel,CHANNEL,"0",old); // wait until the oldest read by all listeners
			tSpace.get(channel,MESSAGE,old,null); // remove the oldest	
		}
		
		tSpace.put(channel,MESSAGE,Integer.toString(write),message);
		
		tuple = tSpace.read(channel,CONN,null); // get number of connections of this channel
		// put number of connections, i.e. listeners left unread, to this position
		tSpace.put(channel,CHANNEL,tuple[2],Integer.toString(write)); 
		
		// update latest read position
		tSpace.get(channel,READ,null);
		tSpace.put(channel,READ,Integer.toString(write));
		
		tSpace.put(channel,WRITE,Integer.toString(write+1));
	}

	public ChatListener openConnection(String channel) {
		// TODO: Implement ChatServer.openConnection(String);
		String[] write_position = tSpace.get(channel,WRITE,null);
		
		return null;
	}
	
	public String channelMapToString(){
		String s = "";
		Set<String> chs = channels.keySet();
		for (String ch : chs) {
			s += ch + ":" + channels.get(ch) + ",";
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
				String ch = parts[0];
				String number = parts[1];
				
				if (!channels.containsKey(ch)) {
					channels.put(ch, number);
				} 
			}
		}	
		
		
	}
	
}
