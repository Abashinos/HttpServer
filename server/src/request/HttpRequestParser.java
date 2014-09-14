package request;

import Exceptions.BadRequestException;

import java.io.*;

import static java.net.URLDecoder.decode;

public class HttpRequestParser {

    public static void parse(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            System.out.println(line);
        }
    }

    public static String getRequestMethod(String request) {
        int end = request.indexOf(" ");
        return ((end != 0) ? (request.substring(0, end)) : null);
    }

    public static String getRequestPath(String request) throws BadRequestException {
        int pathStart = request.indexOf(" ") + 1;
        int pathEnd = request.indexOf(" ", pathStart);
        int httpStart = request.indexOf("HTTP", pathStart);

        if (pathEnd + 1 != httpStart) {
            throw new BadRequestException();
        }

        String path = request.substring(pathStart, pathEnd);
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //query?
        if (path == null || path.length() == 0) {
            throw new BadRequestException();
        }
        return path;
    }
}
