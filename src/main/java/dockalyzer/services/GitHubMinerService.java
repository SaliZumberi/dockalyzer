package dockalyzer.services;

import com.google.gson.Gson;

import dockalyzer.models.GithubRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.io.Reader;

/**
 * Created by salizumberi-laptop on 19.10.2016.
 */
public class GitHubMinerService {

    public GithubRepository getGitHubRepository(String path){
        Gson gson = new Gson();
        GithubRepository repository = gson.fromJson(getJsonRepository(path).toString(),GithubRepository.class);
        return repository;
    }


    public JSONObject getJsonRepository(String path){
        JSONObject json = null;
        try {
            json = readJsonFromUrl(path);
          //  System.out.println(json.get("git_url"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


}
