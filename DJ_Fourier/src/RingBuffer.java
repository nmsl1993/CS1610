import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This code adapted from https://jmonkeyengine.googlecode.com/svn/trunk/engine/src/android/com/jme3/util/RingBuffer.java
 * Ring buffer (fixed size queue) implementation using a circular array (array
 * with wrap-around).
 */
// suppress unchecked warnings in Java 1.5.0_6 and later
@SuppressWarnings("unchecked")
public class RingBuffer{

    private float[] buffer;          // queue elements
    private int count = 0;          // number of elements on queue
    private int head_index = 0;       // index of next available slot
    // cast needed since no generic array creation in Java
    public RingBuffer(int capacity) {
        buffer =  new float[capacity];
    }
    public int getCapacity()
    {
    	return buffer.length;
    }
    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    public void push(float item) { 
        buffer[head_index] = item;
        head_index = (head_index + 1) % buffer.length;     // wrap-around
        if(count < buffer.length)
        {
        count++;
        }
    }
    public float get(int ind)
    {
    	int i = ((((head_index - ind  - 1)% buffer.length) + buffer.length) % buffer.length);
    	return buffer[i];
    	//return buffer[100];
    	//return buffer[0];
    }

}
