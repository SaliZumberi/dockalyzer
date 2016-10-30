package dockalyzer.tools.githubminer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitHistoryFilter {

    public List<RevCommit> getDockerHistory(String localPath) throws IOException, NoHeadException, GitAPIException {
        Git git = new Git(getRepository(localPath));
        List<RevCommit> logs = new ArrayList<>();
        Iterable<RevCommit> iterableLogs = git.log().addPath("Dockerfile").call();
        iterableLogs.forEach(logs::add);
        git.close();
        return logs;
    }

    public Repository getRepository(String localPath) throws IOException {
        File repositoryFile = new File(localPath);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(repositoryFile).readEnvironment().findGitDir().build();
    }

    public void printDiff(Repository repository, ObjectId oldHead, ObjectId head) throws GitAPIException, IOException {
        System.out.println("Printing diff between tree: " + oldHead + " and " + head);

        // prepare the two iterators to compute the diff between
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldHead);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, head);

        // finally get the list of changed files
        Git git = new Git(repository);
        List<DiffEntry> diffs = git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
        for (DiffEntry entry : diffs) {
            System.out.println("Entry: " + entry);
        }

        System.out.println("Done");
    }

    public void printDiffTest(String firstFile, String secondFile){
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            RawText rt1 = new RawText(new File(firstFile));
            RawText rt2 = new RawText(new File(secondFile));

            EditList diffList = new EditList();
            diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
            new DiffFormatter(out).format(diffList, rt1, rt2);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(out.toString());
    }

    public void printDiffTest2(Repository repository, ObjectId oldHead, ObjectId head) throws IOException {

        File gitWorkDir = new File("repo4");
        Git git = null;
        try {
            git = Git.open(gitWorkDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectReader reader = git.getRepository().newObjectReader();

        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        try {
            oldTreeIter.reset(reader, oldHead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        try {
            newTreeIter.reset(reader, head);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<DiffEntry> diffs= null;
        try {
            diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(git.getRepository());

        for(DiffEntry diff : diffs)
        {
            df.format(diff);
            diff.getOldId();
            String diffText = out.toString("UTF-8");
            System.out.println(diffText);
            out.reset();
        }
    }



}