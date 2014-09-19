package worker;

import Exceptions.BadRequestException;
import response.Response;
import server.ThreadPool;
import supplies.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.CharacterCodingException;

import static request.HttpRequestParser.*;
import static response.Response.getResponseHeader;
import static response.Response.makeResponseHeader;
import static supplies.Decoder.decoder;
import static supplies.Constants.*;
/**
 * Created by user on 11.09.2014.
 */
public class Worker implements Runnable{

    private ThreadPool threadPool = null;
    private AsynchronousSocketChannel socket = null;
    private int workerId = 0;
    //private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public Worker(ThreadPool threadPool, int id) {
        this.threadPool = threadPool;
        this.workerId = id;
    }

    @Override
    public synchronized void run() {
        System.out.println("Worker #" + workerId + " started.");
        while(true) {
            if (getSocket() == null) {
                try {
                    this.wait();
                }
                catch (InterruptedException ignored) {

                }
            }
            System.out.println("Worker #" + workerId + " is working.");

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            socket.read(buffer, null, new SocketReadCompleteHandler(buffer, socket, this));

            System.out.println("Work complete");
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

    @SuppressWarnings("null")
    public void handle(ByteBuffer buffer, AsynchronousSocketChannel socket) {
        String request = null;
        String path = null;
        String method = null;


        try {
            request = decoder.decode(buffer).toString();
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        try {
            path = getRequestPath(request);
            method = getRequestMethod(request);

            if (path == null || method == null) {
                throw new BadRequestException();
            }
            System.out.println("Requested " + path + " with method " + method);
            if (!method.equals("HEAD") && !method.equals("GET")) {
                makeResponse(socket, makeResponseHeader(METHOD_NOT_ALLOWED));
            }
        }
        catch (BadRequestException ignored) {
            makeResponse(socket, makeResponseHeader(BAD_REQUEST));
        }

        path = Constants.ROOT + path;
        File file = new File(path);
        try {
            if (!file.getCanonicalPath().contains(Constants.ROOT)) {
                makeResponse(socket, makeResponseHeader(FORBIDDEN));
            }
            else if (!file.exists()) {
                makeResponse(socket, makeResponseHeader(NOT_FOUND));
            }
            else if (file.isDirectory()) {
                file = new File(path + File.separator + "index.html");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.exists()) {
            makeResponse(socket, makeResponseHeader(NOT_FOUND));
        }

        // return response or file
        ByteBuffer writeBuffer = null;
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
            writeBuffer = ByteBuffer.wrap(getResponseHeader(file).getBytes());
            try {
                System.out.println("\r\n" + decoder.decode(writeBuffer).toString() + "\r\n");
            }
            catch (CharacterCodingException e) {
                e.printStackTrace();
            }
            socket.write(writeBuffer, null, new SocketWriteCompleteHandler(writeBuffer, socket));
        }
    }

}
