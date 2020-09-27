package org.oddjob.ssh;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Scanner;

public class EchoCommandFactory implements CommandFactory  {

    private static final Logger logger = LoggerFactory.getLogger(EchoCommandFactory.class);

    @Override
    public Command createCommand(ChannelSession channel, String command)  {
        logger.info("Creating command: {}", command);

        return new EchoCommand();
    }
}

class EchoCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(EchoCommand.class);

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;

    private Thread t;

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

        this.t = new Thread(() -> {

            Scanner scanner = new Scanner(in);
            while (!Thread.interrupted()) {
                String next = scanner.next();

                logger.info("** Received " + next);

                if ("quit".equals(next)) {
                    this.callback.onExit(0, "Goodbye");
                    break;
                }
                else {
                    try {
                        this.out.write(next.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    @Override
    public void destroy(ChannelSession channelSession) throws Exception {
        Optional.ofNullable(t).ifPresent(Thread::interrupt);
    }
}
