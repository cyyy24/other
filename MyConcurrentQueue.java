import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
  * Implementation of a queue that supports multi-writer and multi-reader
  * concurrent operations. It ensures all writers' input will not be lost
  * and all readers will not pop out the same element.
  *
  * Clarification: 
  * 1) an Object[] can also be used to for implementing this Queue instead of
  *    using a LinkedList. That way it will be a "fixed" size queue even though
  *    this implementation has a capacity, we can actually go with the limit a LinkedList
  *    is capable of storing.
  */
public class MyConcurrentQueue<E> {

    // Use a Queue to take input objects.
    private Queue<E> que = new LinkedList<>();
    
    // Current number of objects in the queue.
    private AtomicInteger count = new AtomicInteger();

    // Maximum number of objects allowed in the queue.
    private final int capacity;


    // Constructor.
    public MyConcurrentQueue(int capacity) {
        this.capacity = capacity;
    }
   

    // Locks that are used for concurrent push and pop respectively.
    private final ReentrantLock pushLock = new ReentrantLock();
    private final ReentrantLock popLock = new ReentrantLock();

    // For suspending poping threads when queue is empty.
    private final Condition notEmpty = popLock.newCondition();
    // For suspending pushing threads when queue is full.
    private final Condition notFull = pushLock.newCondition();


    /**
      * Method that takes an Object as input and push it into the queue.
      * If the queue is full, it will keep blocking the thread until
      * the queue is not full.
      */ 
    public void push(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        
        int prevCount = -1;  // temporarily set it to -1
        final AtomicInteger count = this.count;
        final ReentrantLock pushLock = this.pushLock;
        pushLock.lock();
        try {
            // If queue is full, wait until it is no longer full.
            while (count.get() == capacity) {
                notFull.await();
            }
            // Add the input object to the queue.
            que.offer(e);
            prevCount = count.getAndIncrement();
            // If queue is still not full, signal other pushing threads.
            if (prevCount + 1 < capacity) {
                notFull.signal();
            }
        } 
        finally {
            pushLock.unlock();
        }
        
        // If previously there was no element in the queue, we need to signal potential threads
        // waiting for the queue to be not empty.
        if (prevCount == 0) {
            signalNotEmpty();
        }
    }

    /**
      * Method that returns the oldest elements pushed into the queue.
      * If the queue is empty, it will keep blocking the thread until
      * the queue is not empty.
      */ 
    public E pop() throws InterruptedException {
        E e = null;
        
        int prevCount = -1;  // temporarily set it to -1
        
        final AtomicInteger count = this.count;
        final ReentrantLock popLock = this.popLock;
        popLock.lock();
        try {
            // If queue is empty, wait until it is no longer empty.
            while (count.get() == 0) {
                notEmpty.await();
            }
            // Get the oldest object from queue.
            e = que.poll();
            prevCount = count.getAndDecrement();
            // If there is still objects in the queue, signal other popping threads.
            if (prevCount > 1)
                notEmpty.signal();
        } 
        finally {
            popLock.unlock();
        }

        // If previously queue was full, now we can signal pushing threads.
        if (prevCount == capacity)
            signalNotFull();
        return e;
    }


    /**
      * Private method.
      * It signals the waiting thread that the queue is no longer empty.
      */
    private void signalNotEmpty() {
       popLock.lock();
       try {
           notEmpty.signal();
       } finally {
           popLock.unlock();
      }
    }

    /**
      * Private method.
      * It signals the waiting thread that the queue is no longer full.
      */
      private void signalNotFull() {
        pushLock.lock();
        try {
            notFull.signal();
        } finally {
            pushLock.unlock();
       }
     }
    
}