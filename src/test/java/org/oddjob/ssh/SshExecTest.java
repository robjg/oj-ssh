package org.oddjob.ssh;

import org.junit.jupiter.api.Test;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.*;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.tools.ConsoleCapture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SshExecTest {

    @Test
    void testPump() {

        ByteArrayInputStream in = new ByteArrayInputStream("First Line\nSecond Line no LF".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        SshExecJobBroken.Pump pump = new SshExecJobBroken.Pump("test", in, out);

        pump.run();

        assertThat(new String(out.toByteArray()), is("First Line\nSecond Line no LF\n"));
    }

    @Test
    void testPumpToConsole() throws InterruptedException {

        ByteArrayInputStream in = new ByteArrayInputStream("First Line\nSecond Line no LF".getBytes());

        LogArchiveImpl consoleArchive = new LogArchiveImpl(
                "TEST-1", LogArchiver.MAX_HISTORY);

        OutputStream out = new LoggingOutputStream(null,
                LogLevel.INFO, consoleArchive);

        SshExecJobBroken.Pump pump = new SshExecJobBroken.Pump("test", in, out);

        Thread t = new Thread(pump);

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.capture(consoleArchive) ){
            t.start();
            t.join();
        }

        capture.dump();

        String[] lines = capture.getLines();

        assertThat(lines[0], is("First Line"));
        assertThat(lines[1], is("Second Line no LF"));
    }

}
