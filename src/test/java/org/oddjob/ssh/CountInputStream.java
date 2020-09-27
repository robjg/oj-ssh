package org.oddjob.ssh;

import org.oddjob.values.types.SequenceIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

class CountInputStream extends InputStream {

    private final int to;

    private static final Logger logger = LoggerFactory.getLogger(CountInputStream.class);

    private final Iterator<Integer> it;

    private ByteBuffer bytes;

    CountInputStream(int to) {
        this.to = to;
        it = new SequenceIterable(1, 1_000_000, 1).iterator();
    }

    @Override
    public int read() throws IOException {
        if (bytes == null) {
            if (it.hasNext()) {
                int next = it.next();
                if (next % 1000 == 0) {
                    logger.info("Sending: " + next);
                }
                bytes = ByteBuffer.wrap(String.valueOf(next).getBytes());
            } else {
                return -1;
            }
        }
        if (bytes.hasRemaining()) {
            return bytes.get();
        } else {
            bytes = null;
            return '\n';
        }
    }
}
