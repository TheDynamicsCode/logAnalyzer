/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.lognalyzer.config;


import biz.szydlowski.utils.OSValidator;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author szydlowskidom
 */
public class LogCrawlerParamsFiles  {
  
    private Properties prop = new Properties();
    
    private String _setting="setting/log-to-zabbix-files.props";
    
     static final Logger logger =  LogManager.getLogger(LogCrawlerParamsFiles.class);

       
     /** Konstruktor pobierający parametry z pliku konfiguracyjnego "config.xml"
     */
    public LogCrawlerParamsFiles (){
         
         if (OSValidator.isUnix()){
              String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                   absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
              _setting = absolutePath + "/" + _setting;
         }
            
       
	InputStream input = null;

	try {

		input = new FileInputStream(_setting );
		// load a properties file
		prop.load(input);


	} catch (IOException ex) {
            logger.error(ex);
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

  
    }


  public Properties getProperties (){
          return  prop;
  }
}