package response;

import worker.Worker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class FileReadCompleteHandler implements CompletionHandler {

    private AsynchronousSocketChannel socketChannel;
    private AsynchronousFileChannel fileChannel;
    private Worker worker;
    private ByteBuffer buffer;

    public FileReadCompleteHandler (AsynchronousSocketChannel socketChannel, AsynchronousFileChannel fileChannel,
                                    Worker worker, ByteBuffer buffer) {
        this.socketChannel = socketChannel;
        this.fileChannel = fileChannel;
        this.worker = worker;
        this.buffer = buffer;
    }

    @Override
    public void completed(Object result, Object attachment) {
        try {
            fileChannel.close();
            String path = (String) attachment;
            worker.writeFile(socketChannel, buffer, path);
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
