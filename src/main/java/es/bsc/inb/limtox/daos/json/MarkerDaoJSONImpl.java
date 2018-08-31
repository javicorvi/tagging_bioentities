package es.bsc.inb.limtox.daos.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.bsc.inb.limtox.daos.MarkerDao;
import es.bsc.inb.limtox.model.Marker;
@Repository(value="markerDaoJSONImpl")
public class MarkerDaoJSONImpl extends GenericDaoJSONImpl<Marker> implements MarkerDao{
	@Override
    public List<Marker> findAll() {
		ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
		try {
			String json_string = unzipDictionary(env.getProperty("limtox.dictionary.liver_marker"));
			JsonNode rootNode = mapper.readTree(json_string);
			JsonNode data = rootNode.path("liver_marker");
			List<Marker> myObjects = Arrays.asList(mapper.readValue(data.toString(), Marker[].class));
			return myObjects;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
    }

}
