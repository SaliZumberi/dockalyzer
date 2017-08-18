package Main;

import lombok.Getter;
import lombok.Setter;
import models.*;
import services.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import java.text.ParseException;
import java.util.*;

@Getter
@Setter
public class App {
    public final static String LOCAL_REPO_FOLDER = "github.com/";
    public final static String GITAPI = "https://api.github.com/";
    public final static String REPOS = "repos/";
    public static String USERNAME = "";
    public static String PASSWORD = "";
    private String repoFolderName;
    private String repoFolderNameDotGit;
    private Project project;
    private Repository repository;
    private Git git;
    private String relativePathToDockerfile;

    public static void main(String[] args) throws Exception {
        App app;
        if (args.length > 1) {
            app = new App(args[0]);

        } else {
            app = new App(args[0], args[1], args[2]);
        }


        List<File> rawDockerfiles = app.getDockerFilesFromGitRepository(app.repoFolderName);

        List<Dockerfile> dockerfiles = app.getDockerfileObjectsFromRawDockerfiles(rawDockerfiles);

        for (Dockerfile dockerfile : dockerfiles) {
            dockerfile.setProject(app.project);
        }
        app.project.setDockerfiles(dockerfiles);

        HibernateService.save(app.project);
        app.repository.close();
        app.git.close();
    }

    public List<Dockerfile> getDockerfileObjectsFromRawDockerfiles(List<File> rawDockerfiles) throws Exception {
        List<Dockerfile> dockerfiles = new ArrayList<>();

        for (File rawDockerfile : rawDockerfiles) {
            Dockerfile dockerfile = getDockerfileObjectFromRawDockerfile(rawDockerfile);
            dockerfiles.add(dockerfile);
        }
        return dockerfiles;
    }

    public Dockerfile getDockerfileObjectFromRawDockerfile(File rawDockerfile) throws IOException, GitAPIException, ParseException, InterruptedException {
        this.relativePathToDockerfile = getRelativePathToDockerFile(rawDockerfile.getPath());
        List<RevCommit> historyOfFile = getRevCommitsOfRawDockerfile(repoFolderNameDotGit, relativePathToDockerfile);

        Dockerfile dockerfile = new Dockerfile();
        dockerfile.setProject(project);
        dockerfile.setRepo_id(project.getRepo_id());
        dockerfile.setDockerPath(rawDockerfile.getPath());
        dockerfile.setFirstDockerCommitDate(DateExtractor.getUnixDateFromCommit(historyOfFile.get(historyOfFile.size() - 1)));
        dockerfile.setCommits(historyOfFile.size());
        //TODO: please activate this
        // dockerfile.setViolatedRules(dockerfile.getViolatedRules());

        Collections.reverse(historyOfFile);

        List<Snapshot> snapshots = getSnapshotsFromRevCommits(historyOfFile);
        for (Snapshot snapshot : snapshots) {
            snapshot.setDockerfile(dockerfile);
        }

        setDiffsBetweenSnapshots(snapshots);

        dockerfile.setDockerfileSnapshots(snapshots);


        return dockerfile;
    }

    public void setDiffsBetweenSnapshots(List<Snapshot> snapshots) {
        for (int i = 0; i < snapshots.size(); i++) {
            Diff prevDiff = null;
            if (i == 0) {
                prevDiff = DiffProcessor.getDiff(null, snapshots.get(0));
            } else {
                prevDiff = DiffProcessor.getDiff(snapshots.get(i - 1), snapshots.get(i));
            }
            snapshots.get(i).setOldDiff(prevDiff);
        }

        for (int i = 0; i < snapshots.size() - 1; i++) {
            Diff nextDiff = null;
            if (snapshots.size() == 1) {
            } else {
                nextDiff = snapshots.get(i + 1).getDiffs().get(0);
                snapshots.get(i).setNewDiff(nextDiff);
            }
        }
    }

