package dockalyzer.tools.githubminer;

import java.io.File;

import org.eclipse.jgit.api.Git;

public class GitCloner {

    public void cloneRepository(String remotePath, String localPath) throws Exception {
        File repositoryFile = new File(localPath);
        if(repositoryFile.exists() && repositoryFile.isDirectory()) {
            System.out.println("THIS REPO EXISTS");
        }else{
            Git git = Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath)).call();
            git.close();
        }
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

}
