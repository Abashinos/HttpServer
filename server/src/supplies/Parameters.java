package supplies;

/**
 * Created by user on 11.09.2014.
 */
public final class Parameters {

    private int port;
    private int workersNum;
    private int readBufferSize;

    public Parameters() {
        this(9000, 4, 1024);
    }

    public Parameters(int port, int workers) {
        this(port, workers, 1024);
    }

    public Parameters(int port, int workers, int readBufferSize) {
        this.setPort(port);
        this.setWorkersNum(workers);
        this.setReadBufferSize(readBufferSize);
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

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }
}
