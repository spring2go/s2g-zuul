package io.spring2go.zuul.common;

import java.io.IOException;
import javax.servlet.ServletInputStream;
/**
 * ServletInputStream wrapper to wrap a byte[] into a ServletInputStream
 *
 */
public class ServletInputStreamWrapper extends ServletInputStream {

    private byte[] data;
    private int idx = 0;

    /**
     * Creates a new <code>ServletInputStreamWrapper</code> instance.
     *
     * @param data a <code>byte[]</code> value
     */
    public ServletInputStreamWrapper(byte[] data) {
        if (data == null)
            data = new byte[0];
        this.data = data;
    }

    @Override
    public int read() throws IOException {
        if (idx == data.length)
            return -1;
        // I have to AND the byte with 0xff in order to ensure that it is returned as an unsigned integer
        // the lack of this was causing a weird bug when manually unzipping gzipped request bodies
        return data[idx++] & 0xff;
    }

}

