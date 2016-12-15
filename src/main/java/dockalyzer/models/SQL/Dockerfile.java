package dockalyzer.models.SQL;

import com.gitblit.utils.JGitUtils;
import dockalyzer.models.*;
import dockalyzer.models.commands.Run;
import dockalyzer.process.extract.DateExtractor;
import dockalyzer.tools.dockerlinter.DockerLinter;
import dockalyzer.tools.githubminer.CommitProcessor;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.omg.CORBA.RepositoryIdHelper;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 22.11.2016.
 */
@Entity
@Table(name = "dockerfile")
public class Dockerfile {

    @Id
    @GeneratedValue(generator = "SEC_GEN_DOCK", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_DOCK", sequenceName = "SEC_DOCK", allocationSize = 1)
    @Column(name = "DOCK_ID", unique = true, nullable = false)
    private long dockId;

    @Column(name = "REPO_ID", nullable = false)
    private long repo_id;

    public void setViolatedRules(List<String> violatedRules) {
        this.violatedRules = violatedRules;
    }

    @Transient
    private String localRepoPath;

    @Column(name = "repo_path", nullable = false)
    private String repoPath;

    @Column(name = "docker_path", nullable = false)
    private String dockerPath;

    @Column(name = "created_at", nullable = false)
    private long firstCommitDate;
    @Column(name = "first_docker_commit", nullable = false)
    private long firstDockerCommitDate;
    @Column(name = "i_owner_type", nullable = false)
    private String ownerType;
    @Column(name = "i_network_count", nullable = false)
    private int networkCount;
    @Column(name = "i_open_issues", nullable = false)
    private int opneIssues;
    @Column(name = "i_forks", nullable = false)
    private int forks;
    @Column(name = "i_watchers", nullable = false)
    private int watchers;
    @Column(name = "i_stargazers", nullable = false)
    private int stargazers;
    @Column(name = "i_subscribers", nullable = false)
    private int subscribers;
    @Column(name = "i_size", nullable = false)
    private int size;

    private int commits;

    public List<Snapshot> getDockerfileSnapshots() {
        return dockerfileSnapshots;
    }

    public void setDockerfileSnapshots(List<Snapshot> dockerfileSnapshots) {
        this.dockerfileSnapshots = dockerfileSnapshots;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "dockerfile", orphanRemoval = true)
    private List<Snapshot> dockerfileSnapshots = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "violated_rules", joinColumns = @JoinColumn(name = "DOCK_ID"))
    @Column(name = "violated_rules")
    private List<String> violatedRules = new ArrayList<>();

