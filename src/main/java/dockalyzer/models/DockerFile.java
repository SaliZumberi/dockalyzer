package dockalyzer.models;


import dockalyzer.models.commands.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 22.10.2016.
 */
public class DockerFile {

    List<From> froms = new ArrayList<>();
    List<Maintainer> maintainers = new ArrayList<>();
    List<Run> runs = new ArrayList<>();
    List<Cmd> cmds = new ArrayList<>();
    List<Label> labels = new ArrayList<>();
    List<Env> envs = new ArrayList<>();
    List<Expose> exposes = new ArrayList<>();
    List<Add> adds = new ArrayList<>();
    List<Copy> copies = new ArrayList<>();
    List<EntryPoint> entryPoints = new ArrayList<>();
    List<Volume> volumes = new ArrayList<>();
    List<User> users = new ArrayList<>();
    List<WorkDir> workDirs = new ArrayList<>();
    List<Arg> args = new ArrayList<>();
    List<OnBuild> onBuilds = new ArrayList<>();
    List<StopSignal> stopSignals = new ArrayList<>();
    List<Healthcheck> healthchecks = new ArrayList<>();




}