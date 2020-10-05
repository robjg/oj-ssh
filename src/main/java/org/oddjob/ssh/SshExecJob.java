package org.oddjob.ssh;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.IoUtils;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.io.DevNullType;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LoggingOutputStream;
import org.oddjob.logging.cache.LogArchiveImpl;
import org.oddjob.util.OddjobWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @oddjob.description Runs a remote SSH Exec command.
 *
 */
public class SshExecJob extends SshClientBase implements ConsoleOwner {

    private static final Logger logger = LoggerFactory.getLogger(SshExecJob.class);

    /**
     * Timeout for the channel open. Should this be parameterised?
     */
    private static final long OPEN_TIMEOUT = 10_0000L;

    private static final AtomicInteger consoleCount = new AtomicInteger();

    private static String uniqueConsoleId() {
        return ("SSH_EXEC_CONSOLE" + consoleCount.getAndIncrement());
    }

    private transient LogArchiveImpl consoleArchive;

    /**
     * @oddjob.property
     * @oddjob.description The command to run.
     * @oddjob.required Yes.
     */
    private String command;

    /**
     * @oddjob.property
     * @oddjob.description Merge stderr into stdout.
     * @oddjob.required No.
     */
    private boolean redirectStderr;

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

    public SshExecJob() {
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
    Integer withSession(ClientSession session) {

        try {
            OutputStream stdout = this.stdout;

            OutputStream stderr;
            if (redirectStderr) {
                stderr = stdout;
            } else {
                stderr = this.stderr;
            }

            ChannelExec channel = session.createExecChannel(command);

            InputStream inStream = Optional.ofNullable(this.stdin)
                    .orElseGet(() -> DevNullType.IN);

            try (OutputStream outStream = new LoggingOutputStream(stdout,
                    LogLevel.INFO, consoleArchive);

                 OutputStream errStream = new LoggingOutputStream(stderr,
                         LogLevel.ERROR, consoleArchive)
            ) {
                channel.setOut(outStream);
                channel.setErr(errStream);

                try {
                    logger.info("Opening channel with command " + command);
                    channel.open().verify(OPEN_TIMEOUT);
                } catch (IOException e) {
                    throw new OddjobWrapperException("Failed opening channel", e);
                }

                logger.info("Channel open");

                try (OutputStream channelIn = channel.getInvertedIn()) {
                    IoUtils.copy(inStream, channelIn);
                }

                // Wait (forever) for the channel to close - signalling shell exited
                Collection<ClientChannelEvent> events =
                        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);

                // not sure if this can be null be just to be safe.
                Integer exitStatus = channel.getExitStatus();

                if (logger.isDebugEnabled()) {
                    logger.debug("Channel closed with events {}, exit state", events, exitStatus);
                }

                channel.close(false);

                return exitStatus;
            }
        } catch (IOException e) {
            throw new OddjobWrapperException(e);
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
