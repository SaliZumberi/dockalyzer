package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_healthcheck")
public class Healthcheck extends Instruction{

    @Id
    @Column(name = "SNAP_ID", unique = true, nullable = false)
    @GeneratedValue(generator = "healthcheck_gen")
    @GenericGenerator(name = "healthcheck_gen", strategy = "foreign",
            parameters = @org.hibernate.annotations.Parameter(name = "property", value = "snapshot"))
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    Snapshot snapshot;

    @Column(name = "options_params", nullable = false)
    String optionsBeforeInstructions;

    @Column(name = "current", nullable = false)
    public boolean current;

    @Column(name = "instruction", nullable = false)
    public String instruction;

    @Column(name = "instruction_params", nullable = false)
    public String allParams;

    public Healthcheck(Snapshot snapshot, String instruction, String allParams) {
        super();
        this.snapshot = snapshot;
        this.instruction =instruction;
        this.allParams=allParams;
    }

    public Healthcheck(String optionsBeforeInstructions) {
        super();
        this.optionsBeforeInstructions =optionsBeforeInstructions;
    }

    public Healthcheck(Snapshot snapshot, String instruction, String optionsBeforeInstructions, String allParams) {
        super();
        this.snapshot = snapshot;
        this.instruction =instruction;
        this.optionsBeforeInstructions =optionsBeforeInstructions;
        this.allParams=allParams;
    }

    public Healthcheck() {
    }
}
