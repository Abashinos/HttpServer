package response;

import server.ThreadPool;
import supplies.Parameters;
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

    public static ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    @Override
    public void completed(Object result, Object attachment) {

        try {
            fileChannel.close();
            ThreadPool.FILE_CHANNELS_OPEN.decrementAndGet();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String path = (String) attachment;
        if (Parameters.CACHE_ENABLED) {
            worker.addToCache(path, clone(buffer));
            //buffer.position(0);
        }
        worker.writeFile(socketChannel, buffer, path, false);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
