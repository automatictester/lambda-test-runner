package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CloneCommandFactory {

    private static final String SSH_KEY_LOCAL = System.getenv("SSH_KEY_LOCAL");

    private CloneCommandFactory() {
    }

    public static CloneCommand getInstance(String repoUri, String branch, File dir) {
        if (repoUri.startsWith("git")) {
            return getSshCloneCommand(repoUri, branch, dir);
        } else if (repoUri.startsWith("http")) {
            return getHttpsCloneCommand(repoUri, branch, dir);
        } else {
            throw new RuntimeException("Unknown Git repo protocol: " + repoUri);
        }
    }

    private static CloneCommand getHttpsCloneCommand(String repoUri, String branch, File dir) {
        return Git.cloneRepository()
                .setURI(repoUri)
                .setBranch(branch)
                .setDirectory(dir);
    }

    private static CloneCommand getSshCloneCommand(String repoUri, String branch, File dir) {
        return Git.cloneRepository()
                .setURI(repoUri)
                .setBranch(branch)
                .setDirectory(dir)
                .setTransportConfigCallback(transport -> {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(getSshSessionFactory());
                });
    }

    private static SshSessionFactory getSshSessionFactory() {
        getPrivateSshKey();

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJsch = super.createDefaultJSch(fs);
                defaultJsch.addIdentity(SSH_KEY_LOCAL);
                return defaultJsch;
            }

            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setUserInfo(getUserInfo());
            }
        };
        return sshSessionFactory;
    }

    private static void getPrivateSshKey() {
        String content = getSshKeyFromS3();
        storeSshKeyToDisk(content);
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

    private static String getSshKeyFromS3() {
        AmazonS3 amazonS3 = AmazonS3Factory.getRealInstance();
        String bucket = System.getenv("SSH_KEY_BUCKET");
        String key = System.getenv("SSH_KEY_KEY");
        return amazonS3.getObjectAsString(bucket, key);
    }

    private static void storeSshKeyToDisk(String content) {
        try {
            Files.write(Paths.get(SSH_KEY_LOCAL), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
