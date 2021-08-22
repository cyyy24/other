import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit ;

// This class is to test MyConcurrentQueue.java
// It assumes Writers will keep writing and Readers will keep reading.
public class MyConcurrentQueueTest {
    private static final int QUEUE_CAPACITY = 5;   // change this to test various queue capacity.

    public static void main(String[] args) {

            final MyConcurrentQueue<Integer> myCQ = new MyConcurrentQueue<>(QUEUE_CAPACITY);
            
            // Create 2 writer threads:
            Writer w1 = new Writer(myCQ, 0, 10);
            Writer w2 = new Writer(myCQ, 10, 20);
            Thread tw1 = new Thread(w1);
            Thread tw2 = new Thread(w2);

            // Create 2 reader threads:
            Reader r1 = new Reader(myCQ);
            Reader r2 = new Reader(myCQ);
            Thread tr1 = new Thread(r1);
            Thread tr2 = new Thread(r2);

            tr1.start();
            tr2.start();
            tw1.start();
            tw2.start();

    }    
}

// Writer class that writes Integer given start(inclusive) and end(exclusive) number to MyConcurrentQueue.
class Writer implements Runnable {
    private int startNum;  // first (inclusive) int that will be written.
    private int endNum;    // last int (exclusive) that will be written.
    private MyConcurrentQueue myCQ;

    private volatile boolean exit = false;

    public Writer(MyConcurrentQueue myCQ, int startNum, int endNum) {
        this.myCQ = myCQ;
        this.startNum = startNum;
        this.endNum = endNum;
    }
    
    public void run() {
        while (!exit) {
            try {
                int count = 0;
                int total = endNum - startNum;
                while (startNum < endNum) {
                    myCQ.push(startNum);
                    System.out.println(Thread.currentThread().getName() + " -- WRITE -- " + startNum);
                    ++startNum;
                    ++count;
                    if (count == total) {
                        System.out.println(Thread.currentThread().getName() + " ** FINISHED ** " + total + " numbers written.");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Writer InterruptedException");
            }
        } 
    }
    
    public void stop() {
        exit = true;
    }
}

// Reader class that reads Integer in MyConcurrentQueue.
class Reader implements Runnable {
    private MyConcurrentQueue myCQ;
    private static final Set<Integer> seen = new HashSet<>();;   // to help check if threads ever read the same value.

    private volatile boolean exit = false;

    public Reader(MyConcurrentQueue myCQ) {
        this.myCQ = myCQ;
    }

    public void run() {
        while (!exit) {
            try {
                while (true) {
                    Thread.sleep(2000); // wait for 2s for every read.
                    Integer readVal = (Integer)myCQ.pop();
                    if (seen.add(readVal)) {
                        System.out.println(Thread.currentThread().getName() + " -- READ -- " + readVal);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " ** SEEN AGAIN ** " + readVal);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Reader InterruptedException");
            }
        }
    }

    public void stop() {
        exit = true;
    }
}