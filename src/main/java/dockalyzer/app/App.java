package dockalyzer.app;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import dockalyzer.services.DatabaseService;
import dockalyzer.tools.githubminer.CommitProcessor;
import dockalyzer.tools.githubminer.DiffProcessor;
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

import dockalyzer.models.GithubRepository;
import dockalyzer.services.BigQueryService;
import dockalyzer.services.GitHubMinerService;
import dockalyzer.tools.csvutil.CSVUtils;
import dockalyzer.tools.githubminer.GitCloner;
import dockalyzer.tools.githubminer.GitHistoryFilter;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class App {

    private final static String GITAPI = "https://api.github.com/";
    private final static String REPOS = "repos/";
    private final static String LOCAL_REPO_FOLDER = "github_repos/";


    private final static ArrayList<Integer> months = new ArrayList<>();

    public App() {
    }





    public static void main(String[] args) throws Exception {
        //String repo_name = "dockerfile/redis";
       // String repo_name = "centic9/jgit-cookbook";
        String repo_name = "probr/probr-core";

        List<String> repo_names_example = new ArrayList<>();
        repo_names_example.add("dockerfile/redis");
        repo_names_example.add("centic9/jgit-cookbook");
        repo_names_example.add("probr/probr-core");

        App repositoryMinerApp = new App();

        GithubRepository githubRepo = GitHubMinerService.getGitHubRepository(GITAPI + REPOS + repo_name);

        String repoFolderName = LOCAL_REPO_FOLDER + String.valueOf(githubRepo.id);
        String repoFolderNameDotGit = repoFolderName +"\\.git";

        GitCloner gitCloner = new GitCloner();
        gitCloner.cloneRepository(githubRepo.git_url, repoFolderName);

        GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
        Repository repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);

        //gitHistoryFilter.printDiffTest("test1","test2");

        Git git = new Git(repository);
        List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit);
        historyOfFile.size();
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

        CommitProcessor.getChangedFilesWithinCommit(historyOfFile,repository);

    }

    public static void showDiffFilesOfHistory(List<RevCommit> historyOfFile, Repository repository, Git git ) throws IOException, GitAPIException {
        for (int i=0; i<historyOfFile.size()-1; i++){
            DiffProcessor.showDiffFilesOfTwoCommits(historyOfFile.get(i),historyOfFile.get(i+1),repository,git);
        }
    }


    public void databaseExample(){

        // Create two Students
        //DatabaseService.create(1, "Alice", 22); // Alice will get an id 1


        // Update the age of Bob using the id
        DatabaseService.create("Hello",22);
        DatabaseService.create("sda",22);
        DatabaseService.create("asda",22);

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
