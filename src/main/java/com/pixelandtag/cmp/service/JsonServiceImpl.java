package com.pixelandtag.cmp.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonServiceImpl implements JsonService {
	
	
	 /**
     * @see JsonService#deserialize(Class, String)
     */
    @Override
    public <T> List<T> deserialize(Class<T> clazz, String data) {
        return this.desiralizer(clazz, data, "rows");
    }
    
    /**
     * @see JsonService#deserialize(Class, String, String)
     */
    @Override
    public <T> List<T> deserialize(Class<T> clazz, String data, String rootNode) {
        return this.desiralizer(clazz, data, rootNode);
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> desiralizer(Class<T> clazz, String data, String rootNode) {
        
        JSONObject jsonObject = (JSONObject) net.sf.json.JSONSerializer.toJSON(data); 
        JSONArray jsonArray = jsonObject.getJSONArray(rootNode);
        
        JSONObject rec;
        List<T> objects = new ArrayList<T>();
        
        for(int i = 0 ; i < jsonArray.size() ; i++){
            rec = jsonArray.getJSONObject(i);
            objects.add((T)JSONObject.toBean(rec, clazz));
            
        }
        
        return objects;
        
    }

}
