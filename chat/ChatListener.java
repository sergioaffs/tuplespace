package chat;

import com.sun.corba.se.spi.orb.StringPair;

import tupleserver.TupleServer;
import tuplespaces.TupleSpace;

public class ChatListener {
	// Add stuff here.
	
	private String channel;
	private TupleSpace tupleSpace;
	private int messageCount = 0;
	
	public ChatListener(String channel, TupleSpace tupleSpace) {
		this.channel = channel;
		this.tupleSpace = tupleSpace;
		
		String[] readTuple = tupleSpace.read(channel, ChatServer.READ, null, null);
		messageCount = Integer.parseInt(readTuple[2]);
		if(messageCount==-1)messageCount=0;
		System.out.println("Listener started on message "+messageCount);
	}

	public String getNextMessage() 
	{
		String[] readTuple = tupleSpace.read(channel, ChatServer.READ, null, null);
		int minPosition = Integer.parseInt(readTuple[2]);
		int maxPosition = Integer.parseInt(readTuple[3]);
//		
//		for (int i = minPosition; i < maxPosition; i++) 
//		{
//			
//		}
		
		
		System.out.println("Trying to get ".format("Trying to get %s, %s, %d", channel, ChatServer.CHANNEL, messageCount));
		String[] message = tupleSpace.get(channel, ChatServer.CHANNEL, null, Integer.toString(messageCount), null);
//		System.out.println(String.format("Got %s: %s", message[3], message[4]));
		int currentPosition = Integer.parseInt(message[3]);
		
		int remainingReaders = Integer.parseInt(message[2]);
		System.out.println("Min position: "+minPosition);
		System.out.println("Max position: "+maxPosition);
		System.out.println("Current position: "+currentPosition);
//		if(remainingReaders > 1 || (minPosition <= currentPosition && maxPosition >=currentPosition))
//		{
			System.out.println("Tuple back again");
			tupleSpace.put(channel, ChatServer.CHANNEL, Integer.toString(remainingReaders-1), message[3], message[4]);
//		}
//		tupleSpace.put(channel, ChatServer.READ, readTuple[2], readTuple[3]);
		messageCount++;
		return message[4];
		//throw new UnsupportedOperationException();
		// TODO: Implement ChatListener.getNextMessage();
	}

	public void closeConnection() {
		// TODO: Implement ChatListener.closeConnection();
		String[] tupleStrings = tupleSpace.read(channel,ChatServer.READ,null,null);
		while (messageCount <= Integer.parseInt(tupleStrings[3]))  //must consume all remaining message
		{
			getNextMessage();
		}
		
		String[] connections = tupleSpace.get(channel, ChatServer.CONN, null);
		int connectionCount = Integer.parseInt(connections[2]);
		tupleSpace.put(channel, ChatServer.CONN, Integer.toString(connectionCount-1));
	}
}
