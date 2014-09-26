package response;

import com.sun.deploy.net.socket.UnixSocketException;
import server.ThreadPool;
import supplies.Parameters;
import worker.Worker;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static supplies.Constants.*;

public class Response {

    public static AtomicInteger READS_FROM_HDD = new AtomicInteger();

    public static String getExtension (String path) {
        return path.substring(path.lastIndexOf(".") + 1, path.length()).toLowerCase();
    }

    public static void readFile(Worker worker, AsynchronousSocketChannel socket, File file) throws IOException {
        AsynchronousFileChannel fileChannel = null;
        String path = file.getAbsolutePath();

        if(!Parameters.CACHE_ENABLED || (Parameters.CACHE_ENABLED && !worker.cacheContains(path))) {
            while (true) {
                try {
                    fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
                    ThreadPool.FILE_CHANNELS_OPEN.incrementAndGet();
                    System.out.println(ThreadPool.FILE_CHANNELS_OPEN);
                    break;
                } catch (IOException e) {

                }
            }
        }



        if (Parameters.CACHE_ENABLED && worker.cacheContains(path)) {
            worker.writeFile(socket, worker.getFromCache(path), path, true);
        }
        else {
            ByteBuffer writeBuffer = ByteBuffer.allocate((int)file.length());
            //System.out.println(READS_FROM_HDD.incrementAndGet());
            fileChannel.read(writeBuffer, 0, path, new FileReadCompleteHandler(socket, fileChannel, worker, writeBuffer));
        }
    }

    public static String getTypeByPath (String path) {
        if (path == null) {
            return "text/html";
        }
        final String ext = path.substring(path.lastIndexOf(".") + 1, path.length()).toLowerCase();
        String extType = null;

        if (ext.equals("html")) {
            extType = "text/html";
        }
        else if (ext.equals("jpg") || ext.equals("jpeg")) {
            extType = "image/jpeg";
        }
        else if (ext.equals("css")) {
            extType = "text/css";
        }
        else if (ext.equals("js")) {
            extType = "text/javascript";
        }
        else if (ext.equals("png")) {
            extType = "image/png";
        }
        else if (ext.equals("gif")) {
            extType = "image/gif";
        }
        else if (ext.equals("swf")) {
            extType = "application/x-shockwave-flash";
        }

        return extType;
    }

    public static String getResponseHeader(File file) {
        return makeResponseHeader(OK, file.getPath(), file.length());
    }

    public static String getTimeToString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static String makeResponseHeader(int responseCode) {
        return makeResponseHeader(responseCode, null, 0);
    }

    public static String makeResponseHeader(int responseCode, String path, long contentLength) {
        String response = "HTTP/1.1 ";

        switch (responseCode) {
            case OK:
                response += "200 OK";
                break;
            case FORBIDDEN:
                response += "403 FORBIDDEN";
                break;
            case NOT_FOUND:
                response += "404 NOT FOUND";
                break;
            default:
                response += "405 METHOD NOT ALLOWED";
                break;
        }
        response += "\r\n";
        response += "Date: " + getTimeToString() + "\r\n";
        response += "Server: AbashinServer" + "\r\n";
        response += "Content-Type: " + getTypeByPath(path) + "\r\n";
        response += "Content-Length: " + contentLength + "\r\n";
        response += "Connection: close" + "\r\n\r\n";

        return response;
    }
}
