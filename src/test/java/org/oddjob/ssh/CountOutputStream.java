package org.oddjob.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class CountOutputStream extends OutputStream {

    private static final Logger logger = LoggerFactory.getLogger(CountOutputStream.class);

    private int last = 0;

    private ByteBuffer bytes = ByteBuffer.allocate(10);

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            String s = new String(bytes.array(), 0, bytes.position());
            int next = Integer.parseInt(s);
            if (next != last + 1) {
                throw new IOException("Unexpected value " + next);
            }
            last = next;
            if (last % 1000 == 0) {
                logger.info("Count: " + last);
            }
            bytes = ByteBuffer.allocate(10);
        } else {
            bytes.put((byte) b);
        }
    }

    @Override
    public void close() throws IOException {
        if (bytes.position() != 0) {
            throw new IOException("LF expected at EOF");
        }
    }

    public int getLast() {
        return last;
    }
}
