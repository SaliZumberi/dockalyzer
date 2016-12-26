package dockalyzer.process.extract;

import dockalyzer.app.App;
import dockalyzer.models.SQL.Diff;
import dockalyzer.models.SQL.Dockerfile;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.services.HibernateService;
import dockalyzer.tools.dockerparser.DockerParser;
import dockalyzer.tools.githubminer.DiffProcessor;
import dockalyzer.tools.githubminer.GitCloner;
import dockalyzer.tools.githubminer.GitHistoryFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by salizumberi-laptop on 14.12.2016.
 */
public class DockerfileExtractor  implements Runnable{
    String githubId;
    String repo_name;
    String dockerPath;
    String gitURL;
    String firstCommitDate;
    int network;
    int openIssues;
    String ownerType;
    int forks;
    int watchers;
    int stargazers;
    int subscribers;
    int size;

    public DockerfileExtractor(String githubId, String repo_name, String dockerPath,
                               String gitURL, String firstCommitDate, int network, int openIssues, String ownerType,
                               int forks, int watchers, int stargazers, int subscribers, int size) {
        this.githubId = githubId;
        this.repo_name =repo_name;
        this.dockerPath = dockerPath;
        this.gitURL = gitURL;
        this.firstCommitDate = firstCommitDate;
        this.network = network;
        this.openIssues = openIssues;
        this.ownerType= ownerType;
        this.forks = forks;
        this.watchers = watchers;
        this.stargazers = stargazers;
        this.subscribers = subscribers;
        this.size = size;

        Thread thread = new Thread(this);
        thread.start();
    }

