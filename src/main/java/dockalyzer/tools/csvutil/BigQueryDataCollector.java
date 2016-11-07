package dockalyzer.tools.csvutil;

import dockalyzer.models.GithubRepository;
import dockalyzer.services.GitHubMinerService;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.List;

/**
 * Created by salizumberi-laptop on 05.11.2016.
 */
public class BigQueryDataCollector {

    private final static String GITAPI = "https://api.github.com/";
    private final static String REPOS = "repos/";
    private final static String LOCAL_REPO_FOLDER = "github_repos/";

    public static void main(String[] args) throws Exception {
        System.out.println("hello world");
        CsvListReader reader = new CsvListReader(new FileReader(new File("bigquerydata\\data_1.csv")), CsvPreference.STANDARD_PREFERENCE);
        CsvListWriter writer = new CsvListWriter(new FileWriter(new File("bigquerydata\\data_11.csv"),true), CsvPreference.STANDARD_PREFERENCE);

        List<String> columns;
        int i = 0;
        while ((columns = reader.read()) != null) {
            System.out.print("Index is: " + i);
            if (i>4600){
                try {
                    System.out.println("Now we have to wait unti rate limit is filled");
                    Thread.sleep(1000 * 60 * 50);
                    System.out.println("okay 60 min are over, lets do another 5000");
                    i= 0;
                } catch (InterruptedException ex) {}
            }
            try{
                //new file --> id[0], repo_name[1], path to dockerfile[2], git_url[3], created at[4],
                //network_count , open_issues, open_issues_count, owner.id, owner.type, forks_count, watchers_count
                //stargazers_count, subscribers_count, size
                String[] split = columns.get(0).split(";");
                String key = split[0];

                GithubRepository githubRepo = GitHubMinerService.getGitHubRepository(GITAPI + REPOS + key);
                columns.add(0, String.valueOf(githubRepo.id));
                columns.add(githubRepo.git_url);
                columns.add(String.valueOf(githubRepo.created_at));
                columns.add(String.valueOf(githubRepo.network_count));
                columns.add(String.valueOf(githubRepo.open_issues));
                columns.add(String.valueOf(githubRepo.open_issues_count));
                columns.add(String.valueOf(githubRepo.owner.id));
                columns.add(String.valueOf(githubRepo.owner.type));
                columns.add(String.valueOf(githubRepo.forks_count));
                columns.add(String.valueOf(githubRepo.watchers_count));
                columns.add(String.valueOf(githubRepo.stargazers_count));
                columns.add(String.valueOf(githubRepo.subscribers_count));
                columns.add(String.valueOf(githubRepo.size));
                columns.add(String.valueOf(githubRepo.id));

                System.out.print(" - Output2: " + columns + "\n");
                writer.write(columns);
            }catch (Exception e){

            }
            i++;
        }
        reader.close();
        writer.close();

    }

}