    public long getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(long repo_id) {
        this.repo_id = repo_id;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public long getFirstDockerCommitDate() {
        return firstDockerCommitDate;
    }

    public void setFirstDockerCommitDate(long firstDockerCommitDate) {
        this.firstDockerCommitDate = firstDockerCommitDate;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    @Column(name = "i_commits", nullable = false)
    public int getCommits() {
        return dockerfileSnapshots.size();
    }

    public void setCommits(int commits) {
        this.commits = commits;
    }

    public long getDockId() {
        return dockId;
    }

    public void setDockId(long dockId) {
        this.dockId = dockId;
    }

    public String getLocalRepoPath() {
        return localRepoPath;
    }

    public void setLocalRepoPath(String localRepoPath) {
        this.localRepoPath = localRepoPath;
    }

    public String getDockerPath() {
        return dockerPath;
    }

    public void setDockerPath(String dockerPath) {
        this.dockerPath = dockerPath;
    }

    public long getFirstCommitDate() {
        return firstCommitDate;
    }

    public void setFirstCommitDate(long firstCommitDate) {
        this.firstCommitDate = firstCommitDate;
    }


    public int getNetworkCount() {
        return networkCount;
    }

    public void setNetworkCount(int networkCount) {
        this.networkCount = networkCount;
    }

    public int getOpneIssues() {
        return opneIssues;
    }

    public void setOpneIssues(int opneIssues) {
        this.opneIssues = opneIssues;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }

    public int getStargazers() {
        return stargazers;
    }

    public void setStargazers(int stargazers) {
        this.stargazers = stargazers;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


    public List<String> getViolatedRules() throws IOException, InterruptedException {
        List<String> rules = new ArrayList<>();
        File dockerfile = new File(localRepoPath + "/" + dockerPath);
        String violations = DockerLinter.getReportOfLinting(dockerfile);
        String[] lines = StringUtils.split(violations, "\r\n");
        for (String l : lines) {
            String[] parts = l.split(" ");
            rules.add(parts[1]);
        }
        this.violatedRules = rules;
        return rules;
    }

    public void addSnapshots(Snapshot snapshot, boolean isCurrentDockerfile, int index, long id, RevCommit commit, Repository repository,Git git) throws IOException, GitAPIException {
        System.out.println("6.3 Save generated Snapshot in Dockerfile (addSnapshot)");

        Snapshot snap = snapshot;
        if (snap.getDockerfile() != this) {
            snap.setDockerfile(this);
        }
        if(isCurrentDockerfile){
            snap.setCurrentDockerfile(true);
            if(snap.from != null){snap.from.current=true;}
            if(snap.maintainer != null){snap.maintainer.current=true;}
            if(snap.cmd != null){snap.cmd.current=true;}
            if(snap.entryPoint != null){snap.entryPoint.current=true;}
            if(snap.stopSignals != null){snap.stopSignals.current=true;}
            if(snap.healthCheck != null){snap.healthCheck.current=true;}

            if(snap.runs != null){snap.runs.forEach(x -> x.current = true);}
            if(snap.labels != null){snap.labels.forEach(x -> x.current = true);}
            if(snap.envs != null){snap.envs.forEach(x -> x.current = true);}
            if(snap.exposes != null){snap.exposes.forEach(x -> x.current = true);}
            if(snap.adds != null){snap.adds.forEach(x -> x.current = true);}
            if(snap.copies != null){snap.copies.forEach(x -> x.current = true);}
            if(snap.volumes != null){snap.volumes.forEach(x -> x.current = true);}
            if(snap.users != null){snap.users.forEach(x -> x.current = true);}
            if(snap.workDirs != null){snap.workDirs.forEach(x -> x.current = true);}
            if(snap.args != null){snap.args.forEach(x -> x.current = true);}
            if(snap.onBuilds != null){snap.onBuilds.forEach(x -> x.current = true);}
        }else{
            snap.setCurrentDockerfile(false);
        }
        snap.setIndex(index);

        //String stringId = id + "" + index;
       // snap.setId(Long.parseLong(stringId));

        snap.setRepoId(id);

        List<ChangedFile> changedFiles = new ArrayList<>();

        int rangeSize = 6;
        int lowerIntervall = (-1)*(rangeSize/2);
        int upperIntervall = (rangeSize/2)+1;

        for(int i = lowerIntervall; i<upperIntervall; i++){
            CommitProcessor commitProcessor = new CommitProcessor();
            List<ChangedFile> tempChangedFiles = commitProcessor.getChangedFilesWithinCommit(snap, commit, repository, rangeSize,i, git, repoPath);
            if(i < 0 && tempChangedFiles.size()>0){
                String hek = "";
            }
            tempChangedFiles.forEach(x->changedFiles.add(x));
        }

        snap.setFilesChangedWithinCommit(changedFiles);

        for(ChangedFile changedFile : changedFiles){
            if(changedFile.dockerPath != null && changedFile.dockerPath.equals(this.dockerPath)){
                snap.setChangeType(changedFile.changeType);
                snap.setIns(changedFile.insertions);
                snap.setDel(changedFile.deletions);
            }
        }
        snap.setInstructions(snap.countInstructions());
        snap.extractCommit();

        long date = 0;
        try {
            date = DateExtractor.getUnixDateFromCommit(commit);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        snap.setCommitDate(date);

        this.dockerfileSnapshots.add(snap);

    }

}
