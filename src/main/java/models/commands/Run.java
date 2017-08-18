package models.commands;

import lombok.Getter;
import lombok.Setter;
import models.Snapshot;

import javax.persistence.*;
import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
@Entity
@Setter
@Getter
@Table(name = "df_run")
public class Run extends Instruction{

    @Id
    @GeneratedValue(generator = "SEC_GEN_RUN", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_RUN", sequenceName = "SEC_RUN",allocationSize=1)
    @Column(name="RUN_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Transient
    public int score;

    @Column(name = "executable")
    public String executable;

    @Column(name = "run_params", length = 1024)
    public String allParams;

    @ElementCollection
    @CollectionTable(name="run_params", joinColumns=@JoinColumn(name="RUN_ID"))
    @Column(name="run_params", length = 1024)
    public List<String> params;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Run(Snapshot snapshot,String executable, List<String> params) {
        this.snapshot = snapshot;
        this.executable=executable;
        this.params= params;

        String allParams = "";
        for(String p: params){
            allParams += "¦"+ p;
        }

        if (allParams.length() > 240){
            this.allParams = allParams.substring(0, 240) + "...";
        }else{
            this.allParams = allParams;
        }
    }

    public Run() {
    }
}
