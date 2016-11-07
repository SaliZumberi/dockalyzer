package dockalyzer.app;

import java.io.*;
import java.util.*;
import dockalyzer.models.DockerFile;
import dockalyzer.services.DatabaseService;
import dockalyzer.tools.dockerparser.DockerParser;
import dockalyzer.tools.githubminer.DiffProcessor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;

import dockalyzer.models.GithubRepository;
import dockalyzer.services.GitHubMinerService;
import dockalyzer.tools.githubminer.GitCloner;
import dockalyzer.tools.githubminer.GitHistoryFilter;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class App {

    private final static String GITAPI = "https://api.github.com/";
    private final static String REPOS = "repos/";
    private final static String LOCAL_REPO_FOLDER = "github_repos/";


    private final static ArrayList<Integer> months = new ArrayList<>();

    public App() {
    }

    public static void main(String[] args) throws Exception {
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

        String localPath = "github_repos/56803681";
        String dockerPath= "dockerfiles/fedora/fedora-22/fedora-22-base/Dockerfile";
       // printDockerFile(localPath,dockerPath);
        saveDockerObject("50925425","git://github.com/chrisdunelm/grpc.git", "tools/dockerfile/distribtest/python_ubuntu1510_x64/Dockerfile");
    }

    public static void showDiffFilesOfHistory(List<RevCommit> historyOfFile, Repository repository, Git git) throws IOException, GitAPIException {
        for (int i = 0; i < historyOfFile.size() - 1; i++) {
            DiffProcessor.showDiffFilesOfTwoCommits(historyOfFile.get(i), historyOfFile.get(i + 1), repository, git);
        }
    }

    public static void saveDockerObject(String githubId, String gitURL, String dockerPath){
            try {
                String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(githubId);
                String repoFolderNameDotGit = repoFolderName + "\\.git";

                GitCloner gitCloner = new GitCloner();
                gitCloner.cloneRepository(gitURL, repoFolderName);

                GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
                Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);

                Git git = new Git(repository);

                List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit, dockerPath);

                printDockerfileFromCommit(historyOfFile.get(0), repository,dockerPath);
                repository.close();
                git.close();
            } catch (Exception e) {
            }
        }


    public static void printDockerFile(String localPath,String dockerPath) throws IOException {
        DockerParser dockerParser = new DockerParser(localPath,dockerPath);

        File flatDockerFile = dockerParser.getFlatDockerFile();
        System.out.println("lets print it");
        //dockerParser.printLines(flatDockerFile);
        DockerFile test =  dockerParser.getDockerfileObject();

        System.out.println(dockerParser.toString());
    }

    public static void printDockerfileFromCommit(RevCommit revCommit, Repository repository, String dockerPath) throws IOException {

        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(revCommit.getId());
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            System.out.println("Having tree: " + tree);

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(dockerPath));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file 'Dockerfile'");
                }
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                File file = new File("testCommit.txt");
                FileOutputStream fop = new FileOutputStream(file);
                loader.copyTo(fop);
                fop.close();
            }
            revWalk.dispose();
        }
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

    public void databaseExample() {

        // Create two Students
        //DatabaseService.create(1, "Alice", 22); // Alice will get an id 1


        // Update the age of Bob using the id
        DatabaseService.create("Hello", 22);
        DatabaseService.create("sda", 22);
        DatabaseService.create("asda", 22);

        DatabaseService.upate(2, "Sali", 25);

        // Delete the Alice from database
        DatabaseService.delete(2);

        // Print all the Students
 /*       List<Student> students = DatabaseService.readAll();
        if (students != null) {
            for (Student stu : students) {
                System.out.println(stu);
            }
        }*/

        // NEVER FORGET TO CLOSE THE SESSION_FACTORY
        DatabaseService.SESSION_FACTORY.close();
    }


}
