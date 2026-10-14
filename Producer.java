// Gen job and put in shared queue
public class Producer extends Thread {
    private final ConcurrentQueue<Job> queue;
    private final int producerId;
    private final int jobsToProduce;
    private final int firstJobId;

    public Producer(ConcurrentQueue<Job> queue, int producerId, int jobsToProduce, int firstJobId) {
        super("Producer-" + producerId);
        this.queue = queue;
        this.producerId = producerId;
        this.jobsToProduce = jobsToProduce;
        this.firstJobId = firstJobId;
    }

    @Override
    public void run() {
        for (int i = 0; i < jobsToProduce; i++) {
            queue.enq(new Job(firstJobId + i, producerId));
        }
    }
}
