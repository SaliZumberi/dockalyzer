package dockalyzer.models;

import com.gitblit.models.PathModel;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ImageSearchResult;
import dockalyzer.models.commands.*;
import dockalyzer.services.DockerService;
import dockalyzer.tools.githubminer.CommitProcessor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 07.11.2016.
 */
public class DockerfileSnapshot {

    public long commitDate;
    public int indexOfCommit;
    public List<PathModel.PathChangeModel> filesChangedWithinCommit = new ArrayList<>();
    public boolean imageIsAutomated;
    public boolean imageIsOffical;
    private int starCount;
    public long id;
    public int del;
    public int add;
    public int mod;

    public List<From> froms = new ArrayList<>();
    public List<Maintainer> maintainers = new ArrayList<>();
    public List<Run> runs = new ArrayList<>();
    public List<Cmd> cmds = new ArrayList<>();
    public List<Label> labels = new ArrayList<>();
    public List<Env> envs = new ArrayList<>();
    public List<Expose> exposes = new ArrayList<>();
    public List<Add> adds = new ArrayList<>();
    public List<Copy> copies = new ArrayList<>();
    public List<EntryPoint> entryPoints = new ArrayList<>();
    public List<Volume> volumes = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public List<WorkDir> workDirs = new ArrayList<>();
    public List<Arg> args = new ArrayList<>();
    public List<OnBuild> onBuilds = new ArrayList<>();
    public List<StopSignal> stopSignals = new ArrayList<>();
    public List<Healthcheck> healthchecks = new ArrayList<>();

    public int getInstructions(){
        return froms.size() +
                maintainers.size() +
                runs.size() +
                cmds.size() +
                labels.size() +
                envs.size()  +
                exposes.size() +
                adds .size()+
                copies.size() +
                entryPoints.size() +
                volumes.size() +
                users.size() +
                workDirs.size() +
                args.size() +
                onBuilds.size() +
                stopSignals.size()+
                healthchecks.size();
    }

    public void extractCommit(Repository repository,RevCommit revCommit){
        ImageSearchResult  imageSearchResult = getImageInfos();
        this.imageIsAutomated =imageSearchResult.isAutomated();
        this.imageIsOffical =imageSearchResult.isOfficial();
        this.starCount=imageSearchResult.getStarCount();
    }


    public ImageSearchResult getImageInfos(){
        ImageSearchResult imagesInfos = null;
        try {
            imagesInfos = DockerService.getImageInfos(froms.get(froms.size() - 1).imagename);
        } catch (DockerCertificateException e) {
        e.printStackTrace();
    } catch (DockerException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return imagesInfos;
    }
}
