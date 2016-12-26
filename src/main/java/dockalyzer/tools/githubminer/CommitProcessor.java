package dockalyzer.tools.githubminer;

import com.fasterxml.jackson.core.json.JsonGeneratorImpl;
import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.Lists;
import dockalyzer.models.ChangedFile;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.process.extract.DateExtractor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
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
        for (int i = 0; i < historyOfFile.size(); i++) {
            commitDates.add(new Date(historyOfFile.get(i).getCommitTime() * 1000L));
        }
        return commitDates;
    }

    public static <E> List<E> toList(Iterable<E> iterable) {
        if (iterable instanceof List) {
            return (List<E>) iterable;
        }
        ArrayList<E> list = new ArrayList<E>();
        if (iterable != null) {
            for (E e : iterable) {
                list.add(e);
            }
        }
        return list;
    }

    public List<ChangedFile> getChangedFilesWithinCommit(Snapshot snap, RevCommit revCommit, Repository repository, int rangeSize, int range_index, Git git, String repoPath) throws IOException, GitAPIException {
        System.out.println("6.3.1 get Changed Files of a Commit (getChangedFilesWithinCommit) with the range: " + +range_index + " & rangeSize: " + rangeSize);
        RevWalk walk = new RevWalk(repository);
        walk.reset();
        git.close();
        RevCommit dockerCommit = walk.parseCommit(revCommit.getId());
        //Iterable<RevCommit> commits = git.log().all().call();
        // List<RevCommit> xcommits = toList(commits);
        git.close();
        walk.reset();

        List<Ref> branches = git.branchList().call();

        List<RevCommit> commitsList = null;
        for (Ref branch : branches) {
            String branchName = branch.getName();

            System.out.println("Commits of branch: " + branchName);
            System.out.println("-------------------------------------");

            Iterable<RevCommit> branchCommits = git.log().add(repository.resolve(branchName)).call();

            commitsList = Lists.newArrayList(branchCommits.iterator());
            int counter = 0;

        }
        System.out.println("-------------------------------------");


/*        Iterable<RevCommit> xxx = git.log().all().call();
        int count = 0;
        System.out.println("*******************************************************************************");
        for (RevCommit yyy : commits) {
            if(yyy.getShortMessage().equals(dockerCommit.getShortMessage())){
                RevCommit badBrother = walk.parseCommit(yyy);
                System.out.println("BadBrother: " + badBrother.getShortMessage());
                System.out.println("ID: BadBrother: " + badBrother.getId());
                System.out.println("YYYY: " + yyy.getShortMessage());
                System.out.println("ID: YYYY: " + yyy.getId());
                System.out.println("DOCKERCOMMIT: " + dockerCommit.getShortMessage());
                System.out.println("ID: DOCKERCOMMIT: " + dockerCommit.getId());

                RevCommit[] parents = yyy.getParents();
                System.out.println("LENGHT OF PARENTS!: " + parents.length);
                    for(int i =0; i<parents.length; i++){
                        if(parents[i].getShortMessage().equals(dockerCommit.getShortMessage())) {
                            System.out.println("Parent: " + dockerCommit.getShortMessage());
                            System.out.println("ChildrensParent: " + parents[1].getShortMessage());
                            System.out.println("Children: " + yyy.getShortMessage());
                            System.out.println("ID: Parent: " + dockerCommit.getId());
                            System.out.println("ID: ChildrensParent: " + parents[1].getId());
                            System.out.println("ID: Children: " + yyy.getId());
                            System.out.println("*****************************************************");
                            RevCommit parent = walk.parseCommit(dockerCommit.getId());
                            RevCommit son = walk.parseCommit(yyy.getId());
                            RevCommit sonsParent = walk.parseCommit(parents[1].getId());
                            System.out.println("********************AFTER THIS *** WALK*********************************");
                            System.out.println("ID: Parent: " + parent.getId());
                            System.out.println("ID: SonsParent: " +sonsParent.getId());
                            System.out.println("ID: Son: " + son.getId());
                            System.out.println("************************ " + count + " *****************************");
                            System.out.println("*******************************************************************************");
                        }
                    }
                }
                count++;
            }*/

        List<RevCommit> dockys = new ArrayList<>();
        RevCommit foundCommit = null;
        List<PathModel.PathChangeModel> files = new ArrayList<>();
        boolean dockyFound = false;
        finder:
        if (range_index < 0) {
            System.out.println("6.3.1.1 range_index < 0");
            int index = 0;
            for (RevCommit commit : commitsList) {
                if (commit.getId().equals(dockerCommit.getId())) {
                    dockys.add(commit);
                    while (!dockyFound) {
                        if (index == range_index) {
                            System.out.println("6.3.1.2.1 index == range_index");
                            for (RevCommit docky : dockys) {
                                List<PathModel.PathChangeModel> foundFiles = JGitUtils.getFilesInCommit(repository, docky);
                                for (PathModel.PathChangeModel found : foundFiles) {
                                    files.add(found);
                                }
                            }
                            dockyFound = true;
                            break finder;
                        } else {
                            System.out.println("6.3.1.2.1 ---------------------");
                            List<RevCommit> tempDockys = new ArrayList<>();
                            for (RevCommit docky : dockys) {
                                for (int i = 0; i < docky.getParentCount(); i++) {
                                    RevCommit parent = docky.getParents()[i];
                                    tempDockys.add(parent);
                                    walk.reset();
                                }
                            }
                            dockys = tempDockys;
                        }
                        index--;
                    }
                    //Start with: git://github.com/deepaklukose/grpc.git + deepaklukose/grpc + tools/dockerfile/distribtest/csharp_wheezy_x64/Dockerfile
                }
            }
        } else if (range_index > 0) {
            System.out.println("6.3.1.2 range_index >0");
            int index = 0;
            dockys.add(dockerCommit);
            mut:
            while (!dockyFound) {
                if (index == range_index) {
                    System.out.println("6.3.1.2.1 index == range_index");
                    for (RevCommit docky : dockys) {
                        List<PathModel.PathChangeModel> foundFiles = JGitUtils.getFilesInCommit(repository, docky);
                        for (PathModel.PathChangeModel found : foundFiles) {
                            files.add(found);
                        }
                    }
                    dockyFound = true;
                } else {
                    System.out.println("6.3.1.2.1 ---------------------");
                    List<RevCommit> tempDockys = new ArrayList<>();
                    parent:
                    for (RevCommit children : commitsList) {
                        for (int i = 0; i < children.getParentCount(); i++) {
                            System.out.println("6.3.1.2.2 forloo - size "+ commitsList.size());
                            RevCommit parent = children.getParents()[i];
                            for (RevCommit docky : dockys) {
                                if (parent.getName().equals(docky.getName())) {
                                    tempDockys.add(walk.parseCommit(children.getId()));
                                    walk.reset();
                                    index++;
                                    break parent;
                                }
                            }
                        }
                        dockyFound = true;
                    }
                    dockys = tempDockys;
                }
            }
        } else {
            dockys.add(dockerCommit);
            files = JGitUtils.getFilesInCommit(repository, dockerCommit);
            foundCommit = dockys.get(0);
        }
        System.out.println("-------------------------------------");

        List<ChangedFile> changedFiles = new ArrayList<>();
        System.out.println("6.3.2 save Models in List (getChangedFilesWithinCommit");

        for (PathModel.PathChangeModel model : files) {
            String fullFileName = null;
            String fileName = null;
            String fileType = null;
            String filePath = null;
            String[] pathTokens = model.name.split("/");
            if (pathTokens.length == 1) {
                fullFileName = model.name;
                filePath = "";
            } else {
                filePath = "";
                for (int i = 0; i < pathTokens.length - 1; i++) {
                    filePath += pathTokens[i] + "/";
                }
                try {
                    fullFileName = model.name.replaceAll(filePath, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    fullFileName = "";
                }
            }
            if (fullFileName.contains(".")) {
                String[] parts = fullFileName.split("\\.");
                fileName = parts[0];
                fileType = parts[1];
            } else {
                fileName = fullFileName;
                fileType = "";
            }
            ChangedFile changedFile = new ChangedFile(snap,
                    model.path,
                    fullFileName,
                    fileName,
                    filePath,
                    fileType,
                    model.mode, model.changeType, model.deletions, model.insertions,
                    range_index, rangeSize, model.commitId, repoPath);
            changedFiles.add(changedFile);
        }
        return changedFiles;
    }
}


