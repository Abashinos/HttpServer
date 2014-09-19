package supplies;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Decoder {
    private static Charset charset = Charset.forName("UTF-8");
    public static CharsetDecoder decoder = charset.newDecoder();
}
