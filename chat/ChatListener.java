package chat;

import com.sun.corba.se.spi.orb.StringPair;

import tupleserver.TupleServer;
import tuplespaces.TupleSpace;

public class ChatListener {
	// Add stuff here.
	
	private String channel;
	private TupleSpace tupleSpace;
	private int messageCount = 0;
	
	public ChatListener(String channel, TupleSpace tupleSpace, int nextread){
		this.channel = channel;
		this.tupleSpace = tupleSpace;
		this.messageCount = nextread;
	}
	
	public String getNextMessage() 
	{
	
		String[] message = tupleSpace.get(channel, ChatServer.CHANNEL, null, Integer.toString(messageCount), null);
		int currentPosition = Integer.parseInt(message[3]);
		
		int remainingReaders = Integer.parseInt(message[2]);
		
		System.out.println("Current position: "+currentPosition);

		System.out.println("Tuple back again");
		tupleSpace.put(channel, ChatServer.CHANNEL, Integer.toString(remainingReaders-1), message[3], message[4]);
		
		messageCount++;
		return message[4];
	}

	public void closeConnection() {
		// TODO: Implement ChatListener.closeConnection();
		
		String[] connections = tupleSpace.get(channel, ChatServer.CONN, null,null,null);
		while (messageCount <= Integer.parseInt(connections[4]))  //must consume all remaining message
		{
			String[] message = tupleSpace.get(channel, ChatServer.CHANNEL, null, Integer.toString(messageCount), null);
			tupleSpace.put(channel, ChatServer.CHANNEL, Integer.toString(Integer.parseInt(message[2])-1), message[3], message[4]);
			messageCount++;
		}
		int connectionCount = Integer.parseInt(connections[2]);
		tupleSpace.put(channel, ChatServer.CONN, Integer.toString(connectionCount-1),connections[3],connections[4]);
	}
}
