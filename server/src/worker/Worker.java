package worker;

import Exceptions.BadRequestException;
import response.Response;
import server.ThreadPool;
import supplies.Parameters.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import static request.HttpRequestParser.*;
import static response.Response.getResponseHeader;
import static response.Response.makeResponseHeader;
import static supplies.Constants.*;

public class Worker implements Runnable{

    private ThreadPool threadPool = null;
    private AsynchronousSocketChannel socket = null;
    private int workerId = 0;
    private int bufferSize = 4096;

    public Worker(ThreadPool threadPool, int id) {
        this.threadPool = threadPool;
        this.workerId = id;
    }

    @Override
    public synchronized void run() {
        while(true) {
            if (getSocket() == null) {
                try {
                    this.wait();
                }
                catch (InterruptedException ignored) {

                }
            }

            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            socket.read(buffer, null, new SocketReadCompleteHandler(buffer, socket, this));

            //System.out.println("Work complete");
            threadPool.workComplete(this);
        }
    }

    public AsynchronousSocketChannel getSocket() {
        return socket;
    }

    public void setSocket(AsynchronousSocketChannel socket) {
        this.socket = socket;
    }

    public void makeResponse(AsynchronousSocketChannel socket, String response) {
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        socket.write(buffer, null, new SocketWriteCompleteHandler(buffer, socket));
    }

    public void writeFile (AsynchronousSocketChannel socket, ByteBuffer buffer, String path) {
        String preparedHeader = Response.makeResponseHeader(OK, Response.getExtension(path), buffer.capacity());
        ByteBuffer wrappedHeader = ByteBuffer.wrap(preparedHeader.getBytes());
        buffer.flip();

        ByteBuffer fileResponse = ByteBuffer.allocate(buffer.capacity() + preparedHeader.length()).put(wrappedHeader).put(buffer);
        fileResponse.position(0);
        socket.write(fileResponse, null, new SocketWriteCompleteHandler(fileResponse, socket));
    }

    public void handle(ByteBuffer buffer, AsynchronousSocketChannel socket) {
        String path, method;

        String request = new String(buffer.array());

        try {
            path = getRequestPath(request);
            method = getRequestMethod(request);

            if (path == null || method == null) {
                throw new BadRequestException();
            }
            if (!method.equals("HEAD") && !method.equals("GET")) {
                makeResponse(socket, makeResponseHeader(METHOD_NOT_ALLOWED));
                return;
            }
        }
        catch (BadRequestException ignored) {
            makeResponse(socket, makeResponseHeader(BAD_REQUEST));
            return;
        }

        path = ROOT + path;
        File file = new File(path);
        try {
            if (!file.getCanonicalPath().contains(ROOT)) {
                makeResponse(socket, makeResponseHeader(FORBIDDEN));
                return;
            }
            else if (file.isDirectory()) {
                file = new File(path + File.separator + "index.html");
                if (!file.exists()) {
                    makeResponse(socket, makeResponseHeader(FORBIDDEN));
                    return;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.exists()) {
            makeResponse(socket, makeResponseHeader(NOT_FOUND));
        }

        if (method.equals("GET")) {
            try {
                Response.readFile(this, socket, file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (method.equals("HEAD")) {
            System.out.println("Writing response to a HEAD request.");
            ByteBuffer writeBuffer = ByteBuffer.wrap(getResponseHeader(file).getBytes());
            socket.write(writeBuffer, null, new SocketWriteCompleteHandler(writeBuffer, socket));
        }
    }

}
