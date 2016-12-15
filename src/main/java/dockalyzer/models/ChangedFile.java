package dockalyzer.models;

import dockalyzer.models.SQL.Snapshot;
import org.eclipse.jgit.diff.DiffEntry;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 21.11.2016.
 */
@Entity
@Table(name = "changed_files")
public class ChangedFile {

    @Id
    @GeneratedValue(generator = "SEC_GEN_CHANGEDFILE", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_CHANGEDFILE", sequenceName = "SEC_CHANGEDFILE",allocationSize=1)
    @Column(name="CHANGEDFILE_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "path")
    public String filePath;

    @Column(name = "range_size")
    public int rangeSize;

    @Column(name = "range_index")
    public int rangeIndex;

    @Transient
    public String dockerPath;

    @Column(name = "file_name")
    public String fileName;

    @Column(name = "full_file_name")
    public String fullFileName;

    @Column(name = "file_type")
    public String fileType;

    @Column(name = "repoName")
    public String repoName;

    @Column(name = "commit")
    public String commitId;

    @Column(name = "changeType")
    public String changeType;

    @Column(name = "mode")
    public int mode;

    @Column(name = "deletions")
    public int deletions;

    @Column(name = "insertions")
    public int insertions;


    public ChangedFile(Snapshot snap, String path, String fullFileName, String fileName, String filePath, String fileType,
                       int mode, DiffEntry.ChangeType changeType, int deletions, int insertions, int rangeIndex, int rangeSize,
                       String commitId, String repo_name) {
        this.snapshot = snap;
        this.dockerPath = dockerPath;
        this.fullFileName = fullFileName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.mode = mode;
        this.changeType = changeType.toString();
        this.deletions = deletions;
        this.insertions = insertions;
        this.rangeIndex = rangeIndex;
        this.rangeSize  =rangeSize;
        this.commitId = commitId;
        this.repoName = repo_name;

    }
}
