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

        int customPort = 0;
        int customWorkersNum = 0;
        try {
            if (args.length != 0) {
                customPort = Integer.parseInt(args[0]);
                customWorkersNum = Integer.parseInt(args[1]);
            }
        }
        catch (IndexOutOfBoundsException ignored) {}
        catch (NumberFormatException ignored) {}

        final Parameters serverParams = new Parameters((customPort == 0)?(9000):(customPort),
                                                       (customWorkersNum == 0)?(8):(customWorkersNum));

        final ThreadPool threadPool = new ThreadPool(serverParams);

        final AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(ADDRESS, serverParams.getPort()), serverParams.getBacklog());
        System.out.println("Server is starting on port " + serverParams.getPort() +
                           " with " + serverParams.getWorkersNum() +
                            ((serverParams.getWorkersNum() == 1) ? " worker." : " workers."));

        ssc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                ssc.accept(null, this);
                threadPool.acceptRequest(result);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });

        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored) {
                return;
            }
        }
    }
}
