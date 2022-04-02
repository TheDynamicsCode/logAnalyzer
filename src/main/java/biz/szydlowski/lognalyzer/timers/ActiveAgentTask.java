/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.lognalyzer.timers;

import static biz.szydlowski.loganalyzer.LogAnalyzerDaemon.zabbixServerApiList;
import static biz.szydlowski.loganalyzer.WorkingObjects.logApiList;
import static biz.szydlowski.loganalyzer.WorkingObjects.logCrawlerApiList;
import biz.szydlowski.utils.WorkingStats;
import static biz.szydlowski.utils.tasks.TasksWorkspace.ActiveAgentTimer_lock;
import static biz.szydlowski.utils.tasks.TasksWorkspace.ActiveAgentTimer_time;
import biz.szydlowski.zabbixmon.DataObject;
import biz.szydlowski.zabbixmon.SenderResult;
import biz.szydlowski.zabbixmon.ZabbixSender;
import biz.szydlowski.zabbixmon.ZabbixServerApi;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Dominik
 */
public class ActiveAgentTask extends TimerTask  {
    
             static final Logger logger =  LogManager.getLogger(ActiveAgentTask.class); 
        
             //new
            List<DataObject> dataObject_s = new ArrayList<>();
            List<DataObject> dataObject_nsender = new ArrayList<>();
            int maxSenderPacketSize = 200;
            int qSender=0;
            
            SenderResult result; 
               
