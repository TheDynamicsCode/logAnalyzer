/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer;

/**
 *
 * @author szydlowskidom
 */

import static biz.szydlowski.loganalyzer.WorkingObjects.*;
import biz.szydlowski.loganalyzer.api.logApi;
import biz.szydlowski.lognalyzer.config.LogParams;
import biz.szydlowski.lognalyzer.config.LogsInterfaces;
import biz.szydlowski.lognalyzer.config.WebParams;
import biz.szydlowski.lognalyzer.timers.ActiveAgentTask;
import biz.szydlowski.lognalyzer.web.WebServer;
import biz.szydlowski.utils.Constans;
import biz.szydlowski.utils.Memory;
import biz.szydlowski.utils.OSValidator;
import biz.szydlowski.utils.WorkingStats;
import biz.szydlowski.utils.tasks.MaintananceTask;
import biz.szydlowski.utils.tasks.TasksWorkspace;
import static biz.szydlowski.utils.tasks.TasksWorkspace.absolutePath;
import biz.szydlowski.utils.tasks.UpdateServer;
import biz.szydlowski.utils.tasks.WatchDogTask;
import biz.szydlowski.zabbixmon.ZabbixServer;
import biz.szydlowski.zabbixmon.ZabbixServerApi;
import java.io.File;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class LogAnalyzerDaemon implements Daemon {
    static {
        try {
            System.setProperty("log4j.configurationFile", getJarContainingFolder(LogAnalyzerDaemon.class)+"/setting/log4j/log4j2.xml");
        } catch (Exception ex) {
        }
    }
    static final Logger logger =  LogManager.getLogger(LogAnalyzerDaemon.class);
    
    private static boolean TESTING = false;
    private static boolean stop = false;
    private WebServer _WebServer = null;

     
    private Timer ActiveAgentTimer = new Timer("ActiveAgentProcessing", true);
  
    private Timer WatchdogTimer = new Timer("Watchdog", true);
    private Timer MaintenanceTimer = new Timer("MaintenanceTask", true);
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
    public static List <ZabbixServerApi> zabbixServerApiList = null;
     
    private int ACTIVE_AGENT_TIME = 5000;
    public static String APP_NAME="DEFAULT";       
    
    public LogAnalyzerDaemon(){
      
    }
   
        
    public LogAnalyzerDaemon(boolean test, boolean win){
          if (TESTING || test || win){
            if (!win) System.out.println("****** TESTING MODE  ********"); 
            else System.out.println("****** WINDOWS MODE  ********"); 
            try {
                jInit();
                start();
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex);
            }
        }
    }
   
     public void jInit() {
         
        
           if (OSValidator.isUnix()){
              absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                   absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"))+"/";
           } else {
               absolutePath="";
           }
           
           new UpdateServer("http://www.update.szydlowski.biz/software/logcrawler",Version.getBuild(),Version.getAgentVersion()).start();
            
           
           TasksWorkspace.start(Version.DEV_VERSION_EXPIRE_DATE, true);           
           WorkingStats.start();  
                   
           printStarter();
               
           logCrawlerApiList = new LogsInterfaces().getLogCrawlerApiList();
            
           logApiList = new LogParams().getLogApiList();
           
           for (int x=0; x<logCrawlerApiList.size(); x++) {
               for (int y=0; y<logApiList.size(); y++){

                    if (!logApiList.get(y).getType().equals("maintenance")){
                         if (logApiList.get(y).getLogInterface().equals(logCrawlerApiList.get(x).getLogInterface())){
                             logApiList.get(y).setCrawlerID(x);
                         }

                    }
               }
           }

           zabbixServerApiList = new ZabbixServer(absolutePath +"setting/zabbix-server.xml").getZabbixServerApiList();
                  
                 
           
           _LogQuartz = new LogQuartz();
            
            unique_hostname = new HashMap<>();
            zabbixServerApiList.forEach((zabbixServerApi) -> {
                setUniqZabbixHostnameForAgent(zabbixServerApi.getServerName());
            }); 
       
           
            WatchdogTimer.schedule(new WatchDogTask(), 10000, 2000);     
            ActiveAgentTimer.schedule(new ActiveAgentTask(), 1000, ACTIVE_AGENT_TIME);
            MaintenanceTimer.schedule(new MaintananceTask(), 60000, 60000);
            
            WebParams _WebParams = new WebParams();
            if (_WebParams.isWebConsoleEnable()){ 
                allowedConn = _WebParams.getAllowedConn();
                _WebServer = new WebServer(_WebParams.getWebConsolePort());
                _WebServer.setMaxConnectionCount(_WebParams.getWebMaxConnectionCount());
                _WebServer.start();
            } 
            
            Memory.start();
 
    }
    
      @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
            System.out.println("**** Daemon init *****");
                     
            handleCmdLine(daemonContext.getArguments());      
            
            String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
               absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
            
           
            jInit(); 
            
            logger.debug("Current path " + absolutePath);
            
   }

    @Override
    public void start() throws Exception { 
         logger.info("Starting daemon");
         _LogQuartz.doJob();
         logger.info("Started daemon");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stoppping daemon");
        
        _WebServer.stopSever();
    
     
        ActiveAgentTimer.cancel();
        MaintenanceTimer.cancel();
             
        logger.info("Stopped daemon");
    }
   
    @Override
    public void destroy() { 
         logger.info("Destroying daemon");
         _LogQuartz = null;
         ActiveAgentTimer = null;
         MaintenanceTimer = null;
    }
     //https://support.google.com/gsa/answer/6316721?hl=en
     public static void start(String[] args) {
        System.out.println("start");
        LogAnalyzerDaemon logCrawlerDaemon = new LogAnalyzerDaemon(false, true);
        
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
 
    public static void stop(String[] args) {
        System.out.println("stop");
        stop = true;
     
        logger.info("Stoppping daemon");
          
        logger.info("Stopped daemon");  
        
        System.exit(0);
                
    }
 
   
    
    public static void main(String[] args)  {
         if (TESTING){
             LogAnalyzerDaemon  logCrawlerDaemon = new LogAnalyzerDaemon(true, false);
         }  else {
             if (args.length>0){
                 if (args[0].equalsIgnoreCase("testing")){
                     LogAnalyzerDaemon  logCrawlerDaemon = new LogAnalyzerDaemon(true, false);
                 }
                 
             }
         }     
    }
    
    
    private static void printStarter(){
        logger.info(Constans.STARTER);
        logger.info(new Version().getAllInfo());
    }
   
    public void handleCmdLine(String[] args) {
      
         for (int i = 0; i < args.length; i++) {
           String arg = args[i];
           if (arg.regionMatches(0, "-", 0, 1))  {
            try {
               switch (arg.charAt(1)) { 
               case 'n':
                 i++;
                 break;
                 
               case 'a' :
                  i++;
                  APP_NAME = args[i];
                  break;             

               default:
                 //printUsage(language);
               }
             }
             catch (ArrayIndexOutOfBoundsException ae)  {
              // printUsage(language);
             }
           }
        }
     }
    
    private void setUniqZabbixHostnameForAgent(String zabbix_host){
                     
        String ag="";
        String name="";
        
        for (logApi log : logApiList){
             ag = zabbix_host+"."+log.getZabbixHost();
             name = log.getZabbixServerName();
             
             unique_hostname.put(ag, true);
         
        }
        
    }
    
    public static String getJarContainingFolder(Class aclass) throws Exception {
          CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

          File jarFile;

          if (codeSource.getLocation() != null) {
            jarFile = new File(codeSource.getLocation().toURI());
          }
          else {
            String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
            jarFile = new File(jarFilePath);
          }
          return jarFile.getParentFile().getAbsolutePath();
     }


}