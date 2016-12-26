package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */

@Entity
@Table(name = "df_comment")
public class Comment extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_COMMENT", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_COMMENT", sequenceName = "SEC_COMMENT",allocationSize=1)
    @Column(name="COMMENT_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name = "SNAP_ID")
    Snapshot snapshot;

    @Column(name = "instruction")
    public String instructionAfter;

    @Column(name = "comment", nullable = false)
    public String comment;

    public Comment(Snapshot snapshot, String instructionAfter, String comment) {
        super();
        this.snapshot = snapshot;
        this.instructionAfter = instructionAfter;
        if (comment.length() > 250){
            this.comment = comment.substring(0, 250) + "...";
        }else{
            this.comment = comment;
        }
    }
    public Comment() {
    }
}
