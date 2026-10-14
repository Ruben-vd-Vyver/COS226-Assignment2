import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Consumer extends Thread {
    private final ConcurrentQueue<Job> queue;
    private final int totalJobs;
    private final AtomicInteger tickets;
    private final AtomicInteger processedCount;
    private final AtomicIntegerArray seen;
    private int localCount = 0;

    public Consumer(int consumerId, ConcurrentQueue<Job> queue, int totalJobs, AtomicInteger tickets,
            AtomicInteger processedCount, AtomicIntegerArray seen) {
        super("Consumer-" + consumerId);
        this.queue = queue;
        this.totalJobs = totalJobs;
        this.tickets = tickets;
        this.processedCount = processedCount;
        this.seen = seen;
    }

    public int getLocalCount() {
        return localCount;
    }

    @Override
    public void run() {
        while (tickets.getAndIncrement() < totalJobs) {
            Job job = queue.deq();
            while (job == null) {
                if (Thread.currentThread().isInterrupted())
                    return;
                Thread.onSpinWait();
                job = queue.deq();
            }
            process(job);
        }
    }

    private void process(Job job) {
        seen.incrementAndGet(job.getId());
        processedCount.incrementAndGet();
        localCount++;
    }
}