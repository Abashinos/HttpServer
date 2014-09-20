package supplies;

public final class Parameters {

    private int port;
    private int workersNum;

    public Parameters() {
        this(9000, 16);
    }

    public Parameters(int port, int workers) {
        this.setPort(port);
        this.setWorkersNum(workers);
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
}
