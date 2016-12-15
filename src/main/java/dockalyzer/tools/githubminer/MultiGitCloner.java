package dockalyzer.tools.githubminer;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by salizumberi-laptop on 14.11.2016.
 */
public class MultiGitCloner {
    public List<GitCloner> gitCloners;
    public ExecutorService executor;

    public MultiGitCloner(){
        gitCloners = new ArrayList<>();
    }

    public void cloneNRepository(List<String> paths) throws Exception {
        gitCloners = new ArrayList<>();
        for (String n : paths) {
            String[] split = n.split(",");
            if (split.length == 2) {
               gitCloners.add(new GitCloner(split[0], split[1]));
            }
        }
    }


    public boolean areClonesfinished() {
        List<GitCloner> stillRunningThreads = new ArrayList<>();
        for (GitCloner gitCloner : gitCloners) {
            if (gitCloner.running) {
                stillRunningThreads.add(gitCloner);
            }
        }

        if (stillRunningThreads.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String remainingClones() {
        List<GitCloner> stillRunningThreads = new ArrayList<>();
        for (GitCloner gitCloner : gitCloners) {
            if (gitCloner.running) {
                stillRunningThreads.add(gitCloner);
            }
        }
        String remaining = "rmdir ";
        for (GitCloner gitCloner : gitCloners) {
            if (gitCloner.running) {
                remaining += gitCloner.localPath + ", ";
            }
        }
        return "Remaining: " + stillRunningThreads.size() + remaining;
    }

    public int runningThreads() {
        List<GitCloner> stillRunningThreads = new ArrayList<>();
        for (GitCloner gitCloner : gitCloners) {
            if (gitCloner.running) {
                stillRunningThreads.add(gitCloner);
            }
        }
        return stillRunningThreads.size();
    }

    public void cleanList() {
        gitCloners = null;
    }

    public void runThread(String remotePath, String localPath) {
        gitCloners.add(new GitCloner(remotePath, localPath));
    }
}
