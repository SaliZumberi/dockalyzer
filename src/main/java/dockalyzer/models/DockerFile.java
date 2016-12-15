package dockalyzer.models;


import com.gitblit.models.PathModel;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.models.commands.*;
import dockalyzer.tools.dockerlinter.DockerLinter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by salizumberi-laptop on 22.10.2016.
 */
public class DockerFile {
    public String localRepoPath;
    public long repo_id;
    public String dockerPath;
    public long firstCommitDate;
    public Snapshot latestDockerfile = new Snapshot();

    public List<PathModel.PathChangeModel> filesWhenCreated = new ArrayList<>();

    public List<Snapshot> dockerfileSnapshots = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name="violated_rules", joinColumns=@JoinColumn(name="RUN_ID"))
    @Column(name="violated_rules")
    public List<String> violatedRules = new ArrayList<>();

    public List<String> getViolatedRules() throws IOException, InterruptedException {
        List<String> rules = new ArrayList<>();
        File dockerfile = new File(localRepoPath+"/"+dockerPath);
        String violations = DockerLinter.getReportOfLinting(dockerfile);
        String[] lines = StringUtils.split(violations, "\r\n");
        for (String l: lines){
            String[] parts = l.split(" ");
            rules.add(parts[1]);
        }
        this.violatedRules =rules;
        return rules;
    }

}