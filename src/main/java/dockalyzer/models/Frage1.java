package dockalyzer.models;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by salizumberi-laptop on 03.11.2016.
 */
@Entity
@Table(name = "docker_Intro")
public class Frage1 {
    @Id
    @Column(name = "df_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Date projectDate;

    @Column(name = "dockerfile_created", nullable = false)
    private Date dockerfileDate;

    @Column(name = "difference", nullable = false)
    private Date differenceDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getProjectDate() {
        return projectDate;
    }

    public void setProjectDate(Date projectDate) {
        this.projectDate = projectDate;
    }

    public Date getDockerfileDate() {
        return dockerfileDate;
    }

    public void setDockerfileDate(Date dockerfileDate) {
        this.dockerfileDate = dockerfileDate;
    }

    public Date getDifferenceDate() {
        return differenceDate;
    }

    public void setDifferenceDate(Date differenceDate) {
        this.differenceDate = differenceDate;
    }

    @Override
    public String toString() {
        return id + "\t" + getDifferenceDate() + "\t" + getDockerfileDate();
    }
}
