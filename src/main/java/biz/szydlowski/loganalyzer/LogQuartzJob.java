/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer;


import static biz.szydlowski.loganalyzer.WorkingObjects.logApiList;
import static biz.szydlowski.loganalyzer.WorkingObjects.logCrawlerApiList;
import biz.szydlowski.loganalyzer.rules.RulesRouer;
import biz.szydlowski.utils.WorkingStats;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;

/**
 *
 * @author szydlowskidom
 */
public class LogQuartzJob implements InterruptableJob {
    
 
     static final Logger logger =  LogManager.getLogger(LogQuartzJob.class);
   
    private volatile Thread  thisThread;    
    private JobKey   jobKey   = null; 
    private volatile boolean isJobInterrupted = false;
    private final int bufferSize = 8192;
    private boolean isInit=false;
     
  
    public LogQuartzJob (){
    }

    
     @Override
     public void execute(JobExecutionContext jeContext) throws JobExecutionException {
            
            thisThread = Thread.currentThread();
           // logger.info("Thread name of the current job: " + thisThread.getName());
             
                     
            jobKey = jeContext.getJobDetail().getKey();
            logger.info("Job " + jobKey + " executing at " + new Date());
            int id=0;
            String type="log";
            boolean stillExecuting=false;
            String s_return="";
            int lines=0;
            long diff=0; 
            boolean isError=false;
            Multimap<Integer, String> RulesHelper = ArrayListMultimap.create();
              
            try {

                   JobDataMap jdMap = jeContext.getJobDetail().getJobDataMap();
                   type = jdMap.get("type").toString();
                 
                  
                   try {
                        id = Integer.parseInt(jdMap.get("id").toString());
                   } catch (NumberFormatException e){
                       logger.error(e);
                   }
                   
                   if (type.equals("nolog")){
                           String ltype = logApiList.get(id).getType();
                           if (ltype.equalsIgnoreCase("MAINTENANCE")){
                               
                                logApiList.get(id).setIsNowExecuting(); 
                            
                                switch (logApiList.get(id).getStringRule()){   
                                   case "connError":
                                        s_return = ""+WorkingStats.getConnError();
                                        break;

                                   case "activeThreads":
                                        s_return = ""+WorkingStats.getActiveThreads();
                                        break; 

                                    case "javaUsedMemory":
                                        s_return = ""+WorkingStats.getJavaUsedMemory();
                                        break;

                                    case "okCount":
                                        s_return = ""+WorkingStats.getOkCount();
                                        break;

                                    case "errorCount":
                                        s_return = ""+WorkingStats.getErrorCount();
                                        break;

                                    case "testsCount":
                                        s_return = ""+WorkingStats.getTestsCount();
                                        break;

                                    case "uptime":
                                        s_return = ""+WorkingStats.getUptime();
                                        break; 

                                    case "idletime":
                                        s_return = ""+WorkingStats.getIdleTime();
                                        break;

                                    case "lockCount":
                                        s_return = ""+WorkingStats.getLockCount();
                                        break; 

                                    case "lockAttempt":
                                        s_return = ""+WorkingStats.getLockAttempt();
                                        break;

                                    case "zabbixProcessed":
                                        s_return = ""+WorkingStats.getZabbix_processed();
                                        break;

                                    case "zabbixFailed":
                                        s_return = ""+WorkingStats.getZabbix_failed();
                                        break;

                                    case "zabbixTotal":
                                        s_return = ""+WorkingStats.getZabbix_total();
                                        break;  

                                    case "autorestart":
                                        s_return = ""+WorkingStats.getAutorestartCount();
                                        break; 

                                    case "version":
                                        s_return = ""+Version.getAgentVersion();
                                        break;

                                    default  :
                                        s_return = "default";
                                        break;

                               }
                               WorkingStats.idlePlus();  
                               logApiList.get(id).setNormalReturnValue(s_return);
                               logApiList.get(id).setIsNowExecutingToFalse(); 
                               logApiList.get(id).setIsNowRefreshingToFalse();
                               logApiList.get(id).setReadyToSendToTrue();
                              
                                
                           } else if (ltype.equalsIgnoreCase("DISCOVERY")){
                                logApiList.get(id).setIsNowExecuting(); 
                            
                                switch (logApiList.get(id).getStringRule()){   
                                   case "setInterface":
                                     
                                        JSONObject data = new JSONObject();

                                        List<JSONObject> aray = new LinkedList<>();
                                        StringBuilder k = new StringBuilder();


                                        for(int i=0; i<logCrawlerApiList.size();i++) {

                                                JSONObject xxx = new JSONObject();

                                                k.delete(0, k.length());
                                                k.append("{#").append("INTERFACE").append("}"); 
                                                xxx.put(k.toString(), logCrawlerApiList.get(i).getLogInterface());

                                                aray.add(xxx);

                                        }  

                                        data.put("data", aray);
                                        s_return = data.toJSONString();
                                        break;

                                   case "setValue": 
                                        s_return = "done";
                                        break; 
                                   
                                   default  :
                                        s_return = "default";
                                        break;

                                    
                               }
                               WorkingStats.idlePlus();  
                               logApiList.get(id).setNormalReturnValue(s_return);
                               logApiList.get(id).setIsNowExecutingToFalse(); 
                               logApiList.get(id).setIsNowRefreshingToFalse();
                               logApiList.get(id).setReadyToSendToTrue(); 
                               
                       } else if (ltype.equalsIgnoreCase("DISCOVERY_STATIC")){
                                logApiList.get(id).setIsNowExecuting();
                                
                               String [] discovery = logApiList.get(id).getStringRule().split(";;");
                               String [] discovery_map = discovery[0].split(";");
                                                        
                               JSONObject data = new JSONObject();

                               List<JSONObject> aray = new LinkedList<>();
                               StringBuilder k = new StringBuilder();
                                        
                          
                              for(int j=1; j<discovery.length; j++) {      
                                   String [] keys_value =  discovery[j].split(";");
                                   JSONObject xxx = new JSONObject();

                                    if (keys_value.length!=discovery_map.length){
                                        logger.error("Consist error keys_value.length!=discovery_map.length");
                                          continue;
                                    }

                                    for(int i=0; i<discovery_map.length; i++) {
                                        k.delete(0, k.length());
                                        k.append("{#").append(discovery_map[i]).append("}"); 
                                        xxx.put(k.toString(), keys_value[i]);
                                    }

                                    aray.add(xxx); 

                             };
                                   
                                    
                               
   
                               data.put("data", aray);
                               s_return = data.toJSONString();
                             //  System.out.println(s_return);
                          
                               WorkingStats.idlePlus();  
                               logApiList.get(id).setNormalReturnValue(s_return);
                               logApiList.get(id).setIsNowExecutingToFalse(); 
                               logApiList.get(id).setIsNowRefreshingToFalse();
                               logApiList.get(id).setReadyToSendToTrue();                               
                           } 
                       } else {
                          
                
                           if (!logCrawlerApiList.get(id).isActiveMode()){
                           
                           } else  if (logCrawlerApiList.get(id).isNowExecuting()){
                                logger.warn("Job " + jobKey + " is still executing !!!!");
                                WorkingStats.idlePlus();
                                WorkingStats.setLockForQueue(id);
                                stillExecuting=true;
                           } else {


                                String log_interface = logCrawlerApiList.get(id).getLogInterface();
                                String logPath = logCrawlerApiList.get(id).getLogPath();
                                long pointer = logCrawlerApiList.get(id).getPointer();
                                String dateformat = logCrawlerApiList.get(id).getDateFormat();
                                
                                if (!dateformat.equals("default")){
                                        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);

                                        try {
                                            String stringDate = dateFormat.format(new Date());

                                            logPath = logPath.replaceAll("#DATE", stringDate);
                                        } catch (Exception e){logger.error(e);}
                                 }

                                logCrawlerApiList.get(id).setIsNowExecuting();
                                List<String> stringRule = new ArrayList<>();
                                List<Integer> ile = new ArrayList<>();
                                List<Boolean> regEx = new ArrayList<>();
                                List<Boolean> useRules = new ArrayList<>();
                                List<Boolean> ignoreCase = new ArrayList<>();

                                 //BUG 20180118
                                WorkingStats.idlePlus();


                                    for (int i=0; i<logApiList.size(); i++){ 
                                        if (logApiList.get(i).getType().equalsIgnoreCase("log") ){
                                             if (logApiList.get(i).getLogInterface().equalsIgnoreCase(log_interface)){
                                                  if (logApiList.get(i).isActiveMode()){ 

                                                          logApiList.get(i).setIsNowExecuting(); 
                                                          stringRule.add(logApiList.get(i).getStringRule());
                                                          regEx.add(logApiList.get(i).isRegExpression());
                                                          ignoreCase.add(logApiList.get(i).isIgnoreCase());
                                                          useRules.add(false);
                                                          ile.add(0);


                                                  }

                                             }
                                        }  else  if (logApiList.get(i).getType().equalsIgnoreCase("log_inbuild") ){
                                                if (logApiList.get(i).getLogInterface().equalsIgnoreCase(log_interface)){
                                                  if (logApiList.get(i).isActiveMode()){ 

                                                          logApiList.get(i).setIsNowExecuting(); 
                                                          stringRule.add(logApiList.get(i).getStringRule());
                                                          useRules.add(true);
                                                          ile.add(0);


                                                  }

                                             } 
                                        }  else  if (logApiList.get(i).getType().equalsIgnoreCase("log_embedded") ){
                                                if (logApiList.get(i).getLogInterface().equalsIgnoreCase(log_interface)){
                                                  if (logApiList.get(i).isActiveMode()){ 
                                                          logApiList.get(i).setIsNowExecuting(); 
                                                  }

                                             }
                                        }
                                    }

                                   File file = new File(logPath);

                                    if ((!file.exists()) || (file.isDirectory()) || (!file.canRead())) {
                                       logger.error("Can't read this file. " + logPath);
                                       logCrawlerApiList.get(id).setCanReadFile(false);
                                       isError=true;
                                    } else {
                                        isError=false;
                                        logCrawlerApiList.get(id).setCanReadFile(true);
                                    }

                                    if (!isError){
                                        long fileLength = file.length();

                                        if (pointer==-1){
                                            pointer = fileLength;  
                                            isInit = true;
                                            logger.info("File " + logPath + " pointer init...");
                                        } else isInit=false;


                                        if (pointer>fileLength){
                                            logger.info("File " + logPath + " was reset. Reset to pointer 0.");
                                            pointer=0;
                                        }

                                        diff = fileLength-pointer;

                                        logger.debug("pointer " + pointer + ", fileLength " + fileLength);
                                        //System.out.println("pointer " + pointer + ", fileLength " + fileLength);
                                       
                                        RandomAccessFile fileRaf = new RandomAccessFile(file, "r");
                                        fileRaf.seek(pointer);
                                        String str = null;
                                        int t=0;
                                        //boolean eof=false;
                                        
                                        if (useRules.size()!=stringRule.size()){
                                            logger.fatal("useRules.size()!=stringRule.size()");
                                            System.exit(0);
                                        }

                                        InputStream is = Channels.newInputStream(fileRaf.getChannel());
                                        InputStreamReader isr = new InputStreamReader(is);

                                        BufferedReader reader = new BufferedReader(isr, bufferSize);
                                       
                                        //while((str = reader.readLine())!= null && !eof){
                                        lines=0;
                                        boolean found=false;
                                        String ret = "default";
                                        while((str = reader.readLine())!= null){
                                             lines++; 
                                             //System.out.println(str);
                                             
                                             for (int h=0; h<stringRule.size(); h++){
                                                // System.out.println(str); 
                                                
                                                 if (useRules.get(h)){
                                                        ret = RulesRouer.routeLine(stringRule.get(h), str);
                                                        //System.out.println("########" + ret);
                                                        if (!StringUtils.equals(ret, "default")){
                                                            found=false;
                                                            for (Map.Entry<Integer, String> entry : RulesHelper.entries()){
                                                                if (entry.getKey()==h && entry.getValue().equals(ret)){
                                                                     found=true;                                                                    
                                                                }
                                                            }
                                                            if (!found) {
                                                              //  System.out.println(ret);
                                                                RulesHelper.put(h, ret);
                                                            }
                                                        } 
                                                      
                                                    
                                                 } else {
                                                     if (regEx.get(h)) {

                                                          // Create a Pattern object
                                                          Pattern r = Pattern.compile(stringRule.get(h));

                                                          // Now create matcher object.
                                                          Matcher m = r.matcher(str);
                                                          if (m.find( )) {
                                                              t = ile.get(h);
                                                              ile.set(h, t+1);
                                                          }

                                                     } else {

                                                         if (ignoreCase.get(h)){
                                                           if (str.toLowerCase().contains(stringRule.get(h).toLowerCase())){
                                                              t = ile.get(h);
                                                              ile.set(h, t+1);
                                                           }
                                                         } else {
                                                            if (str.contains(stringRule.get(h))){
                                                              t = ile.get(h);
                                                              ile.set(h, t+1);
                                                            }
                                                         }
                                                     }
                                                 }
       

                                                  /*if (fileRaf.getFilePointer()>=fileLength){
                                                       pointer = fileRaf.getFilePointer();
                                                       eof=true;
                                                  }*/
                                            }
                                             
                                            pointer = fileRaf.getFilePointer();
                                        }
                                        
                                       // System.out.println("**pointer " + pointer + ", fileLength " + fileLength);

                                        isr.close();
                                        is.close();
                                        reader.close();
                                        fileRaf.close();                                        

                                       logCrawlerApiList.get(id).setPointer(pointer);
                                   }
                                   int h=0; //h-wazne
                                   for (int i=0; i<logApiList.size(); i++){ 
                                         if (logApiList.get(i).getLogInterface().equalsIgnoreCase(log_interface)){
                                             if (logApiList.get(i).isActiveMode()){
                                                if (logApiList.get(i).getType().equalsIgnoreCase("log")){
                                                     if (isError) {
                                                        logApiList.get(i).setErrorReturnValue("-1");
                                                     } else {
                                                        logApiList.get(i).setNormalReturnValue(Integer.toString(ile.get(h))); 
                                                     }
                                                     logApiList.get(i).setIsNowExecutingToFalse();                                                    
                                                     if (isInit) logApiList.get(i).setReadyToSendToFalse();
                                                     else  logApiList.get(i).setReadyToSendToTrue();
                                                     h++;
                                                }  else if (logApiList.get(i).getType().equalsIgnoreCase("log_inbuild")) {
                                                     if (isError) {
                                                        logApiList.get(i).setErrorReturnValue("Error x001");
                                                     } else {
                                                        logApiList.get(i).setNormalReturnValue(RulesRouer.routeResults(stringRule.get(h),h,RulesHelper));
                                                      }
                                                     logApiList.get(i).setIsNowExecutingToFalse();                                                    
                                                     if (isInit) logApiList.get(i).setReadyToSendToFalse();
                                                     else  logApiList.get(i).setReadyToSendToTrue();
                                                     h++;                                                   
                                                } else if (logApiList.get(i).getType().equalsIgnoreCase("log_embedded")) {
                                                     if (isError) {
                                                        logApiList.get(i).setErrorReturnValue("Error x001");
                                                     } else {
                                                        if (logApiList.get(i).getStringRule().equals("lines_count")){
                                                            logApiList.get(i).setNormalReturnValue(""+lines);
                                                        } else if (logApiList.get(i).getStringRule().equals("char_count")){
                                                            logApiList.get(i).setNormalReturnValue(""+diff);
                                                        }
                                                     }
                                                     logApiList.get(i).setIsNowExecutingToFalse();
                                                     logApiList.get(i).setReadyToSendToTrue();
                                                }
                                             }

                                         }
                                    }
                                   
                                    RulesHelper.clear();

                                    stillExecuting=false;
                                    logger.debug("Log analyze done...");
                                
                       }
                       

                   }
          
             }  catch (Exception e) {
                logger.info("--- Error in job ! ----");
                
                Thread.currentThread().interrupt();
             
                JobExecutionException e2 = new JobExecutionException(e);
        	// this job will refire immediately
        	e2.refireImmediately();
                throw e2;  
          } finally {
                
                            
                  if (!stillExecuting) {
                      if (type.startsWith("log")){
                          logCrawlerApiList.get(id).setIsNowExecutingToFalse();
                          logCrawlerApiList.get(id).setIsNowRefreshingToFalse();
                      }
                  
                  } else {
                       WorkingStats.idlePlus();
                  }
                  
                
                  if (isJobInterrupted) {
                    logger.error("Job " + jobKey + " did not complete");
                  } else {
                    logger.info("Job " + jobKey + " completed at " + new Date());
                  }
            } 
        }
        
 
        
      @Override
     // this method is called by the scheduler
      public void interrupt() throws UnableToInterruptJobException {
        logger.info("Job " + jobKey + "  -- INTERRUPTING --");
        isJobInterrupted = true;
        if (thisThread != null) {
          // this call causes the ClosedByInterruptException to happen
          thisThread.interrupt(); 
        }
      }

 
}