package dockalyzer.app;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import dockalyzer.models.SQL.Dockerfile;
import dockalyzer.models.SQL.Diff;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.models.commands.From;
import dockalyzer.models.commands.Run;
import dockalyzer.process.extract.DateExtractor;
import dockalyzer.process.extract.MultiDockerfileExtractor;
import dockalyzer.services.HibernateService;
import dockalyzer.tools.dockerparser.DockerParser;

import dockalyzer.tools.githubminer.DiffProcessor;
import dockalyzer.tools.githubminer.GitCloner;
import dockalyzer.tools.githubminer.GitHistoryFilter;
import dockalyzer.tools.githubminer.MultiGitCloner;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

import dockalyzer.models.GithubRepository;
import dockalyzer.services.GitHubMinerService;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class App {

    public final static String GITAPI = "https://api.github.com/";
    public final static String REPOS = "repos/";
    public final static String LOCAL_REPO_FOLDER = "github_repos/";

    public final static String GITHUB_USERNAME = "*";
    public final static String GITHUB_PASSWORD = "*";




    private final static ArrayList<Integer> months = new ArrayList<>();

    public App() {
    }

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("org.eclipse.jgit.errors.MissingObjectException").setLevel(Level.SEVERE);
        App repositoryMinerApp = new App();

        //repositoryMinerApp.findFile(historyOfFile.get(0));

        //createMonths();
        // classfiyDatesToMonths(getCommitDates(historyOfFile));
        // exportDatesToCSV(getCommitDates(historyOfFile));
        //  months.forEach(value -> System.out.println("Summary: " +value ));


        // showDiffFilesOfHistory(historyOfFile, repository,git);

        // DiffProcessor.showDiffbetweenTwoFiles(historyOfFile.get(0), historyOfFile.get(1), "Dockerfile", repository,git);

        // DiffProcessor.showDiffOF2(repository,historyOfFile.get(5));

        // DiffProcessor.diffCommit(historyOfFile.get(0),repository,git);


        //DiffProcessor.showDiffFilesOfTwoCommits(historyOfFile.get(0), historyOfFile.get(0), repository, git);

        //    CommitProcessor.getChangedFilesWithinCommit(historyOfFile,repository);

        String dockerfile = "dockerfiles/Dockerfile";

      /*  for(int i=2; i<40; i++){
            File newDockerfile = new File(dockerfile + i);
            DockerParser docky = new DockerParser("","");
            List<Comment> comments =  docky.getCommentsFromDockerfile(newDockerfile,null);
            int muti = comments.size();
        }*/

        String localPath = "github_repos/56803681";
        String dockerPath = "dockerfiles/fedora/fedora-22/fedora-22-base/Dockerfile";
        // printDockerFile(localPath,dockerPath);
        //  saveDockerObject("40481849", "git://github.com/probr/probr-core.git", "Dockerfile");
        //DockerfileSnapshot dockerfileSnapshot = getDockerFile("dockerfiles/", null);
        //JsonElement json = JsonPrinter.getJsonObject(dockerfileSnapshot, "DockerfileTest");

        //cloneRepposFullMultiThread(Runtime.getRuntime().availableProcessors());
       // extractData(0);
        extractDataMultiThread(8);
        //  databaseExample();

        // diffExample();

        // snapExample();
        Thread.sleep(10000);
        // HibernateService.deleteDockerfile(66666666);
        // HibernateService.deleteDockerfile(66666667);
    }

    public static void cloneRepposMultiThread(int threads) throws Exception {
        CsvListReader reader = new CsvListReader(new FileReader(new File("bigquerydata\\random_sample_.csv")), CsvPreference.STANDARD_PREFERENCE);

        MultiGitCloner multiGitCloner = new MultiGitCloner();
        boolean threadFlag = true;

        List<String> columns;
        int i = 0;
        List<String> subColumns = new ArrayList<>();
        while ((columns = reader.read()) != null) {
            if (i == threads - 1) {
                multiGitCloner.cloneNRepository(subColumns);
                threadFlag = true;
                while (threadFlag) {
                    if (multiGitCloner.areClonesfinished()) {
                        threadFlag = false;
                        multiGitCloner.cleanList();
                        multiGitCloner = new MultiGitCloner();
                        subColumns = new ArrayList<>();
                    } else {
                        threadFlag = true;
                        Thread.sleep(2000);
                    }
                }
                i = 0;
            }

            if (i < threads) {
                String[] split = columns.get(0).split(";");
                if (split.length > 4) {
                    String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(split[0]);
                    subColumns.add(split[3] + "," + repoFolderName);
                    i++;
                }
            }

        }
        reader.close();
    }
    public static void cloneRepposFullMultiThread(int threads) throws Exception {
        CsvListReader reader = new CsvListReader(new FileReader(new File("bigquerydata\\random_sample_.csv")), CsvPreference.STANDARD_PREFERENCE);

        MultiGitCloner multiGitCloner = new MultiGitCloner();
        boolean threadFlag = true;

        List<String> columns;
        int i = 0;
        while ((columns = reader.read()) != null) {
            if (multiGitCloner.runningThreads() == threads) {
                threadFlag = true;
                while (threadFlag) {
                    if (multiGitCloner.runningThreads() < threads) {
                        threadFlag = false;
                    } else {
                        threadFlag = true;
                        Thread.sleep(3000);
                    }
                }
            } else if (multiGitCloner.runningThreads() < threads) {
                String[] split = columns.get(0).split(";");
                if (split.length > 4) {
                    String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(split[0]);
                    multiGitCloner.runThread(split[3], repoFolderName);
                    Thread.sleep(1500);
                }
            }
        }
        reader.close();
    }

    public static void extractDataMultiThread(int threads) throws Exception {
        CsvListReader reader = new CsvListReader(new FileReader(new File("bigquerydata\\random_sample_.csv")), CsvPreference.STANDARD_PREFERENCE);

        MultiDockerfileExtractor multiDockerfileExtractor = new MultiDockerfileExtractor();
        boolean threadFlag = true;

        List<String> columns;
        int i = 0;
        while ((columns = reader.read()) != null) {
            if (multiDockerfileExtractor.runningThreads() == threads) {
                threadFlag = true;
                while (threadFlag) {
                    if (multiDockerfileExtractor.runningThreads() < threads) {
                        threadFlag = false;
                    } else {
                        threadFlag = true;
                    }
                }
            } else if (multiDockerfileExtractor.runningThreads() < threads) {
                String[] split = columns.get(0).split(";");
                if (split.length == 16) {
                    multiDockerfileExtractor.runThread(split[0], split[1], split[2], split[3], split[4],
                            Integer.parseInt(split[5]),
                            Integer.parseInt(split[6]),
                            split[9], Integer.parseInt(split[10]),
                            Integer.parseInt(split[11]),
                            Integer.parseInt(split[12]),
                            Integer.parseInt(split[13]),
                            Integer.parseInt(split[14]));
                    Thread.sleep(3000);
                }
            }
        }
        reader.close();
    }

    //new file --> id[0], repo_name[1], path to dockerfile[2], git_url[3], created at[4],
    //network_count5 , open_issues6, open_issues_count7, owner.id8, owner.type9, forks_count10, watchers_count11
    //stargazers_count12, subscribers_count13, size14
    public static void extractData(int threads) throws Exception {
        CsvListReader reader = new CsvListReader(new FileReader(new File("bigquerydata\\random_sample_.csv")), CsvPreference.STANDARD_PREFERENCE);

        List<String> columns;
        int i = 0;
        while ((columns = reader.read()) != null) {
            String[] split = columns.get(0).split(";");
            if (split.length == 16) {
                saveDockerObject(split[0], split[1], split[2], split[3], split[4],
                        Integer.parseInt(split[5]),
                        Integer.parseInt(split[6]),
                        split[9], Integer.parseInt(split[10]),
                        Integer.parseInt(split[11]),
                        Integer.parseInt(split[12]),
                        Integer.parseInt(split[13]),
                        Integer.parseInt(split[14]));
            }
        }
        reader.close();
    }


    public static void saveDockerObject(String githubId, String repo_name, String dockerPath,
                                        String gitURL, String firstCommitDate, int network, int openIssues, String ownerType,
                                        int forks, int watchers, int stargazers, int subscribers, int size) {
        try {
            System.out.println("4##################################################################################################");
            System.out.println("1. Start with: " + gitURL + " + "+  repo_name+ " + " +dockerPath);

            String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(githubId);
            String repoFolderNameDotGit = repoFolderName + "\\.git";

            GitCloner gitCloner = new GitCloner();
            System.out.println("2. Clone Repo");
            gitCloner.cloneRepository(gitURL, repoFolderName);

            System.out.println("3. Get History of Repo");

            GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
            Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);

            System.out.println("4. Load History of Repo");

            Git git = new Git(repository);

            RevWalk reVwalk = new RevWalk(repository);
            List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit, dockerPath);

            //  DiffProcessor.getDiffbetweenTwoFiles(null,null,historyOfFile.get(1), historyOfFile.get(0), dockerPath, repository, git,repoFolderName);

            if (repo_name.equals("netmackan/socialhost")){
                System.out.println("5. FUCK a new Dockerfile Object");
            }

            System.out.println("5. Create a new Dockerfile Object");
            Dockerfile dockerfile = new Dockerfile();
            dockerfile.setLocalRepoPath(repoFolderName);
            dockerfile.setRepo_id(Long.parseLong(githubId));
            dockerfile.setDockerPath(dockerPath);
            dockerfile.setFirstCommitDate(Long.valueOf(firstCommitDate));
            dockerfile.setForks(forks);
            dockerfile.setSize(size);
            dockerfile.setNetworkCount(network);
            dockerfile.setRepoPath(repo_name);
            dockerfile.setStargazers(stargazers);
            dockerfile.setOpneIssues(openIssues);
            dockerfile.setOwnerType(ownerType);
            dockerfile.setWatchers(watchers);
            dockerfile.setSubscribers(subscribers);
            dockerfile.setSubscribers(subscribers);
            dockerfile.setFirstDockerCommitDate(DateExtractor.getUnixDateFromCommit(historyOfFile.get(historyOfFile.size() - 1)));
            dockerfile.setCommits(historyOfFile.size());
            dockerfile.setViolatedRules(dockerfile.getViolatedRules());

            int index = 0;
            System.out.println("6. Create Snapshots for the Dockerfile");
            for (RevCommit commit : historyOfFile) {
                Snapshot snapshot = getDockerfileFromCommit(commit, repository, dockerPath, repoFolderName);
                if (index == 0) {
                    dockerfile.addSnapshots(snapshot,
                            true,
                            index,
                            Long.parseLong(githubId),
                            commit,
                            repository,git);
                } else {
                    dockerfile.addSnapshots(snapshot,
                            false,
                            index,
                            Long.parseLong(githubId),
                            commit,
                            repository, git);
                }
                index++;
            }

            System.out.println("7. Set Dates for the Snapshots");

            for (int i = 0; i < dockerfile.getDockerfileSnapshots().size(); i++) {
                Snapshot snapshot = dockerfile.getDockerfileSnapshots().get(i);
                if (i == 0) {
                    long nowUnx = new Date().getTime() / 1000;
                    dockerfile.getDockerfileSnapshots().get(i).setFromDate(snapshot.getCommitDate());
                    dockerfile.getDockerfileSnapshots().get(i).setToDate(nowUnx);
                } else {
                    Snapshot olderSnapshot = dockerfile.getDockerfileSnapshots().get(i - 1);
                    dockerfile.getDockerfileSnapshots().get(i).setFromDate(snapshot.getCommitDate());
                    dockerfile.getDockerfileSnapshots().get(i).setToDate(olderSnapshot.getCommitDate());
                }
            }

            System.out.println("8. Map Diffs with Snapshots");
            if (dockerfile.getDockerfileSnapshots().size() > 0) {
                for (int i = 0; i < dockerfile.getDockerfileSnapshots().size(); i++) {
                    Diff prevDiff;
                    Diff nextDiff;
                    boolean foundFlag = false;

                    while (!foundFlag) {
                        if (dockerfile.getDockerfileSnapshots().size() == 1) {
                            prevDiff = DiffProcessor.getDiff(null, dockerfile.getDockerfileSnapshots().get(0));
                            nextDiff = null;
                            dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                            foundFlag = true;
                        } else {
                            if (i == 0 && dockerfile.getDockerfileSnapshots().size()> (i+1)) {
                                prevDiff = DiffProcessor.getDiff(null, dockerfile.getDockerfileSnapshots().get(i));
                                nextDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i), dockerfile.getDockerfileSnapshots().get(i+1));
                                dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                foundFlag = true;
                            } else {
                                if(i>0 && dockerfile.getDockerfileSnapshots().size()> (i+1)){
                                    prevDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i-1), dockerfile.getDockerfileSnapshots().get(i));
                                    nextDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i), dockerfile.getDockerfileSnapshots().get(i+1));
                                    dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                    foundFlag = true;
                                }else{
                                    if(i>0){
                                        prevDiff = DiffProcessor.getDiff(dockerfile.getDockerfileSnapshots().get(i-1), dockerfile.getDockerfileSnapshots().get(i));
                                        nextDiff = null;
                                        dockerfile.getDockerfileSnapshots().get(i).setNewAndOldDiff(prevDiff,nextDiff);
                                        foundFlag = true;
                                    }
                                }
                            }
                        }
                    }

                }
            }


            System.out.println("## HIBERNATESERVICE: INSERT Dockerfile Object");
            HibernateService.createDockerfile(dockerfile);

            // JsonElement json = JsonPrinter.getJsonObject(dockerFile, String.valueOf(dockerFile.repo_id));
            //  System.out.println(JsonPrinter.getJsonString(json));
            // getDockerfileFromCommit(historyOfFile.get(1),repository,dockerPath,repoFolderName);
            System.out.println("1##################################################################################################");
            //   showDiffFilesOfHistory(historyOfFile, repository,git);
            System.out.println("2##################################################################################################");
            // DiffProcessor.showDiffbetweenTwoFiles(historyOfFile.get(1), historyOfFile.get(0), dockerPath, repository, git);
            //    CommitProcessor.getChangedFilesWithinCommit(historyOfFile.get(0), repository);


            repository.close();
            git.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Snapshot getDockerFile(String localPath, String dockerPath, String filename) throws IOException {
        DockerParser dockerParser = new DockerParser(localPath, dockerPath);
        Snapshot dockerfileSnapshot;
        if (filename == null) {
            dockerfileSnapshot = dockerParser.getDockerfileObject();
        } else {
            dockerfileSnapshot = dockerParser.getDockerfileObject(filename);
        }

        // System.out.println(dockerParser.toString());
        return dockerfileSnapshot;
    }

    public static Snapshot getDockerFile(String localPath, String filename) throws IOException {
        DockerParser dockerParser = new DockerParser(localPath);
        Snapshot dockerfileSnapshot;
        if (filename == null) {
            dockerfileSnapshot = dockerParser.getDockerfileObject();
        } else {
            dockerfileSnapshot = dockerParser.getDockerfileObject(filename);
        }

        System.out.println(dockerParser.toString());
        return dockerfileSnapshot;
    }

    public static Snapshot getDockerfileFromCommit(RevCommit revCommit, Repository repository, String dockerPath, String localPath) throws IOException {
        // a RevWalk allows to walk over commits based on some filtering that is defined
        System.out.println("6.1 Find File of Commit (getDockerfileFromCommit)");
        Snapshot snapshot = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(revCommit.getId());
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            File tempDockferile = null;
            String tempname = null;
            String filename = null;
            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(dockerPath));
                if (!treeWalk.next()) {

                    // return snapshot = getDockerFile(localPath, dockerPath, tempname);
                    throw new IllegalStateException("Did not find expected file 'Dockerfile'");
                }
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                Random ran = new Random();
                filename = "tempDock";
                tempname = localPath + "/" + filename;
                tempDockferile = new File(tempname);
                FileOutputStream fop = new FileOutputStream(tempDockferile);
                loader.copyTo(fop);
                fop.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            revWalk.dispose();
            System.out.println("6.2 Create Snapshot from File (getDockerfileFromCommit)");
            snapshot = getDockerFile(localPath, dockerPath, tempname);
            if (tempDockferile.exists()) {
                tempDockferile.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return snapshot;
    }

    public void getGitHubRepoInfos(String repo_name, String dockerpath) throws Exception {
        GithubRepository githubRepo = GitHubMinerService.getGitHubRepository(GITAPI + REPOS + repo_name);
        System.out.println("REPO INFO" + githubRepo.name + githubRepo.created_at.toString());

        String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(githubRepo.id);
        String repoFolderNameDotGit = repoFolderName + "\\.git";

        GitCloner gitCloner = new GitCloner();
        gitCloner.cloneRepository(githubRepo.git_url, repoFolderName);

        GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
        Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);


        Git git = new Git(repository);
        List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit, dockerpath);
        historyOfFile.size();
    }

    public static void diffExample() {
        Diff diff = new Diff();
        diff.id = Long.valueOf(1213123121);
        diff.setIns(3111);
        diff.setDel(1122);
        diff.setCommitDate(1312122212);

        HibernateService.upateDiff(diff);
    }

    public static void snapExample() {
        Dockerfile dockerfile = new Dockerfile();

        Snapshot snap = new Snapshot();
        snap.setId(Long.valueOf(66666666));
        snap.setChangeType("MOUT");
        snap.setDel(2);
        snap.setIns(1);
        snap.setStarCount(231231);
        snap.setCommitDate(Long.valueOf(666666));
        snap.setFromDate(Long.valueOf(12121));
        snap.setToDate(Long.valueOf(213123));
        snap.setImageIsAutomated(false);
        snap.setImageIsOffical(false);
        snap.setInstructions(23);

        From from = new From(snap, "testus");
        List<String> params = new ArrayList<>();
        params.add("44");
        params.add("44");
        params.add("44");
        params.add("44");

        Run run = new Run(snap, "mut", params);

        snap.from = from;
        snap.runs.add(run);

        HibernateService.createDockerfile(dockerfile);

        Snapshot snap1 = new Snapshot();
        snap1.setId(Long.valueOf(66666667));
        snap1.setChangeType("MOUT");
        snap1.setDel(2);
        snap1.setIns(1);
        snap1.setStarCount(231231);
        snap1.setCommitDate(Long.valueOf(666666));
        snap1.setFromDate(Long.valueOf(12121));
        snap1.setToDate(Long.valueOf(213123));
        snap1.setImageIsAutomated(false);
        snap1.setImageIsOffical(false);
        snap1.setInstructions(23);

        From from1 = new From(snap1, "testus");
        List<String> params1 = new ArrayList<>();
        params.add("5");
        params.add("5");
        params.add("5");
        params.add("5");

        Run run1 = new Run(snap1, "mut", params);

        snap1.from = from1;
        snap1.runs.add(run1);

        HibernateService.createDockerfile(dockerfile);
    }

}
