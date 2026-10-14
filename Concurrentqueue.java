//common interface for lucian

public interface ConcurrentQueue<T> {
    //Adds an item to the tail of the queue.
    void enq(T item);

    T deq();
}