            List<ZabbixSender> zabbixSenderList = new ArrayList<>();
            boolean firstRun = true;
            StringBuilder k;
           
                              
            @Override
            public void run() {
                     //#BUG 20170608
                ActiveAgentTimer_time = System.currentTimeMillis();
                StringBuilder k = new StringBuilder();
                   
                if (firstRun){
                    logger.info("Setting zabbixServerApi in ActiveAgentTask....");
                    firstRun=false;
                    for (ZabbixServerApi zabbixServerApi : zabbixServerApiList){
                      
                           ZabbixSender zabbixSender = new ZabbixSender(zabbixServerApi.getHost(), zabbixServerApi.getPort(), zabbixServerApi.getConnectTimeout(), zabbixServerApi.getSocketTimeout());
                           zabbixSenderList.add(zabbixSender);
                          
                    }
                }
                               
                if (ActiveAgentTimer_lock){
                    logger.info("ActiveAgentTimer is locked");
                    return;
                } else {
                    ActiveAgentTimer_lock=true;
                }
                           
                try {
                  for (int listServer=0; listServer<zabbixServerApiList.size(); listServer++){

                          boolean setAgent=false;
                          if (dataObject_s!=null) dataObject_s.clear();
                      
                          for (int queue=0; queue<logApiList.size(); queue++){
                            
                                  
                              if (!logApiList.get(queue).getZabbixServerName().equalsIgnoreCase(zabbixServerApiList.get(listServer).getServerName())){
                                  continue;
                              } else {
                                  setAgent = true;
                              }  
                                 
                                if (logApiList.get(queue).isReadyToSend()){ 
                                     try {
                               
                                         if (logApiList.get(queue).getType().equalsIgnoreCase("DISCOVERY")){
                                           
                                              if (logApiList.get(queue).getStringRule().equalsIgnoreCase("setInterface")){
                                         
                                                  DataObject dataObject = new DataObject();

                                                  dataObject.setHost(logApiList.get(queue).getZabbixHost());
                                                  dataObject.setKey(logApiList.get(queue).getZabbixKey());
                                                  dataObject.setState(logApiList.get(queue).getReturnState());
                                                  dataObject.setValue(logApiList.get(queue).getReturnValue());
                                                  dataObject.setClock(System.currentTimeMillis()/1000);

                                                  dataObject_s.add(dataObject); 
                                                  
                                             } else  if (logApiList.get(queue).getStringRule().equalsIgnoreCase("setValue")){
                                                 
                                                  StringBuilder ret = new StringBuilder();
                                   
                                          
                                                   for(int i=0; i<logCrawlerApiList.size();i++) {

                                                         DataObject dataObject = new DataObject();
                                                         dataObject.setHost(logApiList.get(queue).getZabbixHost());
                                                         dataObject.setKey(logApiList.get(queue).getZabbixKey().replace("{#INTERFACE}", logCrawlerApiList.get(i).getLogInterface()));                                              

                                                         if (logCrawlerApiList.get(i).isCanReadFile()){
                                                              dataObject.setValue("0");
                                                         } else {
                                                              dataObject.setValue("1");
                                                         }
                                                        
                                                         dataObject.setState(logApiList.get(queue).getReturnState());

                                                         dataObject.setClock(System.currentTimeMillis()/1000);

                                                         dataObject_s.add(dataObject);
                                                         ret.append(dataObject.getKey()).append(" ").append(dataObject.getValue()).append("\n");

                                                   }
                                                   
                                                   logApiList.get(queue).setNormalReturnValue(ret.toString());
                                            }
                                    
                                         
                                    } else   if (logApiList.get(queue).getType().equalsIgnoreCase("DISCOVERY_STATIC")){
                                           
                                          DataObject dataObject = new DataObject();

                                          dataObject.setHost(logApiList.get(queue).getZabbixHost());
                                          dataObject.setKey(logApiList.get(queue).getZabbixKey());
                                          dataObject.setState(logApiList.get(queue).getReturnState());
                                          dataObject.setValue(logApiList.get(queue).getReturnValue());
                                          dataObject.setClock(System.currentTimeMillis()/1000);

                                          dataObject_s.add(dataObject); 
                                   
                                    } else {
                                         
                                          
                                            DataObject dataObject = new DataObject();

                                            dataObject.setHost(logApiList.get(queue).getZabbixHost());
                                            dataObject.setKey(logApiList.get(queue).getZabbixKey());
                                            dataObject.setValue(logApiList.get(queue).getReturnValue());
                                            dataObject.setState(logApiList.get(queue).getReturnState());
                                            dataObject.setClock(System.currentTimeMillis()/1000);

                                            dataObject_s.add(dataObject);
                                            
                                           // logger.trace(dataObject.toString());
                                       
                                       }
                                    } catch (Exception e){
                                          logger.error("KERNEL PANIC " + e.getMessage() + " for " +  logApiList.get(queue).getZabbixKey()); 
                                          logApiList.get(queue).setReadyToSendToFalse();
                                          WorkingStats.kernelPanicPlus();
                                    } finally {
                                          logApiList.get(queue).setReadyToSendToFalse();
                                    }
                                } //done
                         

                            } 


                            if (dataObject_s.size()>0) {
                                                      
                                if (setAgent){

                                    maxSenderPacketSize = zabbixServerApiList.get(listServer).getMaxSenderPacketSize();
                                    if (maxSenderPacketSize==-1) maxSenderPacketSize=dataObject_s.size(); //unlimited

                                    logger.debug("dataObject to send size - " + dataObject_s.size());

                                    qSender=0;
                                    while (qSender<dataObject_s.size()){

                                        for (int w=1; w<=maxSenderPacketSize; w++){
                                            logger.trace("add object to send nb - " + qSender);
                                            dataObject_nsender.add(dataObject_s.get(qSender));
                                            qSender++;
                                            if (qSender==dataObject_s.size()) break;
                                        }

                                        logger.trace("current parts to send size - " + dataObject_nsender.size());

                                        if (dataObject_nsender.size()>0) {
                                       
                                           logger.debug("dataObject_nsender:" + dataObject_nsender.toString());
                                            result = zabbixSenderList.get(listServer).send(dataObject_nsender);
                                      
                                            WorkingStats.zabbixProcessedCountPlus(result.getProcessed());
                                            //#BUG20170903
                                            WorkingStats.zabbixFailedCountPlus(result.getFailed());
                                            WorkingStats.zabbixTotalCountPlus(result.getTotal());
                                        
                                        
                                            if (result.isConnError()){
                                                WorkingStats.connErrorPlus();
                                            }
                                     
                                    
                                        }
                                    
                                        dataObject_nsender.clear();
                                    }

                                } else {
                                    logger.error("Problem with set active agent???? " +zabbixServerApiList.get(listServer).getServerName());
                                }

                            }

                            dataObject_s.clear();
                    }
                } catch (Exception e){
                    logger.error("ERROR " + e.getMessage());
                    WorkingStats.kernelPanicPlus();
                } finally {
                     ActiveAgentTimer_time = System.currentTimeMillis();
                     ActiveAgentTimer_lock=false;
                }
             
           };   
            
    }   
