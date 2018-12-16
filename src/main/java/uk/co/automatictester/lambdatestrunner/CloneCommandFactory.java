package uk.co.automatictester.lambdatestrunner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class CloneCommandFactory {

    private static final Logger log = LogManager.getLogger(CloneCommandFactory.class);
    private static final String SSH_KEY_LOCAL = System.getenv("SSH_KEY_LOCAL");

    private CloneCommandFactory() {
    }

    public static CloneCommand getInstance(String repoUri, File dir) {
        String branch = "HEAD";
        log.info("Branch not specified, using '{}'", branch);
        return getInstance(repoUri, branch, dir);
    }

    public static CloneCommand getInstance(String repoUri, String branch, File dir) {
        if (repoUri.startsWith("git")) {
            return getSshCloneCommand(repoUri, branch, dir);
        } else if (repoUri.startsWith("http")) {
            return getCloneCommand(repoUri, branch, dir);
        } else {
            throw new IllegalArgumentException("Unknown protocol: " + repoUri);
        }
    }

    private static CloneCommand getCloneCommand(String repoUri, String branch, File dir) {
        return Git.cloneRepository()
                .setURI(repoUri)
                .setBranch(branch)
                .setDirectory(dir);
    }

    private static CloneCommand getSshCloneCommand(String repoUri, String branch, File dir) {
        return getCloneCommand(repoUri, branch, dir)
                .setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(getSshSessionFactory());
                });
    }

    private static SshSessionFactory getSshSessionFactory() {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJsch = super.createDefaultJSch(fs);
                defaultJsch.addIdentity(SSH_KEY_LOCAL);
                return defaultJsch;
            }

            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setUserInfo(getUserInfo());
            }
        };
        return sshSessionFactory;
    }

    private static UserInfo getUserInfo() {
        return new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public boolean promptPassword(String message) {
                return false;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return true;
            }

            @Override
            public boolean promptYesNo(String message) {
                return false;
            }

            @Override
            public void showMessage(String message) {
            }
        };
    }
}
