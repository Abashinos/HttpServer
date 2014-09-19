package worker;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SocketReadCompleteHandler implements CompletionHandler {

    private ByteBuffer buffer;
    private AsynchronousSocketChannel socket;
    private Worker worker;

    public SocketReadCompleteHandler(ByteBuffer buffer, AsynchronousSocketChannel socket, Worker worker) {
        this.buffer = buffer;
        this.socket = socket;
        this.worker = worker;
    }

    @Override
    public void completed(Object result, Object attachment) {
        buffer.flip();
        worker.handle(buffer, socket);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
