package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_copy")
public class Copy extends Instruction{

    @Id
    @GeneratedValue(generator = "SEC_GEN_COPY", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_COPY", sequenceName = "SEC_COPY",allocationSize=1)
    @Column(name="COPY_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "source")
    public String source;

    @Column(name = "destination")
    public String destination;

    @Column(name = "source_destination")
    public String sourceDestination;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Copy(Snapshot snapshot, String source, String destinatation) {
        super();
        this.source = source;
        this.snapshot = snapshot;
        this.destination=destinatation;

        this.sourceDestination = source +"->" + destination;

    }

    public Copy() {
    }
}
