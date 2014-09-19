package worker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class SocketWriteCompleteHandler implements CompletionHandler {

    private ByteBuffer buffer;
    private AsynchronousSocketChannel socket;

    public SocketWriteCompleteHandler(ByteBuffer buffer, AsynchronousSocketChannel socket) {
        this.buffer = buffer;
        this.socket = socket;
    }

    @Override
    public void completed(Object result, Object attachment) {
        while (buffer.position() < buffer.capacity()) {
            socket.write(buffer, null, this);
        }
        buffer.clear();
        try {
            socket.close();
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
