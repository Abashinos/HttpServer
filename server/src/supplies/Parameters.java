package supplies;

public final class Parameters {

    private int port;
    private int workersNum;
    private int backlog;

    public Parameters() {
        this(9000, 16, 200);
    }

    public Parameters(int port, int workers) {
        this(port, workers, 200);
    }

    public Parameters(int port, int workers, int backlog) {
        this.setPort(port);
        this.setWorkersNum(workers);
        this.setBacklog(backlog);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkersNum() {
        return workersNum;
    }

    public void setWorkersNum(int workers) {
        this.workersNum = workers;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }
}
