package dockalyzer.process.output;

import com.github.dockerjava.core.dockerfile.Dockerfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Created by salizumberi-laptop on 07.11.2016.
 */
public class JsonPrinter {

    public static JsonElement  getDockerfileJson(Dockerfile dockerfile, String localPath){

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        String json = null;
        try {
            //Convert object to JSON string and save into file directly
            mapper.writeValue(new File(localPath + "/" + "dockerfile.json"), dockerfile);

            //Convert object to JSON string
            json = mapper.writeValueAsString(dockerfile);

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        return jp.parse(json);
    }

    public static String getJsonString(JsonElement jsonElement){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }

    public static JsonElement getJsonObject(Object object, String objectName){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        String json = null;
        try {
            //Convert object to JSON string and save into file directly
            mapper.writeValue(new File("jsons" + "/" + objectName +".json"), object);

            //Convert object to JSON string
            json = mapper.writeValueAsString(object);

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        return jp.parse(json);
    }
}
