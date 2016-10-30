package dockalyzer.app;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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

public class App {
    GitHubMinerService gitHubMinerService = null;
    BigQueryService bigQueryService = null;

    public final static String GITAPI = "https://api.github.com/";
    public final static String REPOS = "repos/";

    Git git = null;
    Repository repository = null;

    public final static ArrayList<Integer> months = new ArrayList<>();
    public App() {
        initServices();
    }

    private void initServices() {
        this.gitHubMinerService = new GitHubMinerService();
        this.bigQueryService = new BigQueryService();

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
        GithubRepository githubRepo = repositoryMinerApp.gitHubMinerService.getGitHubRepository(GITAPI + REPOS + repo_name);

        String repoFolderName = String.valueOf(githubRepo.id);
        String repoFolderNameDotGit = repoFolderName +"\\.git";

        GitCloner gitCloner = new GitCloner();
        gitCloner.cloneRepository(githubRepo.git_url, repoFolderName);

        GitHistoryFilter gitHistoryFilter = new GitHistoryFilter();
        repositoryMinerApp.repository = gitHistoryFilter.getRepository(repoFolderNameDotGit);

        //gitHistoryFilter.printDiffTest("test1","test2");

        Git git = new Git(repositoryMinerApp.repository);
        repositoryMinerApp.git = git;

        List<RevCommit> historyOfFile = gitHistoryFilter.getDockerHistory(repoFolderNameDotGit);
        //repositoryMinerApp.findFile(historyOfFile.get(0));

        createMonths();
        classfiyDatesToMonths(getCommitDates(historyOfFile));
        exportDatesToCSV(getCommitDates(historyOfFile));
        months.forEach(value -> System.out.println("Summary: " +value ));













        //   repositoryMinerApp.showDiffFilesOfTwoCommits(historyOfFile.get(0), historyOfFile.get(1));
     //   repositoryMinerApp.showDiffbetweenTwoFiles(historyOfFile.get(0), historyOfFile.get(1), "Dockerfile");
    }

    private static void exportDatesToCSV(ArrayList<Date> datesArray) throws IOException {
        String csvFile = "commitdates.csv";
        FileWriter writer = new FileWriter(csvFile);
        CSVUtils.writeLine(writer, datesArray);

        //custom separator + quote
        //CSVUtils.writeLine(writer, Arrays.asList("aaa", "bb,b", "cc,c"), ',', '"');

        //custom separator + quote
       // CSVUtils.writeLine(writer, Arrays.asList("aaa", "bbb", "cc,c"), '|', '\'');

        //double-quotes
       // CSVUtils.writeLine(writer, Arrays.asList("aaa", "bbb", "cc\"c"));

        writer.flush();
        writer.close();
    }

    private static void classfiyDatesToMonths(ArrayList<Date> commitDates){
        for(int i=0; i<commitDates.size(); i++){
            addToMonth(getMonth(commitDates.get(i)));
        }
    }

    private static void addToMonth(int month){
        for (int i = 0; i < 12; i++) {
            if (i==month-1){
                months.set(i,months.get(i)+1);
            }
        }
    }

    private static ArrayList<Date> getCommitDates(List<RevCommit> historyOfFile) {
        ArrayList<Date> commitDates = new ArrayList<>();
        for(int i=0; i<historyOfFile.size(); i++){
            commitDates.add(new Date(historyOfFile.get(i).getCommitTime() * 1000L));
            System.out.println(getMonth(new Date(historyOfFile.get(i).getCommitTime() * 1000L)));
        }
        return commitDates;
    }

    private static void createMonths(){
        Integer month = new Integer(0);
        for (int i = 0; i < 12; i++) {
            months.add(month);
        }
    }

    private static int getMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH)+1;
    }


    private void showDiffbetweenTwoFiles(RevCommit oldCommit, RevCommit newCommit, String fileName) throws IOException, GitAPIException {
        System.out.println("******* Diff of two Commits of + " + fileName + " *******");
        RevTree newRevTree = this.generateRevTree(newCommit);
        RevTree oldRevTree = this.generateRevTree(oldCommit);

        AbstractTreeIterator oldTreeParser = prepareTreeParser(this.repository, oldRevTree);
        AbstractTreeIterator newTreeParser = prepareTreeParser(this.repository, newRevTree);

        // then the procelain diff-command returns a list of diff entries
        List<DiffEntry> diff = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                setPathFilter(PathFilter.create(fileName)).
                call();
        for (DiffEntry entry : diff) {
            System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
            try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                formatter.setRepository(this.repository);
                formatter.format(entry);
            }
        }
        System.out.println("*********************************************************" + '\n');
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, RevTree tree) throws IOException {
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

    public RevTree generateRevTree(RevCommit commit) {
        RevWalk walk = new RevWalk(this.repository);
        try {
            return walk.parseTree(commit.getTree().getId());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void showDiffFilesOfTwoCommits(RevCommit oldCommit, RevCommit newCommit) throws IOException {
        System.out.println("******* File which changed within this those two commits *******");
        RevWalk walk = new RevWalk(this.repository);

        RevTree newRevTree = generateRevTree(newCommit);
        RevTree oldRevTree = generateRevTree(oldCommit);

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
            System.out.println(diffText);
            out.reset();
        }
        System.out.println("****************************************************************"+ '\n');
    }

	private void findFile(String name, RevCommit commit) throws IOException {
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
}
