package server;

import supplies.Parameters;

import static supplies.Constants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {

    public static void main (String[] args) throws IOException {
        Parameters serverParams = new Parameters();
        ServerSocket serverSocket = new ServerSocket(serverParams.getPort());

        final AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(address, serverParams.getPort()));

        ssc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                ssc.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        });

        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }
}
