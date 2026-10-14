import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//Uses a sentinel node so enqueuers and
//dequeuers touch different ends of the list and rarely contend.
public class BoundedQueue<T> implements ConcurrentQueue<T> {

    private class Node {
        final T value;
        volatile Node next;
        Node(T value) { this.value = value; }
    }

    private final ReentrantLock enqLock = new ReentrantLock();
    private final ReentrantLock deqLock = new ReentrantLock();
    private final Condition notFull = enqLock.newCondition();
    private final Condition notEmpty = deqLock.newCondition();

    private final AtomicInteger size = new AtomicInteger(0);
    private final int capacity;

    private volatile Node head; // sentinel; head.next is the first real item
    private volatile Node tail;

    public BoundedQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        head = new Node(null);
        tail = head;
    }

    @Override
    public void enq(T item) {
        if (item == null) throw new NullPointerException("null items not allowed");
        boolean mustWakeDequeuers = false;
        enqLock.lock();
        try {
            while (size.get() == capacity) {
                try {
                    notFull.await();            
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            Node n = new Node(item);
            tail.next = n;
            tail = n;
            // 0 means queue was empty
            if (size.getAndIncrement() == 0) {
                mustWakeDequeuers = true;
            }
        } finally {
            enqLock.unlock();
        }
        if (mustWakeDequeuers) {
            deqLock.lock();
            try {
                notEmpty.signalAll();
            } finally {
                deqLock.unlock();
            }
        }
    }

    @Override
    public T deq() {
        T result;
        boolean mustWakeEnqueuers = false;
        deqLock.lock();
        try {
            while (size.get() == 0) {
                try {
                    notEmpty.await();           
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            result = head.next.value;
            head = head.next;                   
            // old size == capacity means queue was full
            if (size.getAndDecrement() == capacity) {
                mustWakeEnqueuers = true;
            }
        } finally {
            deqLock.unlock();
        }
        if (mustWakeEnqueuers) {
            enqLock.lock();
            try {
                notFull.signalAll();
            } finally {
                enqLock.unlock();
            }
        }
        return result;
    }

    public int size() { return size.get(); }
    public int capacity() { return capacity; }
}
