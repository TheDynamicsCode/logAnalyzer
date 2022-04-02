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
 * @author Dominik
 */
public class logApi {
    
    public static int ZBX_STATE_NORMAL=0;
    public static int ZBX_STATE_NOTSUPPORTED=1;
  
    private boolean activeMode; 
    private String zabbix_server_name;
    private String zabbix_key;
    private String zabbix_host;
    private boolean isReadyToSend; 
    private boolean isNowExecuting; 
    private boolean isNowRefreshing;
    private String log_interface; 
    private String alias;
    private String lastExecuteTime;
    private String settingFile;
    private String cron_expression;
    
    private String stringRule; 
    private String type;
    private String returnValue;
    private boolean ignoreCase;
    private boolean regEx;
    private int state;
    private final SimpleDateFormat sdf; 
    private int idCrawler;
    private final List <String> configErrorList;
      
    public logApi(){
        sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        lastExecuteTime = "NONE";
        this.activeMode=true;
        this.zabbix_server_name="name";
        this.zabbix_key="zkey";
        this.zabbix_host="zhost";
        this.isReadyToSend=false;
        this.isNowExecuting=false;
        this.isNowRefreshing=false;
        this.cron_expression = "0 0 0 * * ?";
        
        this.settingFile = "default";
        this.type = "log";
        this.returnValue = "init";
        this.log_interface = "default";
        ignoreCase = false;
        regEx=false;
        idCrawler = -1;
         configErrorList = new ArrayList<>();
    }
    
    public int getCrawlerID(){
        return idCrawler;
    } 
    
    public void setCrawlerID(int s){
        idCrawler=s;
    }

    public void setSettingFile(String settingFile){
        this.settingFile=settingFile;
    }
    
    public void setToActiveMode(){
        activeMode=true;
    }  
    
    public void setCronExpression(String set){
        if (org.quartz.CronExpression.isValidExpression(set)){
           cron_expression=set;
        } else {
            cron_expression = "0 0 0 * * ?"; 
        }
    } 
    
    public void setIgnoreCase(String val){
        ignoreCase = Boolean.parseBoolean(val);
    }
    
    public boolean isIgnoreCase(){
        return ignoreCase;
    }  
    
    public void setRegExpression(String val){
        regEx = Boolean.parseBoolean(val);
    }
    
    public boolean isRegExpression(){
        return regEx;
    }
    
    public String getCronExpression(){
        return cron_expression;
    }
       
    
    public void setType(String t){
        this.type=t;
    }
    
    public String getType(){
        return type;
    }
    
    
    public void setToInactiveMode(){
        activeMode=false;
    } 
    
    public boolean isActiveMode(){
       return activeMode;
    } 
    
    public void setZabbixServerName(String set){
        zabbix_server_name=set;
    }  
    
    public void setLogInterface(String set){
       log_interface=set;
    }
    
    
    public void setZabbixKey(String set){
        zabbix_key=set;
    } 
    
    public void setZabbixHost(String set){
        zabbix_host=set;
    } 
 

    public void setReadyToSendToTrue(){
        isReadyToSend=true;
    } 
    
    public void setReadyToSendToFalse(){
        isReadyToSend=false;
    }
   
    public void setIsNowExecuting (){
        isNowExecuting = true;
        lastExecuteTime = sdf.format(new Date());
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
    
    public void setAlias(String set){
       alias=set;
    }
    
    public String getAlias(){
       return alias;
    }
    
    public void setNormalReturnValue(String value){
        returnValue=value;
        state=ZBX_STATE_NORMAL;
    }
    
    public String getReturnValue(){
        return returnValue;
    } 
    
    public int getReturnState(){
        return state;
    }
   
    public void setStringRule(String value){
        stringRule=value;
    }
    
   
    public String getStringRule(){
        return stringRule;
    }
    
    public void setErrorReturnValue(String value){
        returnValue=value;
        state=ZBX_STATE_NOTSUPPORTED;
    }
    
     
    public String getSettingFile(){
        return this.settingFile;
    }
  
  
    public String getZabbixKey(){
        return zabbix_key;
    }   
    
    public String getZabbixHost(){
        return zabbix_host;
    } 
   
    public String getZabbixServerName(){
        return zabbix_server_name;
    }   

    
    public String getLogInterface(){
       return log_interface;
    }
   
        
    public boolean isReadyToSend(){
        return isReadyToSend;
    } 
    
    public boolean isNowExecuting(){
        return isNowExecuting;
    }
   
    public boolean isNowRefreshing(){
        return isNowRefreshing;
    }
    
     public void addErrorToConfigErrorList(String error){
       configErrorList.add(error);
    }  
    
    public void addErrorToConfigErrorList(List<String> error){
        error.forEach(configErrorList::add);
    }
   
   
    public String printConfigErrorList(){
        StringBuilder sb = new StringBuilder();
         sb.append("<font color=\"red\">");
        configErrorList.forEach((e) -> {
            sb.append(e).append("<br>");
        }); 
        sb.append("</font>");
        return sb.toString();
    }

        
    
}