    public List<RevCommit> getRevCommitsOfRawDockerfile(String repoFolderNameDotGit, String relativePathToDockerfile) throws IOException, GitAPIException {
        GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
        Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);
        Git git = new Git(repository);
        return gitHistoryFilter.getDockerfileCommitHistory(repoFolderNameDotGit, relativePathToDockerfile);
    }


    public List<Snapshot> getSnapshotsFromRevCommits(List<RevCommit> historyOfFile) throws IOException, GitAPIException {
        List<Snapshot> snapshots = new ArrayList<>();
        int index = 0;

        for (RevCommit commit : historyOfFile) {
            Snapshot snapshot = getFullSnapshotFromRevCommit(commit, index == 0, index, repository, git);
            snapshots.add(snapshot);
            index++;
        }

        for (int i = 0; i < snapshots.size(); i++) {
            Snapshot snapshot = snapshots.get(i);
            if (i == 0) {
                long nowUnx = new Date().getTime() / 1000;
                snapshots.get(i).setFromDate(snapshot.getCommitDate());
                snapshots.get(i).setToDate(nowUnx);
            } else {
                Snapshot olderSnapshot = snapshots.get(i - 1);
                snapshots.get(i).setFromDate(snapshot.getCommitDate());
                snapshots.get(i).setToDate(olderSnapshot.getCommitDate());
            }
        }

        return snapshots;
    }

    public Snapshot getFullSnapshotFromRevCommit(RevCommit commit, boolean isCurrent, int index, Repository repository, Git git) throws IOException, GitAPIException {
        File rawSnapshot = getDockerfileFromCommit(commit, repository, relativePathToDockerfile, repoFolderName);
        boolean exist = rawSnapshot.exists();
        Snapshot snapshot = getSnapshotObjectFromRawSnapshot(rawSnapshot);

        List<ChangedFile> changedFiles = getChangedFilesForCommit(commit);
        for (ChangedFile changedFile : changedFiles) {
            if (changedFile.dockerPath != null && changedFile.dockerPath.equals(relativePathToDockerfile)) {
                snapshot.setChangeType(changedFile.changeType);
                snapshot.setIns(changedFile.insertions);
                snapshot.setDel(changedFile.deletions);
            }
        }

        snapshot.setFilesChangedWithinCommit(changedFiles);

        if (isCurrent) {
            snapshot.setCurrentDockerfile(true);
            if (snapshot.from != null) {
                snapshot.from.current = true;
            }
            if (snapshot.maintainer != null) {
                snapshot.maintainer.current = true;
            }
            if (snapshot.cmd != null) {
                snapshot.cmd.current = true;
            }
            if (snapshot.entryPoint != null) {
                snapshot.entryPoint.current = true;
            }
            if (snapshot.stopSignals != null) {
                snapshot.stopSignals.current = true;
            }
            if (snapshot.healthCheck != null) {
                snapshot.healthCheck.current = true;
            }
            if (snapshot.runs != null) {
                snapshot.runs.forEach(x -> x.current = true);
            }
            if (snapshot.labels != null) {
                snapshot.labels.forEach(x -> x.current = true);
            }
            if (snapshot.envs != null) {
                snapshot.envs.forEach(x -> x.current = true);
            }
            if (snapshot.exposes != null) {
                snapshot.exposes.forEach(x -> x.current = true);
            }
            if (snapshot.adds != null) {
                snapshot.adds.forEach(x -> x.current = true);
            }
            if (snapshot.copies != null) {
                snapshot.copies.forEach(x -> x.current = true);
            }
            if (snapshot.volumes != null) {
                snapshot.volumes.forEach(x -> x.current = true);
            }
            if (snapshot.users != null) {
                snapshot.users.forEach(x -> x.current = true);
            }
            if (snapshot.workDirs != null) {
                snapshot.workDirs.forEach(x -> x.current = true);
            }
            if (snapshot.args != null) {
                snapshot.args.forEach(x -> x.current = true);
            }
            if (snapshot.onBuilds != null) {
                snapshot.onBuilds.forEach(x -> x.current = true);
            }
            if (snapshot.comments != null) {
                snapshot.comments.forEach(x -> x.current = true);
            }
        } else {
            snapshot.setCurrentDockerfile(false);
        }
        snapshot.setIndex(index);

        snapshot.setInstructions(snapshot.countInstructions());
        snapshot.extractCommit();

        long date = 0;
        try {
            date = DateExtractor.getUnixDateFromCommit(commit);
        } catch (ParseException e) {
        }

        snapshot.setCommitDate(date);
        snapshot.setRepoId(project.getRepo_id());

        return snapshot;
    }

    public List<ChangedFile> getChangedFilesForCommit(RevCommit commit) throws IOException, GitAPIException {
        List<ChangedFile> changedFiles = new ArrayList<>();
        int rangeSize = 6;
        int lowerIntervall = (-1) * (rangeSize / 2);
        int upperIntervall = (rangeSize / 2) + 1;

        for (int i = lowerIntervall; i < upperIntervall; i++) {
            CommitProcessor commitProcessor = new CommitProcessor();
            List<ChangedFile> tempChangedFiles = commitProcessor.getChangedFilesWithinCommit(commit, repository, rangeSize, i, git, project.getRepositoryPath());
            if (i < 0 && tempChangedFiles.size() > 0) {
                String hek = "";
            }
            tempChangedFiles.forEach(x -> changedFiles.add(x));
        }

        return changedFiles;
    }

    private Snapshot getSnapshotObjectFromRawSnapshot(File rawSnapshot) throws IOException {
        DockerParser dockerParser = new DockerParser(repoFolderName, relativePathToDockerfile);
        Snapshot dockerfileSnapshot = dockerParser.getParsedDockerfileObject(rawSnapshot);

        if (rawSnapshot.exists()) {
            rawSnapshot.delete();
        }
        return dockerfileSnapshot;
    }


    public List<File> getDockerFilesFromGitRepository(String repoPath) {
        File root = new File(repoPath);
        String fileName = "Dockerfile";
        List<File> rawDockerfiles = new ArrayList<>();
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, null, recursive);

            for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName)) {
                    rawDockerfiles.add(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return rawDockerfiles;
        }
    }

    public String getRelativePathToDockerFile(String absolutePath) {
        return absolutePath.replace("\\", "/").replace(repoFolderName, "").replaceFirst("/", "");
    }

    public File getDockerfileFromCommit(RevCommit revCommit, Repository repository, String dockerPath, String localPath) throws IOException {
        // a RevWalk allows to walk over commits based on some filtering that is defined
        //  System.out.println("6.1 Find File of Commit (getDockerfileFromCommit)");
        Snapshot snapshot = null;
        File tempDockferile = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(revCommit.getId());
            // and using commit's tree find the path
            RevTree tree = commit.getTree();

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
                // fop.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            revWalk.dispose();
            //  System.out.println("6.2 Create Snapshot from File (getDockerfileFromCommit)");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return tempDockferile;

        }
    }

    public void init(String gitUrl) throws Exception {
        this.setProject(new Project(gitUrl));

        this.setRepoFolderName(App.LOCAL_REPO_FOLDER + String.valueOf(this.getProject().getRepositoryPath()));
        this.setRepoFolderNameDotGit(this.getRepoFolderName() + "/.git");

        GitCloner gitCloner = new GitCloner();
        gitCloner.cloneRepository(this.getProject().getGitUrl(), this.getRepoFolderName());
        this.setRepository(gitCloner.getRepository(this.getRepoFolderNameDotGit()));
        this.setGit(new Git(this.getRepository()));
    }

    public App(String gitUrl, String gitHubUserName, String gitHubPassword) throws Exception {
        init(gitUrl);
        USERNAME = (gitHubUserName);
        PASSWORD = (gitHubPassword);
    }


    public App(String gitUrl) throws Exception {
        init(gitUrl);
    }
}


