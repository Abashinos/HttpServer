package worker;

import Exceptions.BadRequestException;
import server.ThreadPool;
import supplies.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import static request.HttpRequestParser.*;
import static response.Response.getResponseHeader;
/**
 * Created by user on 11.09.2014.
 */
public class Worker implements Runnable{

    private ThreadPool threadPool = null;
    private AsynchronousSocketChannel socket = null;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public Worker(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        while(true) {
            if (getSocket() == null) {
                try {
                    this.wait();
                }
                catch (InterruptedException ignored) {}
            }

            //TODO: listener
            this.getSocket().read(buffer);

            threadPool.workComplete(this);
        }
    }

    public AsynchronousSocketChannel getSocket() {
        return socket;
    }

    public void setSocket(AsynchronousSocketChannel socket) {
        this.socket = socket;
    }

    public void handle(ByteBuffer buffer, AsynchronousSocketChannel socket) {
        String request = buffer.toString();
        String path = null;
        String method = null;
        try {
            path = getRequestPath(request);
            method = getRequestMethod(request);
            if (!method.equals("HEAD") && !method.equals("GET")) {
                // return method not allowed
            }
        }
        catch (BadRequestException ignored) {
            // return bad request
        }

        File file = new File(Constants.ROOT + path);
        try {
            if (!file.getCanonicalPath().contains(Constants.ROOT)) {
                // return forbidden
            }
            else if (!file.exists()) {
                // return not found
            }
            else if (file.isDirectory()) {
                file = new File(path + File.separator + "index.html");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.exists()) {
            // return not found
        }

        // return response or file
        ByteBuffer writeBuffer = null;
        if (method.equals("GET")) {

        }
        else if (method.equals("HEAD")) {
            writeBuffer = ByteBuffer.wrap(getResponseHeader(file).getBytes());
        }
        // write??
    }

}
