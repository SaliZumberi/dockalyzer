package dockalyzer.models;

import java.security.acl.Owner;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by salizumberi-laptop on 19.10.2016.
 */
public class GithubRepository {

    public String git_url;
    public long id;
    public String name;
    public String full_name;
    public String created_at;
    public long size;
    public long subscribers_count;
    public long network_count;
    public long open_issues;
    public long watchers_count;
    public long stargazers_count;
    public long forks_count;
    public long open_issues_count;
    public dockalyzer.models.Owner owner;

}


