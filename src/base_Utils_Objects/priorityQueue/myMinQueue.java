package base_Utils_Objects.priorityQueue;

import base_Utils_Objects.priorityQueue.base.myPriorityQueue;

public class myMinQueue<T extends Comparable<T>> extends myPriorityQueue<T>{	
	public myMinQueue() {super();}//empty min priority queue of default size
	public myMinQueue(int _initSize) {super(_initSize);	}
	public myMinQueue(T[] _keys) {	super(_keys);}	
	@Override
    protected boolean compare(T[] ara, int i, int j) {	return ara[i].compareTo(ara[j]) > 0;}	    
	
}//myMinQueue

