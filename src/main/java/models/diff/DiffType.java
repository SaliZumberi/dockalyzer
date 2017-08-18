package models.diff;
import lombok.Getter;
import lombok.Setter;
import models.Diff;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 02.12.2016.
 */
@Entity
@Getter
@Setter
@Table(name = "diff_type")
public class DiffType {
    @Id
    @GeneratedValue(generator = "SEC_GEN_DIFF_TYPE", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_DIFF_TYPE", sequenceName = "SEC_DIFF_TYPE", allocationSize = 1)
    @Column(name = "DIFF_TYPE_ID", unique = true, nullable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    private Enum instruction;

    @Column(name = "change_type", nullable = false)
    private String changeType;

    @Column(name = "before")
    private String before;

    @Column(name = "after")
    private String after;

    @Column(name = "executable")
    private String executable;

    @ManyToOne
    @JoinColumn(name ="DIFF_ID")
    private Diff diff;


    public DiffType(Diff diff) {
        this.diff = diff;
    }

    public DiffType() {
    }

    public void setBeforeAndAfter(String before,String after) {
        this.before = before;
        this.after = after;
    }

    public void setBeforeAndAfter(String before,String after, String executable) {
        this.before = before;
        this.after = after;
        this.executable = executable;
    }
    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String  getChangeType() {
        return changeType;
    }

    public void setChangeType(Enum changeType) {
        Class enumy = changeType.getClass();
        String enumyClassName = enumy.getSimpleName();
        String changetype = changeType.name();
        this.changeType = enumyClassName+"_"+changetype;
    }

    public Enum getInstruction() {
        return instruction;
    }

    public void setInstruction(Enum instruction) {
        this.instruction = instruction;
    }



}
