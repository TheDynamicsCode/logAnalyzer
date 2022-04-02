/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * @author szydlowskidom
 */
public class logCrawlerApi {
    
    public static int ZBX_STATE_NORMAL=0;
    public static int ZBX_STATE_NOTSUPPORTED=1;
    
    private boolean activeMode; 
    private boolean isDone; 
    private boolean isNowExecuting; 
    private boolean isNowRefreshing;
    private String cron_expression;
    private String log_path; 
    private String logInterface; 
    private String lastExecuteTime;
    private long pointer;
    private String dateFormat;
    private boolean canReadFile;
    
    private final  SimpleDateFormat sdf;
    private final List <String> configErrorList;
    
    public logCrawlerApi(){
        lastExecuteTime = "NONE";
        this.activeMode=true;
        this.isDone=false;
        this.isNowExecuting=false;
        this.isNowRefreshing=false;
        this.cron_expression = "0 0 0 * * ?";
        this.pointer = -1;
        sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        dateFormat="default";
        configErrorList = new ArrayList<>();
    }
    
    public void setCanReadFile(boolean set){
        canReadFile=set;
    }
    
    public boolean isCanReadFile(){
        return canReadFile;
    }
     
    public void setToActiveMode(){
        activeMode=true;
    }
    
    public void setToInactiveMode(){
        activeMode=false;
    } 
    
    public boolean isActiveMode(){
       return activeMode;
    } 
    

    public void setCronExpression(String set){
        if (org.quartz.CronExpression.isValidExpression(set)){
           cron_expression=set;
        } else {
            cron_expression = "0 0 0 * * ?"; 
        }
    } 
    
    public String getCronExpression(){
        return cron_expression;
    }
        
     
    public void setlogPath(String set){
       log_path=set;
    } 
    
    public void setDateFormat(String set){
        dateFormat=set;
    }
    
    public void setLogInterface(String set){
       logInterface=set;
    }
    
    public void setDoneToTrue(){
        isDone=true;
    } 
    
    public void setDoneToFalse(){
        isDone=false;
    }
   
    public void setIsNowExecuting (){
        isNowExecuting = true;
        lastExecuteTime = sdf.format(new Date());
    }    
 
    public String getDateFormat(){
        return dateFormat;
    }
    
   
    
    public String getLastExecuteTime(){
        return lastExecuteTime;
    }
    
    public void setIsNowRefreshing (){
        isNowRefreshing= true;
    } 
 
 
    public void setIsNowExecutingToFalse (){
        isNowExecuting = false;
    } 
    
    public void setIsNowRefreshingToFalse (){
        isNowRefreshing = false;
    } 
     

    public String getLogInterface(){
       return logInterface;
    }
  
    public String getLogPath(){
       return log_path;
    }
    
    public boolean isDone(){
        return isDone;
    } 
    
    public boolean isNowExecuting(){
        return isNowExecuting;
    }
   
    public boolean isNowRefreshing(){
        return isNowRefreshing;
    }

    
    public void setPointer(long pointer){
        this.pointer=pointer;
    }
    
     public long getPointer(){
        return this.pointer;
    }
     
     public void addErrorToConfigErrorList(String error){
       configErrorList.add(error);
    }  
  
   
   public List<String> getErrorList(){
       return configErrorList;
    }  
 
 
    
}
