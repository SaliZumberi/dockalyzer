package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_label")
public class Label extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_LABEL", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_LABEL", sequenceName = "SEC_LABEL",allocationSize=1)
    @Column(name="LABEL_ID", unique=true, nullable=false)
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

    public Label(Snapshot snapshot, String key, String value) {
        super();
        this.snapshot = snapshot;
        this.key= key;
        this.value=value;
        this.snapshot = snapshot;

        this.keyValue = key +":" + value;
    }
}
