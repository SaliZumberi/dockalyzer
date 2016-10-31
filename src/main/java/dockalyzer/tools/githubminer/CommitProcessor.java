package dockalyzer.tools.githubminer;

import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import dockalyzer.process.extract.DateExtractor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gitblit.tickets.TicketIndexer.Lucene.repository;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class CommitProcessor {
    private static ArrayList<Date> getCommitDates(List<RevCommit> historyOfFile) {
        ArrayList<Date> commitDates = new ArrayList<>();
        for(int i=0; i<historyOfFile.size(); i++){
            commitDates.add(new Date(historyOfFile.get(i).getCommitTime() * 1000L));
            System.out.println(DateExtractor.getMonth(new Date(historyOfFile.get(i).getCommitTime() * 1000L)));
        }
        return commitDates;
    }

    public static List<PathModel.PathChangeModel> getChangedFilesWithinCommit(List<RevCommit> commitHistory, Repository repository) throws IOException {
        RevWalk walk = new RevWalk(repository);
        walk.reset();
        //String commitid;
        // ObjectId id = repository.resolve("2206551ac982d2136b140f04561fffc212e2d1bc");

        RevCommit commit = walk.parseCommit(commitHistory.get(0).getId());
        List<PathModel.PathChangeModel> models = JGitUtils.getFilesInCommit(repository,commit);

/*        System.out.println("LENGHTH COMMITS:" + commitHistory.size());
        System.out.println("COMMITS:" + commit.getFullMessage());
        System.out.println("COMMITID:" + commit.getId());

        for(PathModel.PathChangeModel model: models){
            System.out.println("***********");
            System.out.print(model.name + " ");
            System.out.print(model.mode + " ");
            System.out.print(model.objectId + " ");
            System.out.print(model.changeType + " ");
            System.out.print(model.deletions + " ");
            System.out.print(model.insertions + " ");
            System.out.print(model.hashCode() + " ");
            System.out.println("***********");
        }*/

        return JGitUtils.getFilesInCommit(repository,commit);
    }


}
