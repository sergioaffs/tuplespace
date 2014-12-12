package chat;

import java.util.HashMap;
import java.util.Set;

import tuplespaces.TupleSpace;

//STATE: [“STATE”, int:rows, String:[channel1:number, channel2:number, …, channeln:number,]>]
//CHANNEL: [String:channel_name, “CHANNEL”, int:listeners, int:position]
//WRITE: [string channel_name, "WRITE", int lastwirte]
//CONN: [string channel_name, "CONN", int numbers],


public class ChatServer {
	// Add stuff here.
	
	private final String CHANNEL = "CHANNEL";
	private final String WRITE = "WRITE";
	private final String CONN = "CONN";
	private final String STATUS = "STATUS"; 
	
	private final TupleSpace tSpace;
	
	private HashMap<String, String> channels = new HashMap<String, String>(); //<name,size>
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		// TODO: Implement ChatServer(TupleSpace, int, String[]);
		tSpace = t;
		
		////  maybe not necessary
		String[] tupleStrings = t.get(STATUS,null,null);
		channelMapFromString(tupleStrings[2]);
		////
		
		for(String channel:channelNames)
		{
			if (addChannel(channel, rows)) 
			{
				// initialize channel tuples
				tSpace.put(channel,WRITE,"0");
				tSpace.put(channel,CONN,"0");
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
		String[] tupleStrings = tSpace.get(STATUS,null,null);
		return null;
	}

	public void writeMessage(String channel, String message) {
		// TODO: Implement ChatServer.writeMessage(String, String);
		String[] write_position = tSpace.get(channel,WRITE,null);
		int write = Integer.parseInt(write_position[2]);
		
		//check if write is the last element in buffer
		// wait until the oldest element is read by all listeners
		int rows = Integer.parseInt( channels.get(channel));
		tSpace.read(channel,CHANNEL,"0",Integer.toString(write-rows));
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
}