    public boolean running;
    @Override
    public void run() {
        this.running = true;
        try {
            //this.getProcessOutput(this.remotePath, this.localPath);
            saveDockerObject( this.githubId,
            this.repo_name,
            this.dockerPath,
            this.gitURL ,
            this.firstCommitDate,
            this.network ,
            this.openIssues ,
            this.ownerType,
            this.forks ,
            this.watchers ,
            this.stargazers ,
            this.subscribers ,
            this.size);

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.running = false;
    }

    public static void saveDockerObject(String githubId, String repo_name, String dockerPath,
                                        String gitURL, String firstCommitDate, int network, int openIssues, String ownerType,
                                        int forks, int watchers, int stargazers, int subscribers, int size) {
        try {
            System.out.println("4##################################################################################################");
            System.out.println("1. Start with: " + gitURL + " + "+  repo_name+ " + " +dockerPath);

            String repoFolderName = App.LOCAL_REPO_FOLDER + String.valueOf(githubId);
            String repoFolderNameDotGit = repoFolderName + "\\.git";

            GitCloner gitCloner = new GitCloner();
            System.out.println("2. Clone Repo");
            gitCloner.cloneRepository(gitURL, repoFolderName);

            System.out.println("3. Get History of Repo");

            GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
            Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);

            System.out.println("4. Load History of Repo");

            Git git = new Git(repository);

            RevWalk reVwalk = new RevWalk(repository);
            List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit, dockerPath);

            //  DiffProcessor.getDiffbetweenTwoFiles(null,null,historyOfFile.get(1), historyOfFile.get(0), dockerPath, repository, git,repoFolderName);

            if (repo_name.equals("netmackan/socialhost")){
                System.out.println("5. FUCK a new Dockerfile Object");
            }

            System.out.println("5. Create a new Dockerfile Object");
            Dockerfile dockerfile = new Dockerfile();
            dockerfile.setLocalRepoPath(repoFolderName);
            dockerfile.setRepo_id(Long.parseLong(githubId));
            dockerfile.setDockerPath(dockerPath);
            dockerfile.setFirstCommitDate(Long.valueOf(firstCommitDate));
            dockerfile.setForks(forks);
            dockerfile.setSize(size);
            dockerfile.setNetworkCount(network);
            dockerfile.setRepoPath(repo_name);
            dockerfile.setStargazers(stargazers);
            dockerfile.setOpneIssues(openIssues);
            dockerfile.setOwnerType(ownerType);
            dockerfile.setWatchers(watchers);
            dockerfile.setSubscribers(subscribers);
            dockerfile.setSubscribers(subscribers);
            dockerfile.setFirstDockerCommitDate(DateExtractor.getUnixDateFromCommit(historyOfFile.get(historyOfFile.size() - 1)));
            dockerfile.setCommits(historyOfFile.size());
            dockerfile.setViolatedRules(dockerfile.getViolatedRules());

            int index = 0;
            System.out.println("6. Create Snapshots for the Dockerfile");
            for (RevCommit commit : historyOfFile) {
                Snapshot snapshot = getDockerfileFromCommit(commit, repository, dockerPath, repoFolderName);
                if (index == 0) {
                    dockerfile.addSnapshots(snapshot,
                            true,
                            index,
                            Long.parseLong(githubId),
                            commit,
                            repository,git);
                } else {
                    dockerfile.addSnapshots(snapshot,
                            false,
                            index,
                            Long.parseLong(githubId),
                            commit,
                            repository, git);
                }
                index++;
            }

            System.out.println("7. Set Dates for the Snapshots");

            for (int i = 0; i < dockerfile.getDockerfileSnapshots().size(); i++) {
                Snapshot snapshot = dockerfile.getDockerfileSnapshots().get(i);
                if (i == 0) {
                    long nowUnx = new Date().getTime() / 1000;
                    dockerfile.getDockerfileSnapshots().get(i).setFromDate(snapshot.getCommitDate());
                    dockerfile.getDockerfileSnapshots().get(i).setToDate(nowUnx);
                } else {
                    Snapshot olderSnapshot = dockerfile.getDockerfileSnapshots().get(i - 1);
                    dockerfile.getDockerfileSnapshots().get(i).setFromDate(snapshot.getCommitDate());
                    dockerfile.getDockerfileSnapshots().get(i).setToDate(olderSnapshot.getCommitDate());
                }
            }

            System.out.println("8. Map Diffs with Snapshots");
            if (dockerfile.getDockerfileSnapshots().size() > 0) {
                for (int i = 0; i < dockerfile.getDockerfileSnapshots().size(); i++) {
                    Diff prevDiff;
                    Diff nextDiff;
                    boolean foundFlag = false;

                    while (!foundFlag) {
                        if (dockerfile.getDockerfileSnapshots().size() == 1) {
                            prevDiff = DiffProcessor.getDiff(null, dockerfile.getDockerfileSnapshots().get(0));
                            nextDiff = null;
                            dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                            foundFlag = true;
                        } else {
                            if (i == 0 && dockerfile.getDockerfileSnapshots().size()> (i+1)) {
                                prevDiff = DiffProcessor.getDiff(null, dockerfile.getDockerfileSnapshots().get(i));
                                nextDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i), dockerfile.getDockerfileSnapshots().get(i+1));
                                dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                foundFlag = true;
                            } else {
                                if(i>0 && dockerfile.getDockerfileSnapshots().size()> (i+1)){
                                    prevDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i-1), dockerfile.getDockerfileSnapshots().get(i));
                                    nextDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i), dockerfile.getDockerfileSnapshots().get(i+1));
                                    dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                    foundFlag = true;
                                }else{
                                    if(i>0){
                                        prevDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i-1), dockerfile.getDockerfileSnapshots().get(i));
                                        nextDiff = null;
                                        dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                        foundFlag = true;
                                    }
                                }
                            }
                        }
                    }

                }
            }


            System.out.println("## HIBERNATESERVICE: INSERT Dockerfile Object");
            HibernateService.createDockerfile(dockerfile);

            // JsonElement json = JsonPrinter.getJsonObject(dockerFile, String.valueOf(dockerFile.repo_id));
            //  System.out.println(JsonPrinter.getJsonString(json));
            // getDockerfileFromCommit(historyOfFile.get(1),repository,dockerPath,repoFolderName);
            System.out.println("1##################################################################################################");
            //   showDiffFilesOfHistory(historyOfFile, repository,git);
            System.out.println("2##################################################################################################");
            // DiffProcessor.showDiffbetweenTwoFiles(historyOfFile.get(1), historyOfFile.get(0), dockerPath, repository, git);
            //    CommitProcessor.getChangedFilesWithinCommit(historyOfFile.get(0), repository);


            repository.close();
            git.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Snapshot getDockerFile(String localPath, String dockerPath, String filename) throws IOException {
        DockerParser dockerParser = new DockerParser(localPath, dockerPath);
        Snapshot dockerfileSnapshot;
        if (filename == null) {
            dockerfileSnapshot = dockerParser.getDockerfileObject();
        } else {
            dockerfileSnapshot = dockerParser.getDockerfileObject(filename);
        }

        // System.out.println(dockerParser.toString());
        return dockerfileSnapshot;
    }

    public static Snapshot getDockerfileFromCommit(RevCommit revCommit, Repository repository, String dockerPath, String localPath) throws IOException {
        // a RevWalk allows to walk over commits based on some filtering that is defined
        System.out.println("6.1 Find File of Commit (getDockerfileFromCommit)");
        Snapshot snapshot = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(revCommit.getId());
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            File tempDockferile = null;
            String tempname = null;
            String filename = null;
            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(dockerPath));
                if (!treeWalk.next()) {

                    // return snapshot = getDockerFile(localPath, dockerPath, tempname);
                    throw new IllegalStateException("Did not find expected file 'Dockerfile'");
                }
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                Random ran = new Random();
                filename = "tempDock";
                tempname = localPath + "/" + filename;
                tempDockferile = new File(tempname);
                FileOutputStream fop = new FileOutputStream(tempDockferile);
                loader.copyTo(fop);
                fop.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            revWalk.dispose();
            System.out.println("6.2 Create Snapshot from File (getDockerfileFromCommit)");
            snapshot = getDockerFile(localPath, dockerPath, tempname);
            if (tempDockferile.exists()) {
                tempDockferile.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return snapshot;
    }

}
