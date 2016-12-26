package dockalyzer.tools.githubminer;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import dockalyzer.models.SQL.Diff;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.models.commands.*;
import dockalyzer.models.commands.enums.Instructions;
import dockalyzer.models.diff.enums.AddType;
import dockalyzer.models.diff.enums.DelType;
import dockalyzer.models.diff.enums.UpdateType;
import dockalyzer.models.diff.DiffType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class DiffProcessor {
    public static Diff diff;

    public static Diff getDiffbetweenTwoFiles(Snapshot oldSnapshot, Snapshot newSnapshot, RevCommit oldCommit, RevCommit newCommit, String dockerpath, Repository repository, Git git, String localPath) throws IOException, GitAPIException {
        Diff generatedDiff = null;
        RevTree newRevTree = generateRevTree(newCommit, repository);
        RevTree oldRevTree = generateRevTree(oldCommit, repository);

        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldRevTree);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newRevTree);

        // then the procelain diff-command returns a list of diff entries
        List<DiffEntry> diff = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                setPathFilter(PathFilter.create(dockerpath)).
                call();
        File tempDockferile = null;
        String tempname = null;
        String filename = null;

        List<Diff> diffs = new ArrayList<>();
        for (DiffEntry entry : diff) {
            filename = "tempDiff";
            tempname = localPath + "/" + filename;
            tempDockferile = new File(tempname);
            FileOutputStream fop = new FileOutputStream(tempDockferile);

            try (DiffFormatter formatter = new DiffFormatter(fop)) {
                formatter.setRepository(repository);
                formatter.format(entry);
                // generatedDiff = getGenerateDiff(tempname);
            }
        }
        return generatedDiff;
    }

   /* public static Diff getGenerateDiff(String fileName) throws IOException {
        Diff diff = new Diff();
        File diffFile = new File(fileName);

        FileInputStream fis = new FileInputStream(diffFile);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        String newLine = null;

        boolean changeFlag = false;

        boolean emptyStart = false;
        boolean emptyEnd  =false;

        String deletetText ="";
        String insertetText= "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if(line.contains("--- ") || line.contains("+++ ") || line.startsWith("diff ") || line.startsWith("index " )|| line.startsWith("@" )){
            // ignore
            }else{
                if (line.contains("\t")){
                    line = line.replaceFirst("\t"," ");
                }
                if (line.contains("/golang/vab820-quad/alpine/1.7/slim/Dockerfile")){
                    line = line.replaceFirst("\t"," ");
                }
                if(line.startsWith("-")){
                    if(!changeFlag){
                        //new change
                        changeFlag = true;
                        emptyStart = true;
                        deletetText += line.replaceFirst("-","");
                    }else{
                        deletetText += line.replaceFirst("-","");
                    }

                }else if(line.startsWith("+")){
                    if(!changeFlag){
                        //new change
                        changeFlag = true;
                        emptyStart = true;
                        insertetText += line.replaceFirst("\\+","");
                    }else{
                        //continoue change
                        insertetText += line.replaceFirst("\\+","");
                    }
                    //deletion
                }else{
                    if(emptyStart && changeFlag){
                        emptyEnd  = true;
                    }
                }
            }
            if(emptyStart && emptyEnd){

            //Object Creation
                if(deletetText.length() >0 && insertetText.length() >0){
                    diff.deletionsInsertions.add(new DeletionInsertion(diff,deletetText,insertetText,""));
                }else if(deletetText.length() >0 && insertetText.length() ==0){
                    diff.deletionsOnly.add(new DeletionOnly(diff,deletetText,""));

                }else if(deletetText.length()==0 && insertetText.length() >0){
                    diff.insertionsOnly.add(new InsertionOnly(diff,insertetText,""));

                }
                changeFlag = false;
                emptyEnd  =false;
                emptyStart  =false;
                deletetText ="";
                insertetText= "";
            }


        }

        reader.close();
        return diff;
    }*/

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
            } catch (Exception e) {
                e.printStackTrace();
            }

            revWalk.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Helper gets the diff as a string.
    public static String getDiffOfCommit(RevCommit newCommit, Git git, Repository repo) throws IOException {
        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(newCommit, repo);
        if (oldCommit == null) {
            return "Start of repo";
        }
        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit, git);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit, git);
        OutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(git.getRepository());
            formatter.format(oldTreeIterator, newTreeIterator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String diff = outputStream.toString();
        return diff;
    }

    //Helper function to get the previous commit.
    public static RevCommit getPrevHash(RevCommit commit, Repository repo) throws IOException {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Reached end and no previous commits.
        return null;
    }

    //Helper function to get the tree of the changes in a commit.
    public static AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId, Git git) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(commitId);
            ObjectId treeId = commit.getTree().getId();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }

    public static int[] getDiffStatistic(List<DiffType> diffTypes) {
        int[] stats = new int[3];

        for (DiffType diffType : diffTypes) {
            if (diffType.getChangeType().contains("AddType")) {
                stats[0]++;
            }
            if (diffType.getChangeType().contains("DelType")) {
                stats[1]++;
            }
            if (diffType.getChangeType().contains("UpdateType")) {
                stats[2]++;
            }
        }
        return stats;
    }

    public static Diff getDiff(Snapshot oldSnapShot, Snapshot newSnapshot) {
        diff = new Diff();
        diff.setSnapshots(oldSnapShot, newSnapshot);

        if (oldSnapShot == null && newSnapshot != null) {
            diff.setDiffState("NULL_COMMIT");
            Snapshot nullSnap = new Snapshot();
            diff.diffs = getDiffTypeOfTwoSnapshot(nullSnap, newSnapshot);
            int[] stats = getDiffStatistic(diff.diffs);
            diff.setIns(stats[0]);
            diff.setDel(stats[1]);
            diff.setMod(stats[2]);
        } else if (oldSnapShot != null && newSnapshot == null) {
            diff.setDiffState("COMMIT_NULL");
            Snapshot nullSnap = new Snapshot();
            diff.diffs = getDiffTypeOfTwoSnapshot(oldSnapShot, nullSnap);
            int[] stats = getDiffStatistic(diff.diffs);
            diff.setIns(stats[0]);
            diff.setDel(stats[1]);
            diff.setMod(stats[2]);
        } else if (newSnapshot != null && oldSnapShot != null) {
            diff.setDiffState("COMMIT_COMMIT");
            Snapshot nullSnap = new Snapshot();
            diff.diffs = getDiffTypeOfTwoSnapshot(oldSnapShot, newSnapshot);
            int[] stats;
            if (diff.diffs != null) {
                stats = getDiffStatistic(diff.diffs);
            } else {
                //TODO: ENABLE COMMENTS
                stats = new int[]{0, 0, 0};
            }
            diff.setIns(stats[0]);
            diff.setDel(stats[1]);
            diff.setMod(stats[2]);

        } else {
            return null;
        }
        return diff;
    }

    private static List<DiffType> getDiffTypeOfTwoSnapshot(Snapshot oldSnapShot, Snapshot newSnapshot) {
        List<DiffType> diffTypes = new ArrayList<>();
        /*
        Single Instructions
         */
        List<DiffType> froms = getDiffOfFrom(oldSnapShot.from, newSnapshot.from);
        if (froms != null && froms.size() > 0) {
            getDiffOfFrom(oldSnapShot.from, newSnapshot.from).forEach(x -> diffTypes.add(x));
        }

        DiffType maintainer = getDiffOfMaintainer(oldSnapShot.maintainer, newSnapshot.maintainer);
        if (maintainer != null) {
            diffTypes.add(getDiffOfMaintainer(oldSnapShot.maintainer, newSnapshot.maintainer));
        }

        List<DiffType> cmds = getDiffOfCmd(oldSnapShot.cmd, newSnapshot.cmd);
        if (cmds != null) {
            getDiffOfCmd(oldSnapShot.cmd, newSnapshot.cmd).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> entryPoints = getDiffOfEntryPoint(oldSnapShot.entryPoint, newSnapshot.entryPoint);
        if (entryPoints != null && entryPoints.size() > 0) {
            getDiffOfEntryPoint(oldSnapShot.entryPoint, newSnapshot.entryPoint).forEach(x -> diffTypes.add(x));
        }

        DiffType stopSignals = getDiffOfStopSignal(oldSnapShot.stopSignals, newSnapshot.stopSignals);
        if (stopSignals != null) {
            diffTypes.add(getDiffOfStopSignal(oldSnapShot.stopSignals, newSnapshot.stopSignals));
        }


        /*
        Multiple Instructions with Params
         */

        List<DiffType> runs = getDiffOfMultipleRuns(oldSnapShot.runs, newSnapshot.runs);
        if (runs != null && runs.size() > 0) {
            getDiffOfMultipleRuns(oldSnapShot.runs, newSnapshot.runs).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> labels = getDiffOfMultipleLabels(oldSnapShot.labels, newSnapshot.labels);
        if (labels != null && labels.size() > 0) {
            getDiffOfMultipleLabels(oldSnapShot.labels, newSnapshot.labels).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> adds = getDiffOfMultipleAdds(oldSnapShot.adds, newSnapshot.adds);
        if (adds != null && adds.size() > 0) {
            getDiffOfMultipleAdds(oldSnapShot.adds, newSnapshot.adds).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> copies = getDiffOfMultipleCopies(oldSnapShot.copies, newSnapshot.copies);
        if (copies != null && copies.size() > 0) {
            getDiffOfMultipleCopies(oldSnapShot.copies, newSnapshot.copies).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> envs = getDiffOfMultipleEnvs(oldSnapShot.envs, newSnapshot.envs);
        if (envs != null && envs.size() > 0) {
            getDiffOfMultipleEnvs(oldSnapShot.envs, newSnapshot.envs).forEach(x -> diffTypes.add(x));
        }


         /*
        Multiple Instructions without Params
         */

        List<DiffType> users = getDiffOfMultipleUsers(oldSnapShot.users, newSnapshot.users);
        if (users != null && users.size() > 0) {
            getDiffOfMultipleUsers(oldSnapShot.users, newSnapshot.users).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> workDirs = getDiffOfMultipleWorkDir(oldSnapShot.workDirs, newSnapshot.workDirs);
        if (workDirs != null && workDirs.size() > 0) {
            getDiffOfMultipleWorkDir(oldSnapShot.workDirs, newSnapshot.workDirs).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> comments = getDiffOfMultipleComments(oldSnapShot.comments, newSnapshot.comments);
        if (comments != null && comments.size() > 0) {
            getDiffOfMultipleComments(oldSnapShot.comments, newSnapshot.comments).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> volumes = getDiffOfMultipleVolumes(oldSnapShot.volumes, newSnapshot.volumes);
        if (volumes != null && volumes.size() > 0) {
            getDiffOfMultipleVolumes(oldSnapShot.volumes, newSnapshot.volumes).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> exposes = getDiffOfMultipleExposes(oldSnapShot.exposes, newSnapshot.exposes);
        if (exposes != null && exposes.size() > 0) {
            getDiffOfMultipleExposes(oldSnapShot.exposes, newSnapshot.exposes).forEach(x -> diffTypes.add(x));
        }

        List<DiffType> args = getDiffOfMultipleArgs(oldSnapShot.args, newSnapshot.args);
        if (args != null && args.size() > 0) {
            getDiffOfMultipleArgs(oldSnapShot.args, newSnapshot.args).forEach(x -> diffTypes.add(x));
        }

        //getDiffOfHealthCheck(oldSnapShot.healthCheck, newSnapshot.healthCheck);
        // getDiffOfOnBuilds(oldSnapShot.onBuilds, newSnapshot.onBuilds);

        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleEnvs(List<Env> oldEnvs, List<Env> newEnvs) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Env> oldSnapShot = oldEnvs;
        List<Env> newSnapshot = newEnvs;

        /*
        Copy Arrays
         */
        List<Env> oldchanged = new ArrayList<>(oldSnapShot);
        List<Env> newchanged = new ArrayList<>(newSnapshot);

        List<Env> notChangedInstruction = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchanged.size(); x++) {
                if (oldSnapShot.get(i).key.equals(newchanged.get(x).key) &&
                        oldSnapShot.get(i).value.equals(newchanged.get(x).value)) {
                    newchanged.remove(x);
                    notChangedInstruction.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }

        }

            /*
            Delete the Runs which have not changed
             */

        List<Env> notUpdated = notChangedInstruction;
        oldchanged.removeAll(notChangedInstruction);


        List<Env> tempOld = new ArrayList<>();
        List<Env> tempNew = new ArrayList<>();
        newchanged.forEach(x -> tempNew.add(x));
        oldchanged.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            for (DiffType diffsOfNewRun : getDiffOfEnvs(tempOld.get(0), tempNew.get(0))) {
                diffTypes.add(diffsOfNewRun);
            }
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Env newInstruction : tempNew) {
                for (DiffType diffsOfNewRun : getDiffOfEnvs(null, newInstruction)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Env deletedRun : tempOld) {
                for (DiffType diffsOfNewRun : getDiffOfEnvs(deletedRun, null)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else {
            boolean mappingFlag = false;
            List<Env> deletedItems = new ArrayList<>();
            List<Env> newItems = new ArrayList<>();

            boolean found = false;
            while (!mappingFlag) {
                if (tempOld.size() != 0) {
                    for (int y = 0; y < tempOld.size(); y++) {
                        for (int x = 0; x < tempNew.size(); x++) {
                            if (tempOld.get(y).key.equals(tempNew.get(x).key)) {

                                for (DiffType diffsOfNewRun : getDiffOfEnvs(tempOld.get(y), tempNew.get(x))) {
                                    diffTypes.add(diffsOfNewRun);
                                }
                                tempOld.remove(y);
                                tempNew.remove(x);
                                found = true;
                                break;
                            }


                        }
                        if (found) {
                            found = false;
                        } else {
                            deletedItems.add(tempOld.get(y));
                            tempOld.remove(y);
                        }
                        break;
                    }
                } else {
                    for (Env newInstruction : tempNew) {
                        newItems.add(newInstruction);
                        for (DiffType diffsOfNewRun : getDiffOfEnvs(null, newInstruction)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    for (Env deletedRun : deletedItems) {
                        for (DiffType diffsOfNewRun : getDiffOfEnvs(deletedRun, null)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    mappingFlag = true;
                }
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }


    private static List<DiffType> getDiffOfMultipleCopies(List<Copy> oldAdds, List<Copy> newAdds) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Copy> oldSnapShot = oldAdds;
        List<Copy> newSnapshot = newAdds;

        /*
        Copy Arrays
         */
        List<Copy> oldchanged = new ArrayList<>(oldSnapShot);
        List<Copy> newchanged = new ArrayList<>(newSnapshot);

        List<Copy> notChangedInstruction = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchanged.size(); x++) {
                if (oldSnapShot.get(i).source.equals(newchanged.get(x).source) &&
                        oldSnapShot.get(i).destination.equals(newchanged.get(x).destination)) {
                    newchanged.remove(x);
                    notChangedInstruction.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }

        }

            /*
            Delete the Runs which have not changed
             */

        List<Copy> notUpdated = notChangedInstruction;
        oldchanged.removeAll(notChangedInstruction);


        List<Copy> tempOld = new ArrayList<>();
        List<Copy> tempNew = new ArrayList<>();
        newchanged.forEach(x -> tempNew.add(x));
        oldchanged.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            for (DiffType diffsOfNewRun : getDiffOfCopies(tempOld.get(0), tempNew.get(0))) {
                diffTypes.add(diffsOfNewRun);
            }
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Copy newInstruction : tempNew) {
                for (DiffType diffsOfNewRun : getDiffOfCopies(null, newInstruction)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Copy deletedRun : tempOld) {
                for (DiffType diffsOfNewRun : getDiffOfCopies(deletedRun, null)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else {
            boolean mappingFlag = false;
            List<Copy> deletedItems = new ArrayList<>();
            List<Copy> newItems = new ArrayList<>();

            boolean found = false;
            while (!mappingFlag) {
                if (tempOld.size() != 0) {
                    for (int y = 0; y < tempOld.size(); y++) {
                        for (int x = 0; x < tempNew.size(); x++) {
                            if (tempOld.get(y).source.equals(tempNew.get(x).source)) {
                                for (DiffType diffsOfNewRun : getDiffOfCopies(tempOld.get(y), tempNew.get(x))) {
                                    diffTypes.add(diffsOfNewRun);
                                }
                                tempOld.remove(y);
                                tempNew.remove(x);
                                found = true;
                                break;
                            }


                        }
                        if (found) {
                            found = false;
                        } else {
                            deletedItems.add(tempOld.get(y));
                            tempOld.remove(y);
                        }
                        break;
                    }
                } else {
                    for (Copy newInstruction : tempNew) {
                        newItems.add(newInstruction);
                        for (DiffType diffsOfNewRun : getDiffOfCopies(null, newInstruction)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    for (Copy deletedRun : deletedItems) {
                        for (DiffType diffsOfNewRun : getDiffOfCopies(deletedRun, null)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    mappingFlag = true;
                }
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleAdds(List<Add> oldAdds, List<Add> newAdds) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Add> oldSnapShot = oldAdds;
        List<Add> newSnapshot = newAdds;

        /*
        Copy Arrays
         */
        List<Add> oldchanged = new ArrayList<>(oldSnapShot);
        List<Add> newchanged = new ArrayList<>(newSnapshot);

        List<Add> notChangedInstruction = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchanged.size(); x++) {
                if (oldSnapShot.get(i).source.equals(newchanged.get(x).source) &&
                        oldSnapShot.get(i).destination.equals(newchanged.get(x).destination)) {
                    newchanged.remove(x);
                    notChangedInstruction.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }

        }

            /*
            Delete the Runs which have not changed
             */

        List<Add> notUpdated = notChangedInstruction;
        oldchanged.removeAll(notChangedInstruction);


        List<Add> tempOld = new ArrayList<>();
        List<Add> tempNew = new ArrayList<>();
        newchanged.forEach(x -> tempNew.add(x));
        oldchanged.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            for (DiffType diffsOfNewRun : getDiffOfAdds(tempOld.get(0), tempNew.get(0))) {
                diffTypes.add(diffsOfNewRun);
            }
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Add newInstruction : tempNew) {
                for (DiffType diffsOfNewRun : getDiffOfAdds(null, newInstruction)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Add deletedRun : tempOld) {
                for (DiffType diffsOfNewRun : getDiffOfAdds(deletedRun, null)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else {
            boolean mappingFlag = false;
            List<Add> deletedItems = new ArrayList<>();
            List<Add> newItems = new ArrayList<>();

            boolean found = false;
            while (!mappingFlag) {
                if (tempOld.size() != 0) {
                    for (int y = 0; y < tempOld.size(); y++) {
                        for (int x = 0; x < tempNew.size(); x++) {
                            if (tempOld.get(y).source.equals(tempNew.get(x).source)) {
                                for (DiffType diffsOfNewRun : getDiffOfAdds(tempOld.get(y), tempNew.get(x))) {
                                    diffTypes.add(diffsOfNewRun);
                                }
                                tempOld.remove(y);
                                tempNew.remove(x);
                                found = true;
                                break;
                            }


                        }
                        if (found) {
                            found = false;
                        } else {
                            deletedItems.add(tempOld.get(y));
                            tempOld.remove(y);
                        }
                        break;
                    }
                } else {
                    for (Add newInstruction : tempNew) {
                        newItems.add(newInstruction);
                        for (DiffType diffsOfNewRun : getDiffOfAdds(null, newInstruction)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    for (Add deletedRun : deletedItems) {
                        for (DiffType diffsOfNewRun : getDiffOfAdds(deletedRun, null)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    mappingFlag = true;
                }
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }


    private static List<DiffType> getDiffOfMultipleLabels(List<Label> oldLabels, List<Label> newLabels) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Label> oldSnapShot = oldLabels;
        List<Label> newSnapshot = newLabels;

        /*
        Copy Arrays
         */
        List<Label> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Label> newchangedRuns = new ArrayList<>(newSnapshot);

        List<Label> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).key.equals(newchangedRuns.get(x).key) &&
                        oldSnapShot.get(i).value.equals(newchangedRuns.get(x).value)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }

        }

            /*
            Delete the Runs which have not changed
             */

        List<Label> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);


        List<Label> tempOldRunList = new ArrayList<>();
        List<Label> tempNewRunList = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNewRunList.add(x));
        oldchangedRuns.forEach(x -> tempOldRunList.add(x));

        if (tempOldRunList.size() == 1 & tempNewRunList.size() == 1) {
            for (DiffType diffsOfNewRun : getDiffOfLabels(tempOldRunList.get(0), tempNewRunList.get(0))) {
                diffTypes.add(diffsOfNewRun);
            }
        } else if (tempOldRunList.size() == 0 & tempNewRunList.size() > 0) {
            for (Label newRun : tempNewRunList) {
                for (DiffType diffsOfNewRun : getDiffOfLabels(null, newRun)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else if (tempOldRunList.size() > 0 & tempNewRunList.size() == 0) {
            for (Label deletedRun : tempOldRunList) {
                for (DiffType diffsOfNewRun : getDiffOfLabels(deletedRun, null)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else {
            boolean mappingFlag = false;
            List<Label> deletedRuns = new ArrayList<>();
            List<Label> newRuns = new ArrayList<>();

            boolean found = false;
            while (!mappingFlag) {
                if (tempOldRunList.size() != 0) {
                    for (int y = 0; y < tempOldRunList.size(); y++) {
                        for (int x = 0; x < tempNewRunList.size(); x++) {
                            if (tempOldRunList.get(y).key.equals(tempNewRunList.get(x).key)) {
                                for (DiffType diffsOfNewRun : getDiffOfLabels(tempOldRunList.get(y), tempNewRunList.get(x))) {
                                    diffTypes.add(diffsOfNewRun);
                                }
                                tempOldRunList.remove(y);
                                tempNewRunList.remove(x);
                                found = true;
                                break;
                            }


                        }
                        if (found) {
                            found = false;
                        } else {
                            deletedRuns.add(tempOldRunList.get(y));
                            tempOldRunList.remove(y);
                        }
                        break;
                    }
                } else {
                    for (Label newRun : tempNewRunList) {
                        newRuns.add(newRun);
                        for (DiffType diffsOfNewRun : getDiffOfLabels(null, newRun)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    for (Label deletedRun : deletedRuns) {
                        for (DiffType diffsOfNewRun : getDiffOfLabels(deletedRun, null)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    mappingFlag = true;
                }
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleRuns(List<Run> oldListRuns, List<Run> newListRuns) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Run> oldSnapShot = oldListRuns;
        List<Run> newSnapshot = newListRuns;
        /*
        Copy Arrays
         */
        List<Run> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Run> newchangedRuns = new ArrayList<>(newSnapshot);

        List<Run> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                String oldExec = oldSnapShot.get(i).executable;
                String oldParams = oldSnapShot.get(i).allParams;
                String newExec = newSnapshot.get(x).executable;
                String newParams = newSnapshot.get(x).allParams;

                if (oldSnapShot.get(i).executable.equals(newchangedRuns.get(x).executable) &&
                        oldSnapShot.get(i).allParams.equals(newchangedRuns.get(x).allParams)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }

            /*
            Delete the Runs which have not changed
             */

        List<Run> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);


        List<Run> tempOldRunList = new ArrayList<>();
        List<Run> tempNewRunList = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNewRunList.add(x));
        oldchangedRuns.forEach(x -> tempOldRunList.add(x));

        if (tempOldRunList.size() == 1 & tempNewRunList.size() == 1) {
            for (DiffType diffsOfNewRun : getDiffsOfRun(tempOldRunList.get(0), tempNewRunList.get(0))) {
                diffTypes.add(diffsOfNewRun);
            }
        } else if (tempOldRunList.size() == 0 & tempNewRunList.size() > 0) {
            for (Run newRun : tempNewRunList) {
                for (DiffType diffsOfNewRun : getDiffsOfRun(null, newRun)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else if (tempOldRunList.size() > 0 & tempNewRunList.size() == 0) {
            for (Run deletedRun : tempOldRunList) {
                for (DiffType diffsOfNewRun : getDiffsOfRun(deletedRun, null)) {
                    diffTypes.add(diffsOfNewRun);
                }
            }
        } else {
            boolean mappingFlag = false;
            List<Run> possibleMappingList = new ArrayList<>();
            List<Run> deletedRuns = new ArrayList<>();
            List<Run> newRuns = new ArrayList<>();
            List<Run> updated = new ArrayList<>();
            while (!mappingFlag) {
                if (tempOldRunList.size() != 0) {
                    for (int y = 0; y < tempOldRunList.size(); y++) {
                        for (int x = 0; x < tempNewRunList.size(); x++) {
                            if (tempOldRunList.get(y).executable.equals(tempNewRunList.get(x).executable)) {
                                possibleMappingList.add(tempNewRunList.get(x));
                            }
                        }
                        if (possibleMappingList.size() > 0) {
                            for (int i = 0; i < possibleMappingList.size(); i++) {
                                possibleMappingList.get(i).score = getParamterScore(tempOldRunList.get(y).params, possibleMappingList.get(i).params);
                            }

                            Integer indexOfMaxRun = null;
                            int maxScore = 0;
                            for (int i = 0; i < possibleMappingList.size(); i++) {
                                if (possibleMappingList.get(i).score > maxScore) {
                                    indexOfMaxRun = i;
                                    maxScore = possibleMappingList.get(i).score;
                                }
                            }

                            if (indexOfMaxRun != null) {
                                updated.add(tempOldRunList.get(y));
                                updated.add(possibleMappingList.get(indexOfMaxRun));
                                for (DiffType diffsOfNewRun : getDiffsOfRun(tempOldRunList.get(y), possibleMappingList.get(indexOfMaxRun))) {
                                    diffTypes.add(diffsOfNewRun);
                                }
                            }
                            tempOldRunList.remove(y);
                            Run mappedRun = possibleMappingList.get(indexOfMaxRun);
                            for (int i = 0; i < tempNewRunList.size(); i++) {
                                if (tempNewRunList.get(i).executable.equals(mappedRun.executable) &&
                                        tempNewRunList.get(i).allParams.equals(mappedRun.allParams)) {
                                    tempNewRunList.remove(i);
                                    break;
                                }
                            }
                            possibleMappingList = new ArrayList<>();
                        } else {
                            deletedRuns.add(tempOldRunList.get(y));
                            tempOldRunList.remove(y);
                        }
                    }
                } else {
                    for (Run newRun : tempNewRunList) {
                        newRuns.add(newRun);
                        for (DiffType diffsOfNewRun : getDiffsOfRun(null, newRun)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    for (Run deletedRun : deletedRuns) {
                        for (DiffType diffsOfNewRun : getDiffsOfRun(deletedRun, null)) {
                            diffTypes.add(diffsOfNewRun);
                        }
                    }
                    mappingFlag = true;
                    possibleMappingList = new ArrayList<>();
                    //   deletedRuns = new ArrayList<>();
                    //   newRuns = new ArrayList<>();
                }
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }

    }

    private static List<DiffType> getDiffOfMultipleUsers(List<User> oldUsers, List<User> newUsers) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<User> oldSnapShot = oldUsers;
        List<User> newSnapshot = newUsers;
        /*
        Copy Arrays
         */
        List<User> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<User> newchangedRuns = new ArrayList<>(newSnapshot);
        List<User> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).username.equals(newchangedRuns.get(x).username)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }

            /*
            Delete the Runs which have not changed
             */

        List<User> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);


        List<User> tempOld = new ArrayList<>();
        List<User> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfUsers(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (User newOnes : tempNew) {
                DiffType diffType = getDiffOfUsers(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (User deletedOnes : tempOld) {
                DiffType diffType = getDiffOfUsers(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else {
            for (User deletedOnes : tempOld) {
                DiffType diffType = getDiffOfUsers(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (User newOnes : tempNew) {
                DiffType diffType = getDiffOfUsers(null, newOnes);
                diffTypes.add(diffType);
            }
        }


        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleWorkDir(List<WorkDir> oldWorkdirs, List<WorkDir> newWorkDirs) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<WorkDir> oldSnapShot = oldWorkdirs;
        List<WorkDir> newSnapshot = newWorkDirs;
        /*
        Copy Arrays
         */
        List<WorkDir> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<WorkDir> newchangedRuns = new ArrayList<>(newSnapshot);
        List<WorkDir> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).path.equals(newchangedRuns.get(x).path)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }
        List<WorkDir> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);

        List<WorkDir> tempOld = new ArrayList<>();
        List<WorkDir> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfWorkDirs(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (WorkDir newOnes : tempNew) {
                DiffType diffType = getDiffOfWorkDirs(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (WorkDir deletedOnes : tempOld) {
                DiffType diffType = getDiffOfWorkDirs(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else {
            for (WorkDir deletedOnes : tempOld) {
                DiffType diffType = getDiffOfWorkDirs(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (WorkDir newOnes : tempNew) {
                DiffType diffType = getDiffOfWorkDirs(null, newOnes);
                diffTypes.add(diffType);
            }
        }


        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleComments(List<Comment> oldWorkdirs, List<Comment> newWorkDirs) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Comment> oldSnapShot = oldWorkdirs;
        List<Comment> newSnapshot = newWorkDirs;
        /*
        Copy Arrays
         */
        List<Comment> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Comment> newchangedRuns = new ArrayList<>(newSnapshot);
        List<Comment> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).comment.equals(newchangedRuns.get(x).comment)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }
        List<Comment> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);

        List<Comment> tempOld = new ArrayList<>();
        List<Comment> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfComments(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Comment newOnes : tempNew) {
                DiffType diffType = getDiffOfComments(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Comment deletedOnes : tempOld) {
                DiffType diffType = getDiffOfComments(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 1 & tempNew.size() > 1) {
            int bestIndex = -1;
            boolean allTested = false;
            while (!allTested) {
                epoche:
                for (int i = 0; i < tempOld.size(); i++) {
                    if (i == tempOld.size() - 1) {
                        allTested = true;
                    }

                    int max = 0;
                    for (int x = 0; x < tempNew.size(); x++) {
                        Splitter splitter = Splitter.onPattern("\\W").trimResults().omitEmptyStrings();
                        Set<String> intersection = Sets.intersection(//
                                Sets.newHashSet(splitter.split(tempOld.get(i).comment)), //
                                Sets.newHashSet(splitter.split(tempNew.get(x).comment)));

                        if (intersection.size() > max) {
                            max = intersection.size();
                            bestIndex = x;
                        }

                    }
                    if (max > 0 && tempOld.get(i).instructionAfter.equals(tempNew.get(bestIndex).instructionAfter)) {
                        DiffType diffType = getDiffOfComments(tempOld.get(i), tempNew.get(bestIndex));
                        diffTypes.add(diffType);
                        tempNew.remove(bestIndex);
                        tempOld.remove(i);
                        break epoche;
                    } else {

                    }
                }
            }
            for (Comment deletedOnes : tempOld) {
                DiffType diffType = getDiffOfComments(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (Comment newOnes : tempNew) {
                DiffType diffType = getDiffOfComments(null, newOnes);
                diffTypes.add(diffType);
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleVolumes(List<Volume> oldVolumes, List<Volume> newVolumes) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Volume> oldSnapShot = oldVolumes;
        List<Volume> newSnapshot = newVolumes;
        /*
        Copy Arrays
         */
        List<Volume> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Volume> newchangedRuns = new ArrayList<>(newSnapshot);
        List<Volume> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).value.equals(newchangedRuns.get(x).value)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }
        List<Volume> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);

        List<Volume> tempOld = new ArrayList<>();
        List<Volume> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfVolumes(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Volume newOnes : tempNew) {
                DiffType diffType = getDiffOfVolumes(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Volume deletedOnes : tempOld) {
                DiffType diffType = getDiffOfVolumes(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else {
            for (Volume deletedOnes : tempOld) {
                DiffType diffType = getDiffOfVolumes(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (Volume newOnes : tempNew) {
                DiffType diffType = getDiffOfVolumes(null, newOnes);
                diffTypes.add(diffType);
            }
        }


        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleArgs(List<Arg> oldArg, List<Arg> newArg) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Arg> oldSnapShot = oldArg;
        List<Arg> newSnapshot = newArg;
        /*
        Copy Arrays
         */
        List<Arg> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Arg> newchangedRuns = new ArrayList<>(newSnapshot);
        List<Arg> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {
                if (oldSnapShot.get(i).arg.equals(newchangedRuns.get(x).arg)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }
        List<Arg> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);

        List<Arg> tempOld = new ArrayList<>();
        List<Arg> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfArgs(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Arg newOnes : tempNew) {
                DiffType diffType = getDiffOfArgs(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Arg deletedOnes : tempOld) {
                DiffType diffType = getDiffOfArgs(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else {
            for (Arg deletedOnes : tempOld) {
                DiffType diffType = getDiffOfArgs(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (Arg newOnes : tempNew) {
                DiffType diffType = getDiffOfArgs(null, newOnes);
                diffTypes.add(diffType);
            }
        }

        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfMultipleExposes(List<Expose> oldExposes, List<Expose> newExposes) {
        List<DiffType> diffTypes = new ArrayList<>();
        List<Expose> oldSnapShot = oldExposes;
        List<Expose> newSnapshot = newExposes;

        /*
        Copy Arrays
         */
        List<Expose> oldchangedRuns = new ArrayList<>(oldSnapShot);
        List<Expose> newchangedRuns = new ArrayList<>(newSnapshot);

        List<Expose> notChangedRuns = new ArrayList<>();

        //CLEAN 1: Remove equal Run Instructions

        for (int i = 0; i < oldSnapShot.size(); i++) {
            boolean foundFlag = false;
            innerloop:
            for (int x = 0; x < newchangedRuns.size(); x++) {

                if (oldSnapShot.get(i).port == (newchangedRuns.get(x).port)) {
                    newchangedRuns.remove(x);
                    notChangedRuns.add(oldSnapShot.get(i));
                    break innerloop;
                }
            }
        }

        List<Expose> notUpdated = notChangedRuns;
        oldchangedRuns.removeAll(notChangedRuns);

        List<Expose> tempOld = new ArrayList<>();
        List<Expose> tempNew = new ArrayList<>();
        newchangedRuns.forEach(x -> tempNew.add(x));
        oldchangedRuns.forEach(x -> tempOld.add(x));

        if (tempOld.size() == 1 & tempNew.size() == 1) {
            DiffType diffType = getDiffOfExposes(tempOld.get(0), tempNew.get(0));
            diffTypes.add(diffType);
        } else if (tempOld.size() == 0 & tempNew.size() > 0) {
            for (Expose newOnes : tempNew) {
                DiffType diffType = getDiffOfExposes(null, newOnes);
                diffTypes.add(diffType);
            }
        } else if (tempOld.size() > 0 & tempNew.size() == 0) {
            for (Expose deletedOnes : tempOld) {
                DiffType diffType = getDiffOfExposes(deletedOnes, null);
                diffTypes.add(diffType);
            }
        } else {
            for (Expose deletedOnes : tempOld) {
                DiffType diffType = getDiffOfExposes(deletedOnes, null);
                diffTypes.add(diffType);
            }
            for (Expose newOnes : tempNew) {
                DiffType diffType = getDiffOfExposes(null, newOnes);
                diffTypes.add(diffType);
            }
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }


    private static int getParamterScore(List<String> oldParams, List<String> newParams) {
        List<String> oldP = new ArrayList<>();
        List<String> newP = new ArrayList<>();

        oldParams.forEach(x -> oldP.add(x));
        newParams.forEach(x -> newP.add(x));
        oldP.retainAll(newP);
        return oldP.size() + 1;
    }

    private static DiffType getDiffOfWorkDirs(WorkDir oldWorkDir, WorkDir newWorkDir) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.WORKDIR);
        if (oldWorkDir == null && newWorkDir != null) {
            diffType.setChangeType(AddType.WORKDIR);
            diffType.setBeforeAndAfter(null, newWorkDir.path);
        } else if (oldWorkDir != null && newWorkDir == null) {
            diffType.setChangeType(DelType.WORKDIR);
            diffType.setBeforeAndAfter(oldWorkDir.path, null);
        } else if (oldWorkDir != null && newWorkDir != null) {
            if (!oldWorkDir.path.equals(newWorkDir.path)) {
                diffType.setChangeType(UpdateType.PATH);
                diffType.setBeforeAndAfter(oldWorkDir.path, newWorkDir.path);
            }
            {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static DiffType getDiffOfComments(Comment oldComment, Comment newComment) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.COMMENT);
        if (oldComment == null && newComment != null) {
            diffType.setChangeType(AddType.COMMENT);
            diffType.setBeforeAndAfter(null, newComment.comment);
        } else if (oldComment != null && newComment == null) {
            diffType.setChangeType(DelType.COMMENT);
            diffType.setBeforeAndAfter(oldComment.comment, null);
        } else if (oldComment != null && newComment != null) {
            if (!oldComment.comment.equals(newComment.comment)) {
                diffType.setChangeType(UpdateType.COMMENT);
                diffType.setBeforeAndAfter(oldComment.comment, newComment.comment);
            }else{
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static DiffType getDiffOfUsers(User oldUser, User newUser) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.USER);
        if (oldUser == null && newUser != null) {
            diffType.setChangeType(AddType.USER);
            diffType.setBeforeAndAfter(null, newUser.username);
        } else if (oldUser != null && newUser == null) {
            diffType.setChangeType(DelType.USER);
            diffType.setBeforeAndAfter(oldUser.username, null);
        } else if (oldUser != null && newUser != null) {
            if (!oldUser.username.equals(newUser.username)) {
                diffType.setChangeType(UpdateType.USER_NAME);
                diffType.setBeforeAndAfter(oldUser.username, newUser.username);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static DiffType getDiffOfVolumes(Volume oldMaintainer, Volume newVolume) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.VOLUME);
        if (oldMaintainer == null && newVolume != null) {
            diffType.setChangeType(AddType.VOLUME);
            diffType.setBeforeAndAfter(null, newVolume.value);
        } else if (oldMaintainer != null && newVolume == null) {
            diffType.setChangeType(DelType.VOLUME);
            diffType.setBeforeAndAfter(oldMaintainer.value, null);
        } else if (oldMaintainer != null && newVolume != null) {
            if (!oldMaintainer.value.equals(newVolume.value)) {
                diffType.setChangeType(UpdateType.VALUE);
                diffType.setBeforeAndAfter(oldMaintainer.value, newVolume.value);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static List<DiffType> getDiffOfCopies(Copy oldCopie, Copy newCopie) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldCopie == null && newCopie != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.COPY);
            diffType.setChangeType(AddType.COPY);
            diffType.setBeforeAndAfter(null, newCopie.sourceDestination);
            diffTypes.add(diffType);
        } else if (oldCopie != null && newCopie == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.COPY);
            diffType.setChangeType(DelType.COPY);
            diffType.setBeforeAndAfter(oldCopie.sourceDestination, null);
            diffTypes.add(diffType);
        } else if (oldCopie != null && newCopie != null) {
            if (!oldCopie.sourceDestination.equals(newCopie.sourceDestination)) {
                if (oldCopie.source.equals(newCopie.source)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.COPY);
                    diffType.setChangeType(UpdateType.SOURCE);
                    diffType.setBeforeAndAfter(oldCopie.sourceDestination, newCopie.sourceDestination);
                    diffTypes.add(diffType);
                }
                if (oldCopie.destination.equals(newCopie.destination)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.COPY);
                    diffType.setChangeType(UpdateType.DESTINATION);
                    diffType.setBeforeAndAfter(oldCopie.sourceDestination, newCopie.sourceDestination);
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfAdds(Add oldAdd, Add newAdd) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldAdd == null && newAdd != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ADD);
            diffType.setChangeType(AddType.ADD);
            diffType.setBeforeAndAfter(null, newAdd.sourceDestination);
            diffTypes.add(diffType);
        } else if (oldAdd != null && newAdd == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ADD);
            diffType.setChangeType(DelType.ADD);
            diffType.setBeforeAndAfter(oldAdd.sourceDestination, null);
            diffTypes.add(diffType);
        } else if (oldAdd != null && newAdd != null) {
            if (!oldAdd.sourceDestination.equals(newAdd.sourceDestination)) {
                if (oldAdd.source.equals(newAdd.source)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.ADD);
                    diffType.setChangeType(UpdateType.SOURCE);
                    diffType.setBeforeAndAfter(oldAdd.sourceDestination, newAdd.sourceDestination);
                    diffTypes.add(diffType);
                }
                if (oldAdd.destination.equals(newAdd.destination)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.ADD);
                    diffType.setChangeType(UpdateType.DESTINATION);
                    diffType.setBeforeAndAfter(oldAdd.sourceDestination, newAdd.sourceDestination);
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static DiffType getDiffOfExposes(Expose oldExpose, Expose newExpose) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.EXPOSE);
        if (oldExpose == null && newExpose != null) {
            diffType.setChangeType(AddType.EXPOSE);
            diffType.setBeforeAndAfter(null, String.valueOf(newExpose.port));
        } else if (oldExpose != null && newExpose == null) {
            diffType.setChangeType(DelType.EXPOSE);
            diffType.setBeforeAndAfter(String.valueOf(oldExpose.port), null);
        } else if (oldExpose != null && newExpose != null) {
            if (oldExpose.port != (newExpose.port)) {
                diffType.setChangeType(UpdateType.MAINTAINER);
                diffType.setBeforeAndAfter(String.valueOf(oldExpose.port), String.valueOf(newExpose.port));
            } else {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static List<DiffType> getDiffOfEnvs(Env oldEnv, Env newEnv) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldEnv == null && newEnv != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ENV);
            diffType.setChangeType(AddType.ENV);
            diffType.setBeforeAndAfter(null, newEnv.keyValue);
            diffTypes.add(diffType);
        } else if (oldEnv != null && newEnv == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ENV);
            diffType.setChangeType(DelType.ENV);
            diffType.setBeforeAndAfter(oldEnv.keyValue, null);
            diffTypes.add(diffType);
        } else if (oldEnv != null && newEnv != null) {
            if (!oldEnv.keyValue.equals(newEnv.keyValue)) {
                if (oldEnv.key.equals(newEnv.key)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.ENV);
                    diffType.setChangeType(UpdateType.KEY);
                    diffType.setBeforeAndAfter(oldEnv.keyValue, newEnv.keyValue);
                    diffTypes.add(diffType);
                }
                if (oldEnv.value.equals(newEnv.value)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.ENV);
                    diffType.setChangeType(UpdateType.VALUE);
                    diffType.setBeforeAndAfter(oldEnv.keyValue, newEnv.keyValue);
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfLabels(Label oldLabel, Label newLabel) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldLabel == null && newLabel != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.LABEL);
            diffType.setChangeType(AddType.LABEL);
            diffType.setBeforeAndAfter(null, newLabel.keyValue);
            diffTypes.add(diffType);
        } else if (oldLabel != null && newLabel == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.LABEL);
            diffType.setChangeType(DelType.LABEL);
            diffType.setBeforeAndAfter(oldLabel.keyValue, null);
            diffTypes.add(diffType);
        } else if (oldLabel != null && newLabel != null) {
            if (!oldLabel.keyValue.equals(newLabel.keyValue)) {
                if (oldLabel.key.equals(newLabel.key)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.LABEL);
                    diffType.setChangeType(UpdateType.KEY);
                    diffType.setBeforeAndAfter(oldLabel.keyValue, newLabel.keyValue);
                    diffTypes.add(diffType);
                }
                if (oldLabel.value.equals(newLabel.value)) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(Instructions.LABEL);
                    diffType.setChangeType(UpdateType.VALUE);
                    diffType.setBeforeAndAfter(oldLabel.keyValue, newLabel.keyValue);
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static DiffType getDiffOfArgs(Arg oldArg, Arg newArg) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.ARG);
        if (oldArg == null && newArg != null) {
            diffType.setChangeType(AddType.ARG);
            diffType.setBeforeAndAfter(null, newArg.arg);
        } else if (oldArg != null && newArg == null) {
            diffType.setChangeType(DelType.ARG);
            diffType.setBeforeAndAfter(oldArg.arg, null);
        } else if (oldArg != null && newArg != null) {
            if (!oldArg.arg.equals(newArg.arg)) {
                diffType.setChangeType(UpdateType.MAINTAINER);
                diffType.setBeforeAndAfter(oldArg.arg, newArg.arg);
            }
        } else {
            return null;
        }
        return diffType;
    }

    //TODO: ONBUILD!
    private static DiffType getDiffOfOnBuilds(List<OnBuild> oldOnBuilds, List<OnBuild> newOnBuilds) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.ONBUILD);
        return diffType;
    }

    private static List<DiffType> getDiffsOfRun(Run oldRun, Run newRun) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldRun == null && newRun != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.RUN);
            diffType.setChangeType(AddType.RUN);
            diffType.setBeforeAndAfter(null, newRun.allParams, newRun.executable);
            diffTypes.add(diffType);
        } else if (oldRun != null && newRun == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.RUN);
            diffType.setChangeType(DelType.RUN);
            diffType.setBeforeAndAfter(oldRun.allParams, null, oldRun.executable);
            diffTypes.add(diffType);
        } else if (oldRun != null && newRun != null) {
            if (!oldRun.executable.equals(newRun.executable) && oldRun.allParams.equals(newRun.allParams)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.RUN);
                diffType.setChangeType(UpdateType.EXECUTABLE);
                diffType.setBeforeAndAfter(oldRun.executable, newRun.executable);
                diffTypes.add(diffType);
            } else if (oldRun.executable.equals(newRun.executable) && !oldRun.allParams.equals(newRun.allParams)) {
                for (DiffType diffType : getParamChanges(oldRun.params, newRun.params, oldRun.executable, Instructions.RUN, false)) {
                    diffTypes.add(diffType);
                }
            } else if (!oldRun.executable.equals(newRun.executable) && !oldRun.allParams.equals(newRun.allParams)) {
                DiffType diffTypeE = new DiffType(diff);
                diffTypeE.setInstruction(Instructions.RUN);
                diffTypeE.setChangeType(UpdateType.EXECUTABLE_PARAMETER);
                diffTypeE.setBeforeAndAfter(oldRun.executable, newRun.executable);
                diffTypes.add(diffTypeE);

                for (DiffType diffType : getParamChanges(oldRun.params, newRun.params, oldRun.executable, Instructions.RUN, true)) {
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }


/*    private static DiffType getDiffOfHealthCheck(Healthcheck oldHealthcheck, Healthcheck newHealthcheck) {
        DiffType diffType = new DiffType(diff);
        if(oldHealthcheck == null && newHealthcheck != null ){
            diffType.setChangeType(AddType.HEALTHCHECK);
        }else if(oldHealthcheck !=null && newHealthcheck ==null){
            diffType.setChangeType(DelType.HEALTHCHECK);
        }else if(oldHealthcheck !=null && newHealthcheck != null){
            if(!oldHealthcheck..equals(newHealthcheck.maintainername)){
                diffType.setChangeType(DelType.HEALTHCHECK);
                diffType.setBeforeAndAfter(oldHealthcheck.maintainername,newHealthcheck.maintainername);
            }
        }else{
            return null;
        }
        return diffType;
    }*/


    private static DiffType getDiffOfStopSignal(StopSignal oldStopSignals, StopSignal newStopSignals) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.MAINAINER);
        if (oldStopSignals == null && newStopSignals != null) {
            diffType.setChangeType(AddType.MAINTAINER);
            diffType.setBeforeAndAfter(null, newStopSignals.signal);
        } else if (oldStopSignals != null && newStopSignals == null) {
            diffType.setChangeType(DelType.MAINTAINER);
            diffType.setBeforeAndAfter(oldStopSignals.signal, null);
        } else if (oldStopSignals != null && newStopSignals != null) {
            if (!oldStopSignals.signal.equals(newStopSignals.signal)) {
                diffType.setChangeType(UpdateType.MAINTAINER);
                diffType.setBeforeAndAfter(oldStopSignals.signal, newStopSignals.signal);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static List<DiffType> getDiffOfEntryPoint(EntryPoint oldEntryPoint, EntryPoint newEntryPoint) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldEntryPoint == null && newEntryPoint != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ENTRYPOINT);
            diffType.setChangeType(AddType.ENTRYPOINT);
            diffType.setBeforeAndAfter(null, newEntryPoint.allParams, newEntryPoint.executable);
            diffTypes.add(diffType);
        } else if (oldEntryPoint != null && newEntryPoint == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.ENTRYPOINT);
            diffType.setChangeType(DelType.ENTRYPOINT);
            diffType.setBeforeAndAfter(oldEntryPoint.allParams, null, oldEntryPoint.executable);
            diffTypes.add(diffType);
        } else if (oldEntryPoint != null && newEntryPoint != null) {
            if (!oldEntryPoint.executable.equals(newEntryPoint.executable) && oldEntryPoint.allParams.equals(newEntryPoint.allParams)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.ENTRYPOINT);
                diffType.setChangeType(UpdateType.EXECUTABLE);
                diffType.setBeforeAndAfter(oldEntryPoint.executable, newEntryPoint.executable);
                diffTypes.add(diffType);
            } else if (oldEntryPoint.executable.equals(newEntryPoint.executable) && !oldEntryPoint.allParams.equals(newEntryPoint.allParams)) {
                for (DiffType diffType : getParamChanges(oldEntryPoint.params, newEntryPoint.params, oldEntryPoint.executable, Instructions.ENTRYPOINT, false)) {
                    diffTypes.add(diffType);
                }
            } else if (!oldEntryPoint.executable.equals(newEntryPoint.executable) && !oldEntryPoint.allParams.equals(newEntryPoint.allParams)) {
                DiffType diffTypeE = new DiffType(diff);
                diffTypeE.setInstruction(Instructions.ENTRYPOINT);
                diffTypeE.setChangeType(UpdateType.EXECUTABLE_PARAMETER);
                diffTypeE.setBeforeAndAfter(oldEntryPoint.executable, newEntryPoint.executable);
                diffTypes.add(diffTypeE);

                for (DiffType diffType : getParamChanges(oldEntryPoint.params, newEntryPoint.params, oldEntryPoint.executable, Instructions.ENTRYPOINT, true)) {
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getDiffOfCmd(Cmd oldCmd, Cmd newCmd) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldCmd == null && newCmd != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.CMD);
            diffType.setChangeType(AddType.CMD);
            diffType.setBeforeAndAfter(null, newCmd.allParams, newCmd.executable);
            diffTypes.add(diffType);
        } else if (oldCmd != null && newCmd == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.CMD);
            diffType.setChangeType(DelType.CMD);
            diffType.setBeforeAndAfter(oldCmd.allParams, null, oldCmd.executable);
            diffTypes.add(diffType);
        } else if (oldCmd != null && newCmd != null) {
            if (!oldCmd.executable.equals(newCmd.executable) && oldCmd.allParams.equals(newCmd.allParams)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.CMD);
                diffType.setChangeType(UpdateType.CMD);
                diffType.setBeforeAndAfter(oldCmd.executable, newCmd.executable);
                diffTypes.add(diffType);
            } else if (oldCmd.executable.equals(newCmd.executable) && !oldCmd.allParams.equals(newCmd.allParams)) {
                for (DiffType diffType : getParamChanges(oldCmd.params, newCmd.params, oldCmd.executable, Instructions.CMD, false)) {
                    diffTypes.add(diffType);
                }
            } else if (!oldCmd.executable.equals(newCmd.executable) && !oldCmd.allParams.equals(newCmd.allParams)) {
                DiffType diffTypeE = new DiffType(diff);
                diffTypeE.setInstruction(Instructions.CMD);
                diffTypeE.setChangeType(UpdateType.EXECUTABLE_PARAMETER);
                diffTypeE.setBeforeAndAfter(oldCmd.executable, newCmd.executable);
                diffTypes.add(diffTypeE);

                for (DiffType diffType : getParamChanges(oldCmd.params, newCmd.params, oldCmd.executable, Instructions.CMD, true)) {
                    diffTypes.add(diffType);
                }
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }

    private static List<DiffType> getParamChanges(List<String> oldParams, List<String> newParams, String executable, Instructions instructionType, boolean bothChanged) {
        List<DiffType> diffTypes = new ArrayList<>();

        List<String> oldP = new ArrayList<>(oldParams);
        List<String> newP = new ArrayList<>(newParams);

        List<String> paramsTempa = new ArrayList<>(oldParams);
        List<String> paramsTempb = new ArrayList<>(newParams);
        paramsTempa.retainAll(paramsTempb);
        paramsTempb.retainAll(oldParams);

        List<String> intersectiona = new ArrayList<>(paramsTempa);
        List<String> intersectionb = new ArrayList<>(paramsTempb);

        oldP.removeAll(intersectiona);
        newP.removeAll(intersectionb);

        if (oldP.size() == 1 && newP.size() == 1) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(instructionType);
            if (bothChanged) {
                diffType.setChangeType(UpdateType.EXECUTABLE_PARAMETER);
            } else {
                diffType.setChangeType(UpdateType.PARAMETER);

            }
            diffType.setBeforeAndAfter(oldP.get(0), newP.get(0), executable);
            diffTypes.add(diffType);
        } else {
            if (oldP.size() > 0) {
                for (int i = 0; i < oldP.size(); i++) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(instructionType);
                    if (bothChanged) {
                        diffType.setChangeType(DelType.EXECUTABLE_PARAMETER);
                    } else {
                        diffType.setChangeType(DelType.PARAMETER);
                    }
                    diffType.setBeforeAndAfter(oldP.get(i), null, executable);
                    diffTypes.add(diffType);
                }
            }
            if (newP.size() > 0) {
                for (int i = 0; i < newP.size(); i++) {
                    DiffType diffType = new DiffType(diff);
                    diffType.setInstruction(instructionType);
                    if (bothChanged) {
                        diffType.setChangeType(AddType.EXECUTABLE_PARAMETER);
                    } else {
                        diffType.setChangeType(AddType.PARAMETER);
                    }
                    diffType.setBeforeAndAfter(null, newP.get(i), executable);
                    diffTypes.add(diffType);
                }
            }
        }
        return diffTypes;
    }

    private static DiffType getDiffOfMaintainer(Maintainer oldMaintainer, Maintainer newMaintainer) {
        DiffType diffType = new DiffType(diff);
        diffType.setInstruction(Instructions.MAINAINER);
        if (oldMaintainer == null && newMaintainer != null) {
            diffType.setChangeType(AddType.MAINTAINER);
            diffType.setBeforeAndAfter(null, newMaintainer.maintainername);
        } else if (oldMaintainer != null && newMaintainer == null) {
            diffType.setChangeType(DelType.MAINTAINER);
            diffType.setBeforeAndAfter(oldMaintainer.maintainername, null);
        } else if (oldMaintainer != null && newMaintainer != null) {
            if (!oldMaintainer.maintainername.equals(newMaintainer.maintainername)) {
                diffType.setChangeType(UpdateType.MAINTAINER);
                diffType.setBeforeAndAfter(oldMaintainer.maintainername, newMaintainer.maintainername);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return diffType;
    }

    private static List<DiffType> getDiffOfFrom(From oldFrom, From newFrom) {
        List<DiffType> diffTypes = new ArrayList<>();
        if (oldFrom == null && newFrom != null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.FROM);
            diffType.setChangeType(AddType.FROM);
            diffType.setBeforeAndAfter(null, newFrom.fullName);
            diffTypes.add(diffType);
        } else if (oldFrom != null && newFrom == null) {
            DiffType diffType = new DiffType(diff);
            diffType.setInstruction(Instructions.FROM);
            diffType.setChangeType(DelType.FROM);
            diffType.setBeforeAndAfter(oldFrom.fullName, null);
            diffTypes.add(diffType);
        } else if (oldFrom != null && newFrom != null) {
            if (!oldFrom.imagename.equals(newFrom.imagename)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.FROM);
                diffType.setChangeType(UpdateType.IMAGE_NAME);
                diffType.setBeforeAndAfter(oldFrom.imagename, newFrom.imagename);
                diffTypes.add(diffType);
            }
            if (!oldFrom.imageVersionString.equals(newFrom.imageVersionString)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.FROM);
                diffType.setChangeType(UpdateType.IMAGE_VERSION_STRING);
                diffType.setBeforeAndAfter(oldFrom.imagename + ":" + oldFrom.imageVersionString,
                        newFrom.imagename + ":" + newFrom.imageVersionString);
                diffTypes.add(diffType);
            }
            if (oldFrom.imageVersionNumber != newFrom.imageVersionNumber) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.FROM);
                diffType.setChangeType(UpdateType.IMAGE_VERSION_NUMBER);
                diffType.setBeforeAndAfter(oldFrom.imagename + ":" + String.valueOf(oldFrom.imageVersionNumber),
                        String.valueOf(newFrom.imagename + ":" + newFrom.imageVersionNumber));
                diffTypes.add(diffType);
            }
            if (!oldFrom.imageVersionString.equals(newFrom.imageVersionString)) {
                DiffType diffType = new DiffType(diff);
                diffType.setInstruction(Instructions.FROM);
                diffType.setChangeType(UpdateType.IMAGE_VERSION_DIGEST);
                diffType.setBeforeAndAfter(oldFrom.imagename + ":" + oldFrom.imageVersionString,
                        newFrom.imagename + ":" + newFrom.imageVersionString);
                diffTypes.add(diffType);
            }
        } else {
            return null;
        }
        if (diffTypes.size() > 0) {
            return diffTypes;
        } else {
            return null;
        }
    }
}
