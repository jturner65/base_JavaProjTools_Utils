package base_Utils_Objects.priorityQueue;

import base_Utils_Objects.priorityQueue.base.myPriorityQueue;

public class myMaxQueue<T extends Comparable<T>> extends myPriorityQueue<T>{
	public myMaxQueue() {super();}//empty max priority queue of default size
	public myMaxQueue(int _initSize) {super(_initSize);	}
	public myMaxQueue( T[] _keys) {	super(_keys);}	
	@Override
	protected boolean compare(T[] ara, int i, int j) {	return ara[i].compareTo(ara[j]) < 0;}	
	
}//myMaxQueue
