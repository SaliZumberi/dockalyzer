package dockalyzer.process.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by salizumberi-laptop on 14.12.2016.
 */
public class MultiDockerfileExtractor {

    public List<DockerfileExtractor> dockerfileExtractors;
    public ExecutorService executor;

    public MultiDockerfileExtractor() {
        dockerfileExtractors = new ArrayList<>();
    }

    public int runningThreads() {
        List<DockerfileExtractor> stillRunningThreads = new ArrayList<>();
        for (DockerfileExtractor extractor : dockerfileExtractors) {
            if (extractor.running) {
                stillRunningThreads.add(extractor);
            }
        }
        return stillRunningThreads.size();
    }

    public void cleanList() {
        dockerfileExtractors = null;
    }

    public void runThread(String githubId, String repo_name, String dockerPath,
                          String gitURL, String firstCommitDate, int network, int openIssues, String ownerType,
                          int forks, int watchers, int stargazers, int subscribers, int size) {
        dockerfileExtractors.add(new DockerfileExtractor(githubId, repo_name, dockerPath,
                gitURL, firstCommitDate, network, openIssues, ownerType,
                forks, watchers, stargazers, subscribers, size));
    }


    public boolean areClonesfinished() {
        List<DockerfileExtractor> stillRunningThreads = new ArrayList<>();
        for (DockerfileExtractor extractor : dockerfileExtractors) {
            if (extractor.running) {
                stillRunningThreads.add(extractor);
            }
        }

        if (stillRunningThreads.size() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
