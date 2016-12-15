package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_workdir")
public class WorkDir extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_WORKDIR", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_WORKDIR", sequenceName = "SEC_WORKDIR",allocationSize=1)
    @Column(name="WORKDIR_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "path")
    public String path;

    @Column(name = "current", nullable = false)
    public boolean current;

    public WorkDir(Snapshot snapshot, String path) {
        super();
        this.snapshot = snapshot;
        this.path= path;
    }

    public WorkDir() {
    }
}
