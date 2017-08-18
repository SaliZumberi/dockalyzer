package models.commands;

import lombok.Getter;
import lombok.Setter;
import services.*;
import models.*;
import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Getter
@Setter
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

    @Column(name = "key_value", length = 1024)
    public String keyValue;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Env(Snapshot snapshot, String key, String value) {
        super();
        this.snapshot =snapshot;
        this.key=key;
        this.value=value;

        this.keyValue = key +":" + value;

        if (this.keyValue.length() > 240){
            this.keyValue = this.keyValue.substring(0, 240) + "...";
        }


    }

    public Env() {
    }
}
