package base_Utils_Objects.priorityQueue.base;

/**
 * Priority Queue class implementation 
 * @author john
 *
 * @param <T> comparable object this queue will hold.  comparator determines priority
 * 
 * TODO : Needs to throw errors instead of error messages
 */
public abstract class myPriorityQueue<T extends Comparable<T>>{
    /**
     * initially have room for initSize items; 
     */
    protected int _initSize = 10;
    /**
     * start index of heap array - using 1 so that heap idxes line up to 2x / 2x + 1
     */
    protected final int _stIDX = 1;
    /**
     * size of heap array holding elements in queue;
     */
    protected int _size;
    /**
     * # of elements in heap/queue
     */
    private int _numElems;
    /**
     * array data structure holding data - using heap structure - ignore idx 0, using idxs 1->_size-1
     */
    protected T[] _heap;
    /**
     * empty ctor
     */
    public myPriorityQueue() {
        initHeap(_initSize);    
    }//empty priority queue
    
    /**
     * ctor for initial size to be different than base initSize
     * @param _initSize
     */
    public myPriorityQueue(int initSize) {
        initHeap(initSize);
    }
    /**
     * ctor for initial set of keys
     * @param _keys
     */
    public myPriorityQueue(T[] _keys) {
        //make 2x as large as initial key set
        initHeap(_keys.length * 2);
        System.arraycopy(_keys, _stIDX, _heap, _stIDX, _keys.length);
        _numElems = _keys.length;        
    }
    
    /**
     * call to reinitialize/clear heap and set initial size to set value for _initSize
     */    
    public final void initHeap() {
        initHeap(_initSize);
    }
    /**
     * call to reinitialize/clear heap and set initial size to passed value
     * @param desired initial size
     */
    public final void initHeap(int initSize) {
        _numElems = 0;
        _initSize = initSize;
        resizeHeap(_initSize);    
    }//
    
    /**
     * remake heap array for new size - only do when current size is too small - need to then heapify   
     * cannot shrink heap smaller than current _numElems.._newSize needs to be bigger than _numElems    
     * ignores elems idx < _stIDX         
     *                                                               
     * @param _newSize
     */
    private void resizeHeap(int _newSize) {
        if(_newSize < _numElems) {return;}//no change if trying to shrink smaller than # of elements in queue
        T[] tmpHeap = buildCompAra(_newSize+_stIDX);//ignore idx 0
        //if heap already exists, copy current heap into tmpHeap
        if((null != _heap) && (_numElems > 0) && (_numElems < _heap.length)){//copy from heap into tmpHeap    - heap should already be in appropriate order, so no need to reheapify
            System.arraycopy(_heap, _stIDX, tmpHeap, _stIDX, _numElems);    
        }
        _heap = tmpHeap;
        _size = _newSize;        
    }//resizeHeap
    /**
     * insert elem into priority queue
     * @param elem
     */
    public final void insert(T elem) {
        if(isFull()) {resizeHeap(_size*2);}//increase size of heap before adding if heap is full-double size of array if necessary
        _heap[++_numElems] = elem;
        up(_heap,_numElems);        //heapify toward leaves
        if (!isHeap(_heap)) {System.out.println("HEAP ERROR : Heapness not preserved after attempting to add element : "+ elem);}
    }//insert

    /**
     * peek at 1st element from list
     * @return
     */
    public final T peekFirst() {
        if(isEmpty()) {return null;}
        return _heap[_stIDX];
    }
    public final T peekElem(int idx) {
        if(isEmpty() || idx > _numElems) {return null;}
        return _heap[idx];
    }

    /**
     * remove and return idx'th element from heap
     * @param idx
     * @return
     */
    protected final T removeElem(int idx) {
        if(isEmpty()) {return null;}
        T elem = _heap[idx];
        swap(_heap, idx, _numElems);        //move element to remove from heap to end of heap ara
        _heap[_numElems] = null;         //remove old elem    
        --_numElems;                    //shrink # of elements
        down(_heap, idx, _numElems);                    
 
        //shrink heap to half size, if possible
        if ((_numElems > 0) && (_numElems <= _size / 3)) {resizeHeap(_size / 2);}
        if (!isHeap(_heap)) {System.out.println("HEAP ERROR : Heapness not preserved after attempting to remove element  " + elem + " at idx : "+ idx);}
        return elem;        
    }//removeMin
    
