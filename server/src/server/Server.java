package server;

import supplies.Parameters;

import static supplies.Constants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {

    public static void main (String[] args) throws IOException {
        final Parameters serverParams = new Parameters();
        final ThreadPool threadPool = new ThreadPool(serverParams);

        final AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(ADDRESS, serverParams.getPort()));

        ssc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                ssc.accept(null, this);
                System.out.println("Request accepted.");
                threadPool.acceptRequest(result);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        });

        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored) {
                break;
            }
        }
    }
}
