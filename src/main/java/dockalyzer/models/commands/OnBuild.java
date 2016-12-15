package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_onbuild")
public class OnBuild extends Instruction{

    @Id
    @GeneratedValue(generator = "SEC_GEN_ONBUILD", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_ONBUILD", sequenceName = "SEC_ONBUILD",allocationSize=1)
    @Column(name="ONBUILD_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    //@OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy = "snapshot")
    //Object instruction;

    @Column(name = "current", nullable = false)
    public boolean current;

    public OnBuild(Snapshot snapshot, Instruction instruction) {
        super();
        this.snapshot = snapshot;
       // this.instruction = instruction;
    }

    public OnBuild() {
    }
}
