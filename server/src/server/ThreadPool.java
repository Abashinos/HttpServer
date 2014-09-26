package server;

import supplies.Parameters;
import worker.Worker;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadPool {

    private LinkedBlockingQueue<AsynchronousSocketChannel> requests = new LinkedBlockingQueue<AsynchronousSocketChannel>();
    private ConcurrentLinkedQueue<Worker> workers = new ConcurrentLinkedQueue<Worker>();
    public static AtomicInteger FILE_CHANNELS_OPEN = new AtomicInteger();
    public static AtomicInteger FILES_IN_CACHE = new AtomicInteger();

    public ThreadPool (Parameters parameters) {
        startWorkers(parameters.getWorkersNum());
    }


    public void startWorkers (int workersNum) {

        for (int i = 0; i < workersNum; ++i) {
            Worker worker = new Worker(this, i);
            workers.add(worker);

            (new Thread(worker)).start();
        }
    }

    public void workComplete (Worker worker) {
        worker.setSocket(null);
        AsynchronousSocketChannel socket = requests.poll();

        if (socket == null) {
            workers.add(worker);
        }
        else {
            worker.setSocket(socket);
            worker.notify();
        }
    }

    public void acceptRequest(AsynchronousSocketChannel socket) {
        Worker worker = workers.poll();

        if (worker == null) {
            requests.add(socket);
        }
        else {
            synchronized (worker) {
                //System.out.println("Work assigned.");
                worker.setSocket(socket);
                worker.notify();
            }
        }
    }
}
