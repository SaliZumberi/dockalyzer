package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Table(name = "df_entrypoint")
public class EntryPoint extends Instruction{

    @Id
    @Column(name = "SNAP_ID", unique = true, nullable = false)
    @GeneratedValue(generator = "entrypoint_gen")
    @GenericGenerator(name = "entrypoint_gen", strategy = "foreign",
            parameters = @org.hibernate.annotations.Parameter(name = "property", value = "snapshot"))
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    Snapshot snapshot;

    @Column(name = "arg")
    public String executable;

    @ElementCollection
    @CollectionTable(name="entrypoints_params", joinColumns=@JoinColumn(name="ENTRYPOINT_ID"))
    @Column(name="entrypoints_params")
    public List<String> params;

    @Column(name = "current", nullable = false)
    public boolean current;

    @Column(name = "run_params")
    public String allParams;

    public EntryPoint(Snapshot snapshot,String executable, List<String> params) {
        super();
        this.snapshot = snapshot;
        this.executable=executable;
        this.params = params;

        String allParams = "";
        for(String p: params){
            allParams +=p;
        }
        this.allParams = allParams;
    }

    public EntryPoint() {
    }
}
