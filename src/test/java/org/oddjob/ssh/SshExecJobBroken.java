package org.oddjob.ssh;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LoggingOutputStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.util.OddjobWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  This is the broken version. Left here to revisit one day.
 *  </p>
 *  The In and Out use Mina SSHD 'inverted' stream and threads to pump the data. An exception occurs
 *  from {@code org.apache.sshd.common.channel.ChannelPipedInputStream} - 'Pipe closed after 0 cycles'
 *  which is coming from line 120:
 *  <pre>
 *                      if (((!openState) && writerClosedState && eofSent.get()) || ((!openState) && (!writerClosedState))) {
 *                     throw new IOException("Pipe closed after " + index + " cycles");
 *                 }
 *  </pre>
 *  This might be a bug in the ssh-core lib because it's closing the stream before we've finished reading but
 *  this needs more investigation.
 */
public class SshExecJobBroken extends SshClientBase implements ConsoleOwner {

    private static final Logger logger = LoggerFactory.getLogger(SshExecJobBroken.class);

    /**
     * Timeout for the channel open. Should this be parameterised?
     */
    private static final long OPEN_TIMEOUT = 10_0000L;

    private static final AtomicInteger consoleCount = new AtomicInteger();

    private static String uniqueConsoleId() {
        return ("SSH_EXEC_CONSOLE" + consoleCount.getAndIncrement());
    }

    private transient LogArchiveImpl consoleArchive;

    private boolean redirectStderr;

    private String command;

    /**
     * @oddjob.property
     * @oddjob.description An input stream which will
     * act as stdin for the process.
     * @oddjob.required No.
     */
    private transient InputStream stdin;

    /**
     * @oddjob.property
     * @oddjob.description An output to where stdout
     * for the process will be written.
     * @oddjob.required No.
     */
    private transient OutputStream stdout;

    /**
     * @oddjob.property
     * @oddjob.description An output to where stderr
     * of the proces will be written.
     * @oddjob.required No.
     */
    private transient OutputStream stderr;

    public SshExecJobBroken() {
        completeConstruction();
    }

    /**
     * Complete construction.
     */
    private void completeConstruction() {
        consoleArchive = new LogArchiveImpl(
                uniqueConsoleId(), LogArchiver.MAX_HISTORY);
    }

    @Override
    void withSession(ClientSession session) {

        try {
            OutputStream stdout = this.stdout;

            OutputStream stderr;
            if (redirectStderr) {
                stderr = stdout;
            } else {
                stderr = this.stderr;
            }

            ChannelExec channel = session.createExecChannel(command);

            try (
                    OutputStream outStream = new LoggingOutputStream(stdout,
                            LogLevel.INFO, consoleArchive);

                    OutputStream errStream = new LoggingOutputStream(stderr,
                            LogLevel.ERROR, consoleArchive);

                    InputStream inStream = Optional.ofNullable(this.stdin)
                            .orElseGet(() -> new ByteArrayInputStream(new byte[0]));
            ) {

                try {
                    logger.info("Opening channel with command " + command);
                    channel.open().verify(OPEN_TIMEOUT);
                } catch (IOException e) {
                    throw new OddjobWrapperException("Failed opening channel", e);
                }

                logger.info("Channel open");

                Thread outThread = new Thread(new Pump("stdout", channel.getInvertedOut(),
                        outStream));

                Thread errThread = new Thread(new Pump("stderr", channel.getInvertedErr(),
                        errStream));

                outThread.start();
                errThread.start();

                try {

                    new Pump("stdin", inStream, channel.getInvertedIn())
                            .run();

                    // Wait (forever) for the channel to close - signalling shell exited
                    Collection<ClientChannelEvent> events =
                            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Channel closed with events {}", events);
                    }

                    channel.close(false);

                } finally {
                    try {
                        errThread.join();
                        outThread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (IOException e) {
            throw new OddjobWrapperException(e);
        }
    }

    static class Pump implements Runnable {

        private final String name;

        private final InputStream from;

        private final OutputStream to;

        Pump(String name, InputStream from, OutputStream to) {
            this.name = name;
            this.from = Objects.requireNonNull(from);
            this.to = Objects.requireNonNull(to);
        }

        @Override
        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.from));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.to));

            try {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    out.write(line);
                    out.write('\n');
                    out.flush();
                }
                out.close();
                in.close();

                logger.debug("Finished pumping " + name);

            } catch (IOException e) {
                logger.error("Failed pumping " + name, e);
            }
        }
    }

    public LogArchive consoleLog() {
        return consoleArchive;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isRedirectStderr() {
        return redirectStderr;
    }

    public void setRedirectStderr(boolean redirectStderr) {
        this.redirectStderr = redirectStderr;
    }

    public InputStream getStdin() {
        return stdin;
    }

    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    public OutputStream getStdout() {
        return stdout;
    }

    public void setStdout(OutputStream stdout) {
        this.stdout = stdout;
    }

    public OutputStream getStderr() {
        return stderr;
    }

    public void setStderr(OutputStream stderr) {
        this.stderr = stderr;
    }

}
