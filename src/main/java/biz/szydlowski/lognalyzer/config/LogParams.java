/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.lognalyzer.config;



import static biz.szydlowski.loganalyzer.WorkingObjects.logCrawlerApiList;
import biz.szydlowski.loganalyzer.api.logApi;
import biz.szydlowski.loganalyzer.api.logCrawlerApi;
import biz.szydlowski.utils.OSValidator;
import biz.szydlowski.utils.template.TemplateFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
 * @author dominik
 */
public class LogParams {
  
   private List<logApi> logApiList = new ArrayList<>();

   private String _setting="setting/log-to-zabbix.xml";  
   
    static final Logger logger =  LogManager.getLogger(LogParams.class);
    
       
     /** Konstruktor pobierający parametry z pliku konfiguracyjnego "config.xml"
     */
     public LogParams(){
        
         if (OSValidator.isUnix()){
              String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                   absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
              _setting = absolutePath + "/" + _setting;
         }
  
         TemplateFile _LogTemplate = new  TemplateFile("default");    
   
        Properties filenames = new LogCrawlerParamsFiles().getProperties();
        Set<Object> keys = getAllKeys(filenames);
        for(Object k:keys){
            String key = (String)k; 
            String set_filename = getPropertyValue(filenames, key);
            if (OSValidator.isUnix()){
              String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                   absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
               set_filename = absolutePath + "/setting/" + set_filename;
            } else {
                set_filename =  "setting/" + set_filename;
            }
            addPropsFromFile(set_filename);
            
            //logger.debug("props size = "  + props.size());
        }
        
        
         for (String file : _LogTemplate.getFilenames()){
            addPropsFromFile(file);
        }
        
        logger.info("Total props size = "  + logApiList.size());
     }
   
