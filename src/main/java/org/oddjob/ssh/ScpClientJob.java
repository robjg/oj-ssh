package org.oddjob.ssh;

import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.scp.ScpClientCreator;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.common.session.Session;
import org.oddjob.util.OddjobWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class ScpClientJob extends SshClientBase {

    private static final Logger logger = LoggerFactory.getLogger(ScpClientJob.class);

    private String remote;

    private String from;

    private String to;


    protected static final ScpTransferEventListener DEBUG_LISTENER = new ScpTransferEventListener() {
        @Override
        public void startFolderEvent(
                Session s, FileOperation op, Path file, Set<PosixFilePermission> perms) {
            logEvent("starFolderEvent", s, op, file, false, -1L, perms, null);
        }

        @Override
        public void startFileEvent(
                Session s, FileOperation op, Path file, long length, Set<PosixFilePermission> perms) {
            logEvent("startFileEvent", s, op, file, true, length, perms, null);
        }

        @Override
        public void endFolderEvent(
                Session s, FileOperation op, Path file, Set<PosixFilePermission> perms, Throwable thrown) {
            logEvent("endFolderEvent", s, op, file, false, -1L, perms, thrown);
        }

        @Override
        public void endFileEvent(
                Session s, FileOperation op, Path file, long length, Set<PosixFilePermission> perms, Throwable thrown) {
            logEvent("endFileEvent", s, op, file, true, length, perms, thrown);
        }

        private void logEvent(
                String type, Session s, FileOperation op, Path path, boolean isFile,
                long length, Collection<PosixFilePermission> perms, Throwable t) {

            StringBuilder sb = new StringBuilder(Byte.MAX_VALUE);
            sb.append("    ").append(type)
                    .append('[').append(s).append(']')
                    .append('[').append(op).append(']')
                    .append(' ').append(isFile ? "File" : "Directory").append('=').append(path)
                    .append(' ').append("length=").append(length)
                    .append(' ').append("perms=").append(perms);
            if (t != null) {
                sb.append(' ').append("ERROR=").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
            }

            logger.debug(sb.toString());
        }
    };


    @Override
    void withSession(ClientSession session) {

        String remote = Objects.requireNonNull(this.remote, "No remote file");

        ScpClientCreator creator = ScpClientCreator.instance();
        ScpTransferEventListener listener = logger.isDebugEnabled() ? DEBUG_LISTENER : null;

        ScpClient scpClient = creator.createScpClient(session, listener);

        String from = this.from;
        if (from != null) {

            try {
                scpClient.upload(from, remote);
            } catch (IOException e) {
                throw new OddjobWrapperException(e);
            }
        }

        String to = this.to;
        if (to != null) {

            try {
                scpClient.download(remote, to);
            } catch (IOException e) {
                throw new OddjobWrapperException(e);
            }
        }

    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
