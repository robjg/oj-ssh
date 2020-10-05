package org.oddjob.ssh;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.oddjob.arooa.utils.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.LongConsumer;

public class SimpleCommandFactory implements CommandFactory {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCommandFactory.class);

    @Override
    public Command createCommand(ChannelSession channel, String command) {
        logger.info("Creating command: {}", command);

        return new SimpleCommand(command);
    }
}

class SimpleCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCommandFactory.class);

    private final String command;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;

    private Thread t;

    SimpleCommand(String command) {
        this.command = command;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.in = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.out = outputStream;
    }

    @Override
    public void setErrorStream(OutputStream outputStream) {
        this.err = outputStream;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.callback = exitCallback;
    }

    @Override
    public void start(ChannelSession channelSession, Environment environment) throws IOException {

        // Todo: Do we need all the flushes?


        this.t = new Thread(() -> {

            try {

                if (command.equals("cat")) {

                    long copied = IoUtils.copy(in, out, new Progress());

                    out.flush();
                    err.flush();

                    logger.debug("** copied {} ", copied);

                    callback.onExit(0, "Goodbye");

                } else if (command.startsWith("echo")) {
                    while (in.read() != -1) {}

                    out.write(command.substring(5).getBytes());
                    out.write('\n');
                    out.flush();
                    err.flush();

                    callback.onExit(0, "Goodbye");
                } else if (command.equals("hang")) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        logger.info("Hang interrupted.");
                    }
                    // don't exit.
                } else {
                    err.write(new String("No command: " + command).getBytes());
                    callback.onExit(-1, "Fail");
                }

                logger.info("Command complete.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

    @Override
    public void destroy(ChannelSession channelSession) throws Exception {
        logger.info("** Session destroyed **");
        Optional.ofNullable(t).ifPresent(Thread::interrupt);
    }

    static class Progress implements LongConsumer {

        int after = 1024;

        @Override
        public void accept(long value) {
            if (value - after > 0) {
                System.out.print("#");
                System.out.flush();
                after+=1024;
            }
        }
    }
}
