/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer;


import static biz.szydlowski.loganalyzer.WorkingObjects.logApiList;
import static biz.szydlowski.loganalyzer.WorkingObjects.logCrawlerApiList;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author szydlowskidom
 */
public class LogQuartz {
    
     static final Logger logger =  LogManager.getLogger(LogQuartz.class);
  
    private Scheduler scheduler;
    private boolean isDo = false;
     
    public LogQuartz(){
    }
     
    public void doJob(){
        
        if (isDo){
            logger.fatal("LogQuartz  is running");
            return;
        } else {
            logger.info("*** LogQuartz  starting ****");
            isDo = true;
        }
                  
        try {
           scheduler = new StdSchedulerFactory().getScheduler();
        } catch(SchedulerException e){
           logger.error(e);
        }
     
      
        try {
                int queue = 0;
                int queue_active = 0;
                
                List<JobDetail> jobs = new ArrayList<>();
                List<Trigger> triggers = new ArrayList<>();
                logger.debug("logCrawlerApiList " +  logCrawlerApiList.size());
                     
               
               for (queue=0; queue<logCrawlerApiList.size(); queue++){
               
                                  
                   logger.debug("Add job " + logCrawlerApiList.get(queue).getLogInterface() + "." + queue);
             
                   jobs.add(JobBuilder.newJob(LogQuartzJob.class).withIdentity(logCrawlerApiList.get(queue).getLogInterface() + "." + queue, "logCrawler").build());

                   jobs.get(queue_active).getJobDataMap().put("id", queue);
                   jobs.get(queue_active).getJobDataMap().put("type", "log");

                   triggers.add(TriggerBuilder
                        .newTrigger()
                        .withIdentity("trigger." +   logCrawlerApiList.get(queue).getLogInterface() + "." + queue, "LOG")
                        .withSchedule(CronScheduleBuilder.cronSchedule( logCrawlerApiList.get(queue).getCronExpression()))
                        .build());

                   queue_active++;

                  jobs.add(JobBuilder.newJob(LogQuartzJob.class).withIdentity( logCrawlerApiList.get(queue).getLogInterface() + ".runAtStartup." + queue, "Zabbix.Sybase").build());
                  jobs.get(queue_active).getJobDataMap().put("id", queue);
                  jobs.get(queue_active).getJobDataMap().put("type", "log");

                  Trigger runAtStartupTrigger = TriggerBuilder.newTrigger().withIdentity("runAtStartup.now." + logCrawlerApiList.get(queue).getLogInterface()  + queue).build();
                  triggers.add(runAtStartupTrigger);

                  queue_active++;
             
                } 
               
                for (queue=0; queue<logApiList.size(); queue++){
                    
                    if (logApiList.get(queue).getType().equalsIgnoreCase("maintenance")){
                        
                                  
                       logger.debug("Add nolog job " + logApiList.get(queue).getStringRule() + "." + queue);

                       jobs.add(JobBuilder.newJob(LogQuartzJob.class).withIdentity(logApiList.get(queue).getStringRule() + "." + queue, "nolog").build());

                       jobs.get(queue_active).getJobDataMap().put("id", queue);
                       jobs.get(queue_active).getJobDataMap().put("type", "nolog");

                       triggers.add(TriggerBuilder
                            .newTrigger()
                            .withIdentity("trigger." +  logApiList.get(queue).getStringRule() + "." + queue, "NOLOG")
                            .withSchedule(CronScheduleBuilder.cronSchedule( logApiList.get(queue).getCronExpression()))
                            .build());

                       queue_active++;
                    }  else  if (logApiList.get(queue).getType().equalsIgnoreCase("discovery") || logApiList.get(queue).getType().equalsIgnoreCase("discovery_static")){                        
                                  
                       logger.debug("Add nolog job " + logApiList.get(queue).getStringRule() + "." + queue);

                       jobs.add(JobBuilder.newJob(LogQuartzJob.class).withIdentity(logApiList.get(queue).getStringRule() + "." + queue, "nolog").build());

                       jobs.get(queue_active).getJobDataMap().put("id", queue);
                       jobs.get(queue_active).getJobDataMap().put("type", "nolog");

                       triggers.add(TriggerBuilder
                            .newTrigger()
                            .withIdentity("trigger." +  logApiList.get(queue).getStringRule() + "." + queue, "NOLOG")
                            .withSchedule(CronScheduleBuilder.cronSchedule( logApiList.get(queue).getCronExpression()))
                            .build());

                        queue_active++;
                

                        jobs.add(JobBuilder.newJob(LogQuartzJob.class).withIdentity(logApiList.get(queue).getAlias() + ".now." + queue, "Zabbix.Discovery.DB").build());
                        jobs.get(queue_active).getJobDataMap().put("id", queue);
                        jobs.get(queue_active).getJobDataMap().put("type", "nolog");


                       
                        Trigger runOnceTrigger = TriggerBuilder.newTrigger().withIdentity("trigger.now." +  logApiList.get(queue).getAlias()  + queue).build();
                        triggers.add(runOnceTrigger);
                        
                         queue_active++;
                       
                       
                    } 
             
            
                } 
          
                 //schedule it
                 if (!scheduler.isStarted()){
                       logger.info("Start scheduler");
                       scheduler.start();
                 }  
                
                
                for (int ii=0; ii<jobs.size(); ii++){
                    scheduler.scheduleJob(jobs.get(ii), triggers.get(ii));
                }
          
        }
        catch(SchedulerException e){
           logger.error(e);
        }
        
    
    }
   
