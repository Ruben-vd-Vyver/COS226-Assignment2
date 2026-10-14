// singlew unit of work
public final class Job {
    private final int id;
    private final int producerId;

    public Job(int id, int producerId) {
        this.id = id;
        this.producerId = producerId;
    }

    public int getId() {
        return id;
    }

    public int getProducerId() {
        return producerId;
    }

    @Override
    public String toString() {
        return "Job#" + id + " (producer " + producerId + ")";
    }
}