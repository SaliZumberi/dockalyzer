package dockalyzer.tools.githubminer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class DiffProcessor {

    public static void showDiffbetweenTwoFiles(RevCommit oldCommit, RevCommit newCommit, String fileName, Repository repository, Git git) throws IOException, GitAPIException {
        System.out.println("******* Diff of two Commits of + " + fileName + " *******");
        RevTree newRevTree = generateRevTree(newCommit, repository);
        RevTree oldRevTree = generateRevTree(oldCommit, repository);

        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldRevTree);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newRevTree);

        // then the procelain diff-command returns a list of diff entries
        List<DiffEntry> diff = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                setPathFilter(PathFilter.create(fileName)).
                call();
        for (DiffEntry entry : diff) {
            System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
            try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                formatter.setRepository(repository);
                formatter.format(entry);
            }
        }
        System.out.println("*********************************************************" + '\n');
    }

    public static AbstractTreeIterator prepareTreeParser(Repository repository, RevTree tree) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }
            walk.dispose();
            return oldTreeParser;
        }
    }

    public static RevTree generateRevTree(RevCommit commit, Repository repository) {
        RevWalk walk = new RevWalk(repository);
        try {
            return walk.parseTree(commit.getTree().getId());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    public static void showDiffOF2(Repository repository, RevCommit commit) throws IOException {
        System.out.println(commit.getFullMessage());

        RevWalk rw = new RevWalk(repository);
        ObjectId head = repository.resolve(Constants.HEAD);
        RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
        for (DiffEntry diff : diffs) {
            System.out.println(MessageFormat.format("({0} {1} {2}", diff.getChangeType().name(), diff.getNewMode().getBits(), diff.getNewPath()));
        }
    }
    public static void showDiffFilesOfTwoCommits(RevCommit oldCommit, RevCommit newCommit, Repository repository, Git git) throws IOException {
        System.out.println("******* File which changed within this those two commits *******");
        RevWalk walk = new RevWalk(repository);

        RevTree newRevTree = generateRevTree(newCommit, repository);
        RevTree oldRevTree = generateRevTree(oldCommit, repository);

        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectId oldTree = git.getRepository().resolve(newRevTree.getName()); // equals newCommit.getTree()
        oldTreeIter.reset(reader, oldTree);

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        ObjectId newTree = git.getRepository().resolve(oldRevTree.getName()); // equals oldCommit.getTree()
        newTreeIter.reset(reader, newTree);

        DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream());
        diffFormatter.setRepository(git.getRepository());
        List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (DiffEntry entry : entries) {
            diffFormatter.format(diffFormatter.toFileHeader(entry));
            System.out.println(entry);
            diffFormatter.format(entry);
            entry.getOldId();
            String diffText = out.toString("UTF-8");
            //parseDiffEntry(diffText);
            System.out.println(diffText);
            System.out.println("getNewId" + entry.getNewId());
            System.out.println("getNewId" + entry.getNewId());
            System.out.println("getOldId" + entry.getOldId());
            System.out.println("getNewPath" + entry.getNewPath());
            System.out.println("getOldPath" + entry.getOldPath());
            System.out.println("getChangeType" + entry.getChangeType());
            System.out.println("getNewMode" + entry.getNewMode());
            System.out.println("getOldMode" + entry.getOldMode());

            out.reset();

        }
        System.out.println("****************************************************************"+ '\n');
    }

   /* public static void parseDiffEntry(String string){
        Matcher m = MY_PATTERN.matcher("FOO[BAR]");
        while (m.find()) {
            String s = m.group(1);
            // s now contains "BAR"
        }
    }*/
    public static void findFile(String name, RevCommit commit, Repository repository) throws IOException {
        ObjectId lastCommitId = repository.resolve(Constants.HEAD);

        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repository)) {
            // if you want to find the firstCommit --> RevCommit commit = revWalk.parseCommit(lastCommitId);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            System.out.println("Having tree: " + tree);

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create("Dockerfile"));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file 'README.md'");
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                loader.copyTo(System.out);
            }

            revWalk.dispose();
        }
    }


    public static void diffCommit(RevCommit newCommit, Repository repo, Git git) throws IOException {

        //Get the commit you are looking for.

        System.out.println("LogCommit: " + newCommit);
        String logMessage = newCommit.getFullMessage();
        System.out.println("LogMessage: " + logMessage);
        //Print diff of the commit with the previous one.
        System.out.println(getDiffOfCommit(newCommit, git, repo));

    }
    //Helper gets the diff as a string.
    public static String getDiffOfCommit(RevCommit newCommit, Git git, Repository repo) throws IOException {

        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(newCommit, repo);
        if(oldCommit == null){
            return "Start of repo";
        }
        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit, git);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit, git);
        OutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(git.getRepository());
            formatter.format(oldTreeIterator, newTreeIterator);
        }
        String diff = outputStream.toString();
        return diff;
    }
    //Helper function to get the previous commit.
    public static RevCommit getPrevHash(RevCommit commit, Repository repo)  throws  IOException {

        try (RevWalk walk = new RevWalk(repo)) {
            // Starting point
            walk.markStart(commit);
            int count = 0;
            for (RevCommit rev : walk) {
                // got the previous commit.
                if (count == 1) {
                    return rev;
                }
                count++;
            }
            walk.dispose();
        }
        //Reached end and no previous commits.
        return null;
    }
    //Helper function to get the tree of the changes in a commit. Written by Rüdiger Herrmann
    public static AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId, Git git) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(commitId);
            ObjectId treeId = commit.getTree().getId();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }
}