    public boolean stop(){

       try {
            scheduler.clear();
            scheduler.shutdown();
            return true;
       }  catch(SchedulerException e){
            logger.error(e);
            return false;
       }
      
    }
    
    public String refreshMaintananceTask(int _queue_task){
        
        if (_queue_task<0){
            return "Error in logCrawler task ERC = -1";
         }  else if (_queue_task>=logApiList.size()){
            return "Error in logCrawler task ERC > SIZE";
        }
       
        if (isDo){
            
            if (!logApiList.get(_queue_task).isActiveMode()){
               return "The logCrawler task " + logApiList.get(_queue_task).getLogInterface()  + " is in inactive mode, you cannot refresh it.";
            }
       
            if  ( !logApiList.get(_queue_task).isNowRefreshing() ){
                
                if  ( logApiList.get(_queue_task).isNowExecuting() ){
                   return "The logCrawler task " + logApiList.get(_queue_task).getLogInterface() + " is executing now, you cannot refresh it.";
                } 
                
                
                // if you don't call startAt() then the current time (immediately) is assumed.
                Trigger runOnceTrigger = TriggerBuilder.newTrigger().withIdentity("logCrawler.trigger.now."+ _queue_task, "NOLOG").build();
                JobDetail job = JobBuilder.newJob(LogQuartzJob.class)
                        .withIdentity("refresh."+logApiList.get(_queue_task).getLogInterface()+"."+_queue_task, "NOLOG").build();
               
                job.getJobDataMap().put("id", _queue_task); 
                job.getJobDataMap().put("type", "nolog");
                logApiList.get(_queue_task).setIsNowRefreshing();
               
                try {
                    scheduler.scheduleJob(job, runOnceTrigger);
                     //schedule it
                     if (!scheduler.isStarted()){
                           logger.info("Start scheduler");
                           scheduler.start();
                     }  
                    return "The logApi task " + logApiList.get(_queue_task).getStringRule() + " was scheduled.";
                } catch (SchedulerException e){
                    logger.error(e);
                    return e.getMessage();
                }             

            } else {
                return  "The logApi  task " + logCrawlerApiList.get(_queue_task).getLogInterface()  + " is refreshing now, you cannot refresh it again.";  
               
            }
        } else {
            return "The logApi task is not monitoring.";
        }
   
    } 
    
    public String refreshCrawlerTask(int _queue_task){
        
        if (_queue_task<0){
            return "Error in logCrawler task ERC = -1";
        } else if (_queue_task>=logCrawlerApiList.size()){
            return "Error in logCrawler task ERC > SIZE";
        }
       
       
        if (isDo){
            
            if (!logCrawlerApiList.get(_queue_task).isActiveMode()){
               return "The logCrawler task " + logCrawlerApiList.get(_queue_task).getLogInterface()  + " is in inactive mode, you cannot refresh it.";
            }
       
            if  ( !logCrawlerApiList.get(_queue_task).isNowRefreshing() ){
                
                if  ( logCrawlerApiList.get(_queue_task).isNowExecuting() ){
                   return "The logCrawler task " + logCrawlerApiList.get(_queue_task).getLogInterface() + " is executing now, you cannot refresh it.";
                } 
                
                
                // if you don't call startAt() then the current time (immediately) is assumed.
                Trigger runOnceTrigger = TriggerBuilder.newTrigger().withIdentity("logCrawler.trigger.now."+ _queue_task, "logCrawler").build();
                JobDetail job = JobBuilder.newJob(LogQuartzJob.class)
                        .withIdentity("refresh."+logCrawlerApiList.get(_queue_task).getLogInterface()+"."+_queue_task, "logCrawler").build();
               
                job.getJobDataMap().put("id", _queue_task); 
                job.getJobDataMap().put("type", "log");
                logCrawlerApiList.get(_queue_task).setIsNowRefreshing();
               
                try {
                    scheduler.scheduleJob(job, runOnceTrigger);
                     //schedule it
                     if (!scheduler.isStarted()){
                           logger.info("Start scheduler");
                           scheduler.start();
                     }  
                    return "The logCrawler task " + logCrawlerApiList.get(_queue_task).getLogInterface() + " was scheduled.";
                } catch (SchedulerException e){
                    logger.error(e);
                    return e.getMessage();
                }             

            } else {
                return  "The logCrawler  task " + logCrawlerApiList.get(_queue_task).getLogInterface()  + " is refreshing now, you cannot refresh it again.";  
               
            }
        } else {
            return "The logCrawler task is not monitoring.";
        }
   
    } 
}