    /**
     * remove and return first element - _stIDX is location of element
     * @return
     */
    public final T removeFirst() {return removeElem(_stIDX);}    
    /**
     * remove and return passed element - not necessarily the first element
     * @param elem
     * @return
     */
    public final T removeElem(T elem) {
        if(isEmpty()) {return null;}
        int idx = findElem(elem);
        if(-1==idx) {return null;}                //not found
        return removeElem(idx); 
    }
    
    /**
     * find index of element elem
     * @param elem
     * @return
     */
    private int findElem(T elem) {
        for(int i=_stIDX; i<=_numElems;++i) {            if(_heap[i].compareTo(elem) == 0) {return i;}        }
        return -1;
    }
    
    //////////////////////////
    //tests for heapness
    /**
     * test for heapness
     * @return
     */
    public final boolean isHeap() {return isHeap(_heap,_stIDX);}
    protected final boolean isHeap(T[] ara) {return isHeap(ara,_stIDX);}
    protected final boolean isHeap(T[] ara, int idx) {
        if (idx > _numElems) return true;
        int left = 2*idx;
        if (left  <= _numElems && compare(ara,idx, left))  return false;
        int right = left + 1;
        if (right <= _numElems && compare(ara,idx, right)) return false;
        return isHeap(ara,left) && isHeap(ara,right);
    }//verify this is an appropriate heap (either min or max)
    
    /**
     * move elements based on comparison - heapify up tree
     * @param ara
     * @param idx
     */
    protected final void up(T[] ara, int idx) {
        while (idx > 1 && compare(ara,idx/2, idx)) {        swap(ara, idx, idx/2);    idx = idx/2; } 
    }//up
   
    /**
     * move elements based on comparison -  heapify down tree ara that has n elements, starting at idx 
     * @param ara
     * @param idx
     * @param n
     */
    protected final void down(T[] ara,int idx, int n) {
        int j;
        while (2*idx <= n) {
            j = 2*idx;
            if (j < n && compare(ara,j, j+1)){++j;}
            if (!compare(ara,idx, j)) {break;}
            swap(ara, idx, j);
            idx = j;
        }
    }//down
    
    /**
     * build array of comparables of passed size
     * @param size
     * @return
     */
    private T[] buildCompAra(int size) {
        @SuppressWarnings("unchecked")
        T[] res = (T[]) new Comparable[size];   
        return res;
    }
    
    /**
     * return all members of heap in sorted order
     * @return
     */
    public final T[] getSortedElems() {
        T[] res = buildCompAra(_numElems+_stIDX);
        System.arraycopy(_heap, _stIDX, res, _stIDX, _numElems);
        int n = _numElems;
        for (int i=n/2; i>0; --i) {    down(res, i, n);} 
        while (n>1) {               swap(res, 1, n--);        down(res, 1, n);       }
        
        T[] res2 = buildCompAra(_numElems);
        System.arraycopy(res, _stIDX, res2, 0, _numElems);        
        if (!isHeap(_heap)) {System.out.println("HEAP ERROR : Heapness not preserved after attempting to build sorted ara of elements");}       
        return res2;        
    }//getSortedElems
    
    /**
     * either min or max heap
     * @param ara other
     * @param i
     * @param j
     * @return
     */
    protected abstract boolean compare(T[] ara, int i, int j);
    protected final void swap(T[] ara, int i, int j) { T s = ara[i];ara[i] = ara[j];ara[j] = s;}
    
    public final boolean isEmpty() {return (_numElems == 0);}
    public final boolean isFull() {return (_numElems == _size);}
    public final int size() {return _numElems;}    
    /**
     * For debugging/testing only - get a copy of the heap in current heap order
     * @return
     */
    public final T[] getHeapCopy() {
        T[] res = buildCompAra(_heap.length);
        System.arraycopy(_heap, 0, res, 0, _heap.length);        
        return res;
    }

    /**
     * @return the _numElems
     */
    public final int getNumElems() {        return _numElems;    }

    ///////////////
    /////DEBUG AND TESTING
    
    /**
     * Validate priority queue heap state and integrity
     * @name PQ's name, for test output
     * @return
     */
    public final String TEST_verifyHeapState(String name) {
        //first determine FEL's current state
        String res = "";
        if (isEmpty()) {res += name+" is Empty ";} 
        else if (isFull()) { res += name+"FEL is Full ";}
        boolean isHeap = isHeap();
        res += " | Heapness of " + name + (isHeap ? "is currently preserved. " : "IS NOT PRESERVED!!! ");
        int numElems = size();
        res += " | " +name +" currently has " + numElems + " elements.";
        return res;
    }//TEST_verifyHeapState
        
}//class myPriorityQueue

