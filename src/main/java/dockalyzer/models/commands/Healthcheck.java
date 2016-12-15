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
/*    @Id
    @Column(name="REPO_ID", unique=true, nullable=false)
    private long id;*/


    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    Snapshot snapshot;

    //@OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy = "snapshot")
   // Instruction healthInstruction;

    @ElementCollection
    @CollectionTable(name="option_params", joinColumns=@JoinColumn(name="RUN_ID"))
    @Column(name="option_params")
    List<String> optionsBeforeInstructions = new ArrayList<>();

    @Column(name = "current", nullable = false)
    public boolean current;

    public Healthcheck(Snapshot snapshot,Instruction instruction) {
        super();
        this.snapshot = snapshot;
       // this.healthInstruction =instruction;
    }

    public Healthcheck(List<String> optionsBeforeInstructions) {
        super();
        this.optionsBeforeInstructions =optionsBeforeInstructions;
    }

    public Healthcheck(Snapshot snapshot, Instruction instruction, List<String> optionsBeforeInstructions) {
        super();
        this.snapshot = snapshot;
       // this.healthInstruction =instruction;
        this.optionsBeforeInstructions =optionsBeforeInstructions;
    }

    public Healthcheck() {
    }
}
