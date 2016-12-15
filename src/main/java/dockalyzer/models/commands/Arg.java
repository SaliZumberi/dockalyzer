package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_arg")
public class Arg extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_ARG", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_ARG", sequenceName = "SEC_ARG",allocationSize=1)
    @Column(name="ARG_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "arg")
    public String arg;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Arg(String arg) {
        super();
        this.arg = arg;
    }

    public Arg() {
    }
}
