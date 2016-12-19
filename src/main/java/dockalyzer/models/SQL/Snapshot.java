package dockalyzer.models.SQL;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ImageSearchResult;
import dockalyzer.models.*;
import dockalyzer.models.commands.*;
import dockalyzer.models.commands.Label;
import dockalyzer.models.commands.Run;
import dockalyzer.services.DockerService;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by salizumberi-laptop on 18.11.2016.
 */
@Entity
@Table(name = "snapshot")
public class Snapshot {

    @Id
    @GeneratedValue(generator = "SEC_GEN_SNAP", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_SNAP", sequenceName = "SEC_SNAP", allocationSize = 1)
    @Column(name = "SNAP_ID", unique = true, nullable = false)
    private long id;

    @Column(name = "REPO_ID", updatable = false, nullable = false)
    private Long repoId;

    public Dockerfile getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(Dockerfile dockerfile) {
        this.dockerfile = dockerfile;
    }

    @ManyToOne
    @JoinColumn(name ="DOCK_ID")
    private Dockerfile dockerfile;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "snapshot", orphanRemoval = true)
    public List<ChangedFile> filesChangedWithinCommit = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Run> runs = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Label> labels = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Env> envs = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Expose> exposes = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Add> adds = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Copy> copies = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Volume> volumes = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<User> users = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<WorkDir> workDirs = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Arg> args = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<Comment> comments = new ArrayList<>();

    //TODO: repair instruction !!!
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, mappedBy = "snapshot", orphanRemoval = true)
    public List<OnBuild> onBuilds = new ArrayList<>();

    //TODO: repair instruction !!!
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "snapshot", orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public Healthcheck healthCheck;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public EntryPoint entryPoint;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public From from;


    public void setNewAndOldDiff(Diff oldDiff,Diff newDiff) {
        if(oldDiff !=null){
            this.diffs.add(oldDiff);
        }
        if(newDiff !=null){
            this.diffs.add(newDiff);
        }
    }

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="SNAP_DIFF",
            joinColumns={@JoinColumn(name="SNAP_ID")},
            inverseJoinColumns={@JoinColumn(name="DIFF_ID")})
    private Set<Diff> diffs = new HashSet<Diff>();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "snapshot", orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public Cmd cmd;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "snapshot", orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public StopSignal stopSignals;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "snapshot", orphanRemoval = true)
    @PrimaryKeyJoinColumn
    public Maintainer maintainer;

    @Column(name = "instructions", nullable = false)
    private int instructions;

    @Column(name = "commit_date", nullable = false)
    private long commitDate;

    @Column(name = "fromDate", nullable = false)
    private long fromDate;

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    @Column(name = "toDate", nullable = false)

    private long toDate;

    // TODO: assign those values
    @Column(name = "change_type")
    private String changeType;

    // TODO: assign those values
    @Column(name = "del")
    private int del;

    // TODO: assign those values
    @Column(name = "ins")
    private int ins;

    @Column(name = "imageIsAutomated")
    private Boolean imageIsAutomated;

    @Column(name = "imageIsOffical")
    private Boolean imageIsOffical;

    @Column(name = "starCount")
    private Integer starCount;

    @Column(name = "commit_index", nullable = false)
    private int commitIndex;

    @Column(name = "index", nullable = false)
    private boolean isCurrentDockerfile;

    public List<ChangedFile> getFilesChangedWithinCommit() {
        return filesChangedWithinCommit;
    }

    public void setFilesChangedWithinCommit(List<ChangedFile> filesChangedWithinCommit) {
        this.filesChangedWithinCommit = filesChangedWithinCommit;
    }

    public int getIndex() {
        return commitIndex;
    }

    public void setIndex(int index) {
        this.commitIndex = index;
    }

    public boolean isCurrentDockerfile() {
        return isCurrentDockerfile;
    }

    public void setCurrentDockerfile(boolean currentDockerfile) {
        isCurrentDockerfile = currentDockerfile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getInstructions() {
        return instructions;
    }

    public void setInstructions(int instructions) {
        this.instructions = instructions;
    }

    public long getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(long commitDate) {
        this.commitDate = commitDate;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int del) {
        this.del = del;
    }

    public int getIns() {
        return ins;
    }

    public void setIns(int ins) {
        this.ins = ins;
    }

    public boolean isImageIsAutomated() {
        return imageIsAutomated;
    }

    public void setImageIsAutomated(boolean imageIsAutomated) {
        this.imageIsAutomated = imageIsAutomated;
    }

    public boolean isImageIsOffical() {
        return imageIsOffical;
    }

    public void setImageIsOffical(boolean imageIsOffical) {
        this.imageIsOffical = imageIsOffical;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int countInstructions(){
        int counter = 1;
        if(maintainer != null){
            counter++;
        }
        if(cmd != null){
            counter++;
        }
        if(entryPoint != null){
            counter++;
        }
        if(stopSignals != null){
            counter++;
        }
        if(healthCheck != null){
            counter++;
        }
        return  runs.size() +
                labels.size() +
                envs.size()  +
                exposes.size() +
                adds .size()+
                copies.size() +
                volumes.size() +
                users.size() +
                workDirs.size() +
                args.size() +
                onBuilds.size() +counter;
    }

    public void extractCommit(){
        ImageSearchResult imageSearchResult = null;
        try {
            imageSearchResult = getImageInfos();
        }catch (Exception e){
        }
        if(imageSearchResult != null){
            this.imageIsAutomated =imageSearchResult.isAutomated();
            this.imageIsOffical =imageSearchResult.isOfficial();
            this.starCount=imageSearchResult.getStarCount();
        }else{
            this.imageIsAutomated = null;
            this.imageIsOffical = null;
            this.starCount = null;
        }

    }


    public ImageSearchResult getImageInfos(){
        ImageSearchResult imagesInfos = null;
        try {
            imagesInfos = DockerService.getImageInfos(from.imagename);
        } catch (DockerCertificateException e) {
        } catch (DockerException e) {
        } catch (InterruptedException e) {
        }
        return imagesInfos;
    }
}
