/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.lognalyzer.config;

import biz.szydlowski.loganalyzer.api.logCrawlerApi;
import biz.szydlowski.utils.OSValidator;
import biz.szydlowski.utils.WorkingStats;
import biz.szydlowski.utils.template.TemplateFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author szydlowskidom
 */
public class LogsInterfaces {
  

   private List<logCrawlerApi> logCrawlerApiList = new ArrayList<>();

   private String _setting="setting/logs-interfaces.xml";  
   
    static final Logger logger =  LogManager.getLogger(LogsInterfaces.class);
    
       
     /** Konstruktor pobierający parametry z pliku konfiguracyjnego "config.xml"
     */
    public LogsInterfaces (){
        
         if (OSValidator.isUnix()){
              String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                   absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
              _setting = absolutePath + "/" + _setting;
         }
         
         addCrowlerFromFile(_setting);
         
         TemplateFile _LogTemplate = new  TemplateFile("interfaces"); 
         
         for (String file : _LogTemplate.getFilenames()){
             addCrowlerFromFile(file);
        }
    }
    
    public void addCrowlerFromFile(String filename){ 
         
     
                  
         try {
                        
		File fXmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
             
		logger.debug("Read logs-crawler " + _setting);
                
                
                NodeList  nList = doc.getElementsByTagName("log");
                		 
		for (int temp = 0; temp < nList.getLength(); temp++) {
                        
                   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                          Element eElement = (Element) nNode;
                          
                          logCrawlerApi _logCrawlerApi = new logCrawlerApi ();
                     
                          
                          String cron_expression = getTagValue("cron_expression", eElement);

                          if (org.quartz.CronExpression.isValidExpression(cron_expression)){
                              _logCrawlerApi.setCronExpression(getTagValue("cron_expression", eElement));
                          } else { 
                              _logCrawlerApi.setCronExpression("0 0 0 * * ?"); 
                              _logCrawlerApi.addErrorToConfigErrorList("invalid cron expression " + cron_expression);
                               logger.error("invalid cron expression " + cron_expression);
                          }
                          
                          _logCrawlerApi.setLogInterface(getTagValue("logInterface", eElement));
                          _logCrawlerApi.setlogPath(getTagValue("path", eElement));
                           
                                             
                           if (eElement.getElementsByTagName("path").item(0).hasAttributes()){

                                NamedNodeMap  baseElmnt_attr = eElement.getElementsByTagName("path").item(0).getAttributes();
                                    for (int i = 0; i <  baseElmnt_attr.getLength(); ++i)
                                    {
                                        Node attr =  baseElmnt_attr.item(i);

                                        if (attr.getNodeName().equalsIgnoreCase("dateFormat")){
                                            logger.debug("dateFormat=" + attr.getNodeValue());
                                            _logCrawlerApi.setDateFormat(attr.getNodeValue());
                                        }

                                  }
                             }
                          WorkingStats.initLockForQueue();
                          logCrawlerApiList.add(_logCrawlerApi);
                                   
		   }
		}
                
                logger.debug("Readlogs-crawler done");
                                
         }  catch (ParserConfigurationException | SAXException | IOException e) {         
                logger.fatal("logs-crawler::XML Exception/Error:", e);
                System.exit(-1);
				
	  }
    }
  
    

  
  private static String getTagValue(String sTag, Element eElement) {
	try {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
            Node nValue = (Node) nlList.item(0);
            return nValue.getNodeValue();
        } catch (Exception e){
            logger.error("getTagValue error " + sTag + " " + e);
            return "ERROR";
        }
  }
  

  public List<logCrawlerApi> getLogCrawlerApiList (){
          return  logCrawlerApiList;
  }

}
