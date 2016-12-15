package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_env")
public class Env extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_ENV", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_ENV", sequenceName = "SEC_ENV",allocationSize=1)
    @Column(name="ENV_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "key")
    public String key;

    @Column(name = "value")
    public String value;

    @Column(name = "key_value")
    public String keyValue;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Env(Snapshot snapshot, String key, String value) {
        super();
        this.snapshot =snapshot;
        this.key=key;
        this.value=value;

        this.keyValue = key +":" + value;

    }

    public Env() {
    }
}
