package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_expose")
public class Expose extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_EXPOSE", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_EXPOSE", sequenceName = "SEC_EXPOSE",allocationSize=1)
    @Column(name="EXPOSE_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "port")
    public long port;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Expose(Snapshot snapshot, String port) {
        super();
        this.snapshot = snapshot;
        this.port = Integer.parseInt(port);
}

    public Expose() {
    }
}
