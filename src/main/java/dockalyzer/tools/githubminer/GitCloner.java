package dockalyzer.tools.githubminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jgit.api.Git;

public class GitCloner implements Runnable {

    public boolean running = false;
    public String remotePath;
    public String localPath;
    public GitCloner() {
    }

    public GitCloner(String remotePath, String localPath) {
        this.remotePath = remotePath;
        this.localPath = localPath;

       Thread thread = new Thread(this);
        thread.start();
    }

    public void cloneRepository(String remotePath, String localPath) throws Exception {
        File repositoryFile = new File(localPath);
        if (repositoryFile.exists() && repositoryFile.isDirectory()) {
        } else {
            Git git = Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath)).call();
            git.close();
        }
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    @Override
    public void run() {
        this.running = true;
        try {
            //this.getProcessOutput(this.remotePath, this.localPath);
            this.cloneRepository(this.remotePath, this.localPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.running = false;
    }

    public static String getProcessOutput(String remotePath, String localPath) throws IOException, InterruptedException{
                ProcessBuilder processBuilder = new ProcessBuilder("git",
                "clone", remotePath, localPath);


        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder processOutput = new StringBuilder();

        try (BufferedReader processOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())))
        {
            String readLine;
            while ((readLine = processOutputReader.readLine()) != null){
                processOutput.append(readLine + System.lineSeparator());
            }
            process.waitFor();
        }

        return processOutput.toString().trim();
    }
}


