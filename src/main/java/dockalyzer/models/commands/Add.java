package dockalyzer.models.commands;

import dockalyzer.models.SQL.Snapshot;

import javax.persistence.*;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */

@Entity
@Table(name = "df_add")
public class Add extends Instruction{
    @Id
    @GeneratedValue(generator = "SEC_GEN_ADD", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SEC_GEN_ADD", sequenceName = "SEC_ADD",allocationSize=1)
    @Column(name="ADD_ID", unique=true, nullable=false)
    public long id;

    @ManyToOne
    @JoinColumn(name ="SNAP_ID")
    Snapshot snapshot;

    @Column(name = "source")
    public String source;

    @Column(name = "source_destination")
    public String sourceDestination;

    @Column(name = "destination")
    public String destination;

    @Column(name = "current", nullable = false)
    public boolean current;

    public Add(Snapshot snapshot, String source, String destinatation) {
        super();
        this.snapshot = snapshot;
        this.source = source;
        this.destination=destinatation;

        this.sourceDestination = source +"->" + destination;

    }

    public Add() {
    }
}