     private void addPropsFromFile(String filename){               
        
        try {
                        
		File fXmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
               
                String prefix1 = doc.getElementsByTagName("params").item(0).getAttributes().getNamedItem("prefix").getNodeValue();
                                         
		logger.info("Read logAnalyze " + filename);
                           
		NodeList  nList = doc.getElementsByTagName("logAnalyze");
                Node nNode;
                		 
		for (int temp = 0; temp < nList.getLength(); temp++) {
                
		   nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                          
                       String zabbix_server =""; 
                       String itemhost ="";
                       String prefix2 ="";    
                       String active ="true";//
                       
                       if (nNode.hasAttributes()){

                                NamedNodeMap  baseElmnt_attr = nNode.getAttributes();
                                for (int i = 0; i <  baseElmnt_attr.getLength(); ++i)
                                {
                                    Node attr =  baseElmnt_attr.item(i);

                                   if (attr.getNodeName().equalsIgnoreCase("zabbix_server")){
                                        zabbix_server =   nNode.getAttributes().getNamedItem("zabbix_server").getNodeValue();
                                    } else if (attr.getNodeName().equalsIgnoreCase("host")){
                                        itemhost =   nNode.getAttributes().getNamedItem("host").getNodeValue();
                                    } else if (attr.getNodeName().equalsIgnoreCase("active")){
                                        active =   nNode.getAttributes().getNamedItem("active").getNodeValue();
                                    } else if (attr.getNodeName().equalsIgnoreCase("prefix")){
                                        prefix2 =   nNode.getAttributes().getNamedItem("prefix").getNodeValue();
                                    }
                           
                                }
                        } else {
                        } 
                           Element eElementMain = (Element) nNode;
                      
                          
                          NodeList  nListItem = eElementMain.getElementsByTagName("item");
                                              
                          for (int it = 0; it < nListItem.getLength(); it++) {
                             
                               
                               Node nNodeItem = nListItem.item(it);
                               if (nNodeItem.getNodeType() == Node.ELEMENT_NODE) {
                                   Element eElementItem = (Element) nNodeItem;
                                   logApi _logApi = new logApi ();
                                   String itemKey = "default";
                                  
                                   NamedNodeMap attributes = eElementItem.getAttributes();
                                    for (int i = 0; i <  attributes .getLength(); ++i) {
                                          Node item = attributes.item(i);
                                          if (item.getNodeName().equals("key")){
                                               itemKey = item.getNodeValue();
                                          } else if (item.getNodeName().equals("active")){
                                               String tmp =  item.getNodeValue();
                                               if (tmp.equals("false")) _logApi.setToInactiveMode();
                                          }
                                          
                                   }
                                     
                                   
                                   logger.debug("Processing " + zabbix_server+"."+itemhost+"."+prefix1+prefix2+itemKey);
                                
                                   _logApi.setSettingFile(filename);
                                   _logApi.setAlias(zabbix_server+"."+itemhost+"."+prefix1+prefix2+itemKey);
                                   _logApi.setZabbixServerName(zabbix_server);
                                   _logApi.setZabbixKey(prefix1+prefix2+itemKey);
                                   _logApi.setZabbixHost(itemhost);
                                  
                                   if (active.equals("false")) _logApi.setToInactiveMode();
                                   
                                   String type = getTagValue("type", eElementItem);
                                   _logApi.setType(type.toLowerCase());
                                  
                                   if (type.equalsIgnoreCase("log")){
                                        String logInterface = getTagValue("logInterface", eElementItem);

                                       _logApi.setLogInterface(logInterface);
                                       _logApi.setStringRule(getTagValue("stringRule", eElementItem));
                                       
                                        NamedNodeMap stringRuleAttributes = eElementItem.getAttributes();
                                        for (int i = 0; i <   stringRuleAttributes.getLength(); ++i) {
                                              Node item =  stringRuleAttributes.item(i);
                                              if (item.getNodeName().equalsIgnoreCase("ignoreCase")){
                                                   _logApi.setIgnoreCase(item.getNodeValue());
                                              } else if (item.getNodeName().equals("regEx")){
                                                    _logApi.setRegExpression(item.getNodeValue());
                                              }

                                       }
                               
                                       boolean found=false;
                                       for (logCrawlerApi sys :  logCrawlerApiList){
                                           if (sys.getLogInterface().equals(logInterface )){
                                               _logApi.setCronExpression(sys.getCronExpression());
                                                _logApi.addErrorToConfigErrorList(sys.getErrorList());
                                               found=true;
                                               break;
                                           }
                                       }
                                       if (!found) {
                                           _logApi.addErrorToConfigErrorList("logInterface  not found " + logInterface );
                                           logger.error("logInterface not found " + logInterface );
                                       } 
                                   } else if (type.equalsIgnoreCase("log_embedded") || type.equalsIgnoreCase("log_inbuild")){
                                        String logInterface = getTagValue("logInterface", eElementItem);

                                       _logApi.setLogInterface(logInterface);
                                       _logApi.setStringRule(getTagValue("function", eElementItem));
                               
                                       boolean found=false;
                                       for (logCrawlerApi sys :  logCrawlerApiList){
                                           if (sys.getLogInterface().equals(logInterface )){
                                                _logApi.setCronExpression(sys.getCronExpression());
                                                _logApi.addErrorToConfigErrorList(sys.getErrorList());
                                               found=true;
                                               break;
                                           }
                                       }
                                       if (!found) {
                                           _logApi.addErrorToConfigErrorList("logInterface  not found " + logInterface );
                                           logger.error("logInterface  not found " + logInterface );
                                           System.exit(150);
                                           
                                       } 
                                       
                                   } else if (type.equalsIgnoreCase("MAINTENANCE") || type.equalsIgnoreCase("DISCOVERY") || type.equalsIgnoreCase("DISCOVERY_STATIC")){
                                           StringBuilder function = new StringBuilder();
                                            //  _logApi.setStringRule(getTagValue("function", eElementItem));
                                           NodeList nListCmd =  eElementItem .getElementsByTagName("function");

                                           for (int replaceIndx = 0; replaceIndx < nListCmd.getLength(); replaceIndx++) {
                                                  NodeList nListReplace = nListCmd.item(replaceIndx).getChildNodes();
                                                  Node nValue = (Node) nListReplace.item(0);
                                                  function .append(nValue.getNodeValue());
                                           }

                                       _logApi.setStringRule(function.toString());                               
                                       
                                      
                                        _logApi.setCronExpression(getTagValue("cron_expression", eElementItem));
                                   }
                                   
                                   logApiList.add(_logApi);
                               
                               } //node item
                              
                          }  
                     
                    
		   }
		}
                
                logger.debug("Read log-to-zabbix done");
       
                
         }  catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {            
                logger.fatal("log-to-zabbix::XML Exception/Error:", e);
                System.exit(-1);
				
	  }
    }
    

  
    private static String getTagValue(String sTag, Element eElement) {
	
        try {
            NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
            Node nValue = (Node) nlList.item(0);
            return nValue.getNodeValue();
        } catch (Exception e){
            if (!sTag.equals("type")){
                logger.error("getTagValue error " + sTag + " " + e);
                return "ERROR";
            } else {
                return "log";
            }
        }
 
   }
   
   public Set<Object> getAllKeys(Properties prop){
        Set<Object> keys = prop.keySet();
        return keys;
    }

    public String getPropertyValue(Properties prop, String key){
        return prop.getProperty(key);
    }
  
  
    public List<logApi> getLogApiList(){
          return logApiList;
    }
 
}