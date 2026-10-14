import java.util.concurrent.atomic.AtomicReference;


 //Using a sentinel node
 //and compareAndSet().
 //No locks are used.
 //deq() returns null if the queue is empty at the moment of the call.
public class LockFreeQueue<T> implements ConcurrentQueue<T> {

    private static class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next = new AtomicReference<>(null);
        Node(T value) { this.value = value; }
    }

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    public LockFreeQueue() {
        Node<T> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    @Override
    public void enq(T item) {
        if (item == null) throw new NullPointerException("null items not allowed");
        Node<T> node = new Node<>(item);
        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();
            if (last == tail.get()) {
		    if (next == null) {
                    	if (last.next.compareAndSet(null, node)) {
                        // linearization point done
                        tail.compareAndSet(last, node);
                        return;
                    }
                } else {
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    @Override
    public T deq() {
        while (true) {
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();
            if (first == head.get()) {                      
                if (first == last) {
                    if (next == null) {
                        return null;                        // queue is empty
                    }
                    tail.compareAndSet(last, next);
                } else {
                    T value = next.value;
                    // next becomes the new sentinel
                    if (head.compareAndSet(first, next)) {
                        return value;
                    }
                }
            }
        }
    }
}
