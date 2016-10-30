package dockalyzer.services;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ImageSearchResult;

import java.util.List;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class DockerService {

    public boolean isContainerOfficial(String image) throws DockerCertificateException, DockerException, InterruptedException {
        final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
        List<ImageSearchResult> results =  dockerClient.searchImages(image) ;
       // System.out.println("Name of the result" + results.get(0).isAutomated());
       // final List<Container> containers = dockerClient.listContainers(DockerClient.ListContainersParam.allContainers());
      //  System.out.println("Name of the Container" + containers.get(0).image());
        return results.get(0).isOfficial();
        // final String[] command = {"run", "--rm", "-i","lukasmartinelli/hadolint", "<" ," C:\\Users\\salizumberi-laptop\\workspace\\dockerlinter\\Dockerfile"};
    }


}
