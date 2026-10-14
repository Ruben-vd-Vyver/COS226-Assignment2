import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

// Defaults for mathias :bounded 4 4 12000 100
public class Main {

    static final class Result {
        final String queueName;
        final int producers, consumers, totalJobs;
        final long nanos;
        final boolean correct;

        Result(String q, int p, int c, int t, long n, boolean ok) {
            queueName = q;
            producers = p;
            consumers = c;
            totalJobs = t;
            nanos = n;
            correct = ok;
        }

        double seconds() {
            return nanos / 1e9;
        }

        double throughput() {
            return totalJobs / seconds();
        }
    }

    /// hureee
    public static void main(String[] args) throws InterruptedException {
        String type = args.length > 0 ? args[0].toLowerCase() : "bounded";
        int producers = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        int consumers = args.length > 2 ? Integer.parseInt(args[2]) : 4;
        int total = args.length > 3 ? Integer.parseInt(args[3]) : 12000;
        int capacity = args.length > 4 ? Integer.parseInt(args[4]) : 100;

        if (total % producers != 0) {
            System.err.println("totalJobs must divide even between producers.");
            return;
        }
        run(type, producers, consumers, total, capacity, true);
    }

    static Result run(String type, int producers, int consumers, int total, int capacity, boolean verbose)
            throws InterruptedException {
        ConcurrentQueue<Job> queue;
        String name;
        //// check here for types
        if (type.equals("lockfree")) {
            queue = new LockFreeQueue<>();
            name = "LockFreeQueue";
        } else if (type.equals("bounded")) {
            queue = new BoundedQueue<>(capacity);
            name = "BoundedQueue(cap=" + capacity + ")";
        } else {
            throw new IllegalArgumentException("queue type must be 'bounded' or 'lockfree'");
        }

        int perProducer = total / producers;
        AtomicInteger tickets = new AtomicInteger(0);
        AtomicInteger processed = new AtomicInteger(0);
        AtomicIntegerArray seen = new AtomicIntegerArray(total);

        Producer[] ps = new Producer[producers];
        Consumer[] cs = new Consumer[consumers];
        for (int i = 0; i < producers; i++)
            ps[i] = new Producer(queue, i, perProducer, i * perProducer);
        for (int i = 0; i < consumers; i++)
            cs[i] = new Consumer(i, queue, total, tickets, processed, seen);

        long start = System.nanoTime();
        for (Producer p : ps)
            p.start();
        for (Consumer c : cs)
            c.start();
        for (Producer p : ps)
            p.join();
        for (Consumer c : cs)
            c.join();
        long elapsed = System.nanoTime() - start;

        int missing = 0, duplicates = 0;
        for (int i = 0; i < total; i++) {
            int n = seen.get(i);
            if (n == 0)
                missing++;
            else if (n > 1)
                duplicates++;
        }
        boolean ok = processed.get() == total && missing == 0 && duplicates == 0;
        Result r = new Result(name, producers, consumers, total, elapsed, ok);

        if (verbose) {
            System.out.println("Queue: " + name);
            System.out.println("Producers: " + producers + " (" + perProducer + " jobs each)");
            System.out.println("Consumers: " + consumers);
            System.out.println("Jobs produced: " + total);
            System.out.println("Jobs processed: " + processed.get());
            System.out.println("Missing / dupes: " + missing + " / " + duplicates);
            for (Consumer c : cs)
                System.out.println("  " + c.getName() + " processed " + c.getLocalCount());
            System.out.printf("Execution time:    %.4f s%n", r.seconds());
            System.out.printf("Throughput:        %.0f jobs/s%n", r.throughput());
            System.out.println("Result: " + (ok ? "PASS - all jobs processed exactly once" : "FAIL"));
        }
        return r;
    }
}