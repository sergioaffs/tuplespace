package tuplespaces;

import java.util.HashMap;
import java.util.LinkedList;

public class LocalTupleSpace implements TupleSpace {
	// Add stuff here.
	 
	private HashMap<Integer, LinkedList<String[]>> tupleMap;
	
	public LocalTupleSpace() {
		// TODO Auto-generated constructor stub
		this.tupleMap = new HashMap<Integer, LinkedList<String[]>>();
	} 
	
	private Integer findPosition(LinkedList<String[]>list, String... pattern){
		int position = 0;
		for (String[] tuple : list) {
			//iterate a String[] tuple to see it matches pattern
			boolean match = true;
			int index = 0;
			
			while (match && index < tuple.length) {
				
				if (pattern[index] == null) {
					// DO NOTHING, automatically match
				}
				else {
					if (pattern[index].hashCode() != tuple[index].hashCode()) {
						match = false;
					}
				}
				index++;
			}
			
			if (match) {
				return position;
			}
			else {
				position ++;
			}
		}
		return -1;
	}
	
	public String[] get(String... pattern) {
		// TODO: Implement LocalTupleSpace.get(String...).
		if (!tupleMap.containsKey(pattern.length)) {
			tupleMap.put(pattern.length, new LinkedList<String[]>()); // put in an empty list
		}
		LinkedList<String[]> list = tupleMap.get(pattern.length);
		
		int position;
		synchronized (list) {
			while((position = findPosition(list,pattern)) == -1){
				try {
					list.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return list.remove(position);
		}
	}

	public String[] read(String... pattern) {
		// TODO: Implement LocalTupleSpace.read(String...).
		if (!tupleMap.containsKey(pattern.length)) {
			tupleMap.put(pattern.length, new LinkedList<String[]>()); // put in an empty list
		}
		LinkedList<String[]> list = tupleMap.get(pattern.length);
		
		int position;
		synchronized (list) {
			while((position = findPosition(list,pattern)) == -1){
				try {
					list.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return list.get(position).clone();
		}
	}

	public void put(String... tuple) {
		// TODO: Implement LocalTupleSpace.put(String...).
		if(!tupleMap.containsKey(tuple.length)){
			tupleMap.put(tuple.length, new LinkedList<String[]>());
		}
		
		LinkedList<String[]> list= tupleMap.get(tuple.length);
		synchronized (list) {
			// if it's not tuple.clone(), "Tuple returned by read affected by change made after it was put",
			list.add(tuple.clone()); 
			list.notifyAll();
		}
	}
	
}

