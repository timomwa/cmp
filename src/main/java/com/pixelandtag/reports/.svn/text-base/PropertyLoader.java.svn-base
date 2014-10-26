package com.inmobia.axiata.reports;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertyLoader {
     public static Properties getPropertyFile(String filename)
    {
        // Load it and initialize the provider instance
        //Category log= Category.getRoot();
        Properties prop=new Properties();
        InputStream inputStream=null;;
        String path;
        try{
            path=System.getProperty("user.dir")+"/"+filename;
            inputStream= new FileInputStream(path);

          }
        catch(Exception e)
        {
            URL urlpath = new String().getClass().getResource(filename);
            try{
                inputStream = new FileInputStream(urlpath.getPath());
            } catch(Exception exb)
                {
                }
        }
        try{
            if (inputStream!=null) {
                prop.load(inputStream);
                inputStream.close();
            }
        } catch(Exception e)
        {
            System.out.println(e);
        }
      return prop;
    }
  
  
  

}
