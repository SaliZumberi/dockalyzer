package dockalyzer.models;


import dockalyzer.models.commands.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 22.10.2016.
 */
public class DockerFile {
    public String localRepoPath;

    public List<From> froms = new ArrayList<>();
    public List<Maintainer> maintainers = new ArrayList<>();
    public List<Run> runs = new ArrayList<>();
    public List<Cmd> cmds = new ArrayList<>();
    public List<Label> labels = new ArrayList<>();
    public List<Env> envs = new ArrayList<>();
    public List<Expose> exposes = new ArrayList<>();
    public List<Add> adds = new ArrayList<>();
    public List<Copy> copies = new ArrayList<>();
    public  List<EntryPoint> entryPoints = new ArrayList<>();
    public List<Volume> volumes = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public List<WorkDir> workDirs = new ArrayList<>();
    public List<Arg> args = new ArrayList<>();
    public List<OnBuild> onBuilds = new ArrayList<>();
    public List<StopSignal> stopSignals = new ArrayList<>();
    public List<Healthcheck> healthchecks = new ArrayList<>();
}