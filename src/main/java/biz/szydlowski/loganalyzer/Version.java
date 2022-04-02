/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer;

import biz.szydlowski.utils.UtilsVersion;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author dominik
 */
public class Version {
   
    static final Logger logger =  LogManager.getLogger(Version.class);
    /**Nazwa programu*/
   private static final  String PRODUCT_STRING = "logAnalyzer";
    
   public static final boolean IS_DEVELOPMENT_VERSION = true;
   public static final String DEV_VERSION_EXPIRE_STR = "2022-12-31";
   public static Date DEV_VERSION_EXPIRE_DATE = null;
  
    /**Nazwa programu*/
    private static  String fullname = "logAnalyzer Enterprise Edition";
    /**Wersja programu*/
    private static int major = 1;
    /**Minor version*/
    private static int minor = 3;
    /**realse*/
    private static int realase = 2;
 
     /**kompilacja*/
    private static  String update = "2021-12-13";
    
     /**build*/
    private static int build = 0;
   
    private static String author = "Dominik Szydłowski (DoSS)";
        
    private static String website = "www.szydlowski.biz";
    
    private static String support = "support@szydlowski.biz";
    
    static
   {
     try
     {
       build = Integer.parseInt(update.replaceAll("-",""));
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       Date date = new Date();
       DEV_VERSION_EXPIRE_DATE = sdf.parse(DEV_VERSION_EXPIRE_STR);
       
       if(DEV_VERSION_EXPIRE_DATE.before(date)) {
          System.out.println("DEV VERSION DATE EXPIRED");
          System.err.println("DEV VERSION DATE EXPIRED");
          logger.info("DEV VERSION DATE EXPIRED");
          System.exit(2000);
      }
    }
     catch (ParseException e)
     {
        logger.error(e.getMessage());
     }
  }

    public Version(){
     
    }
    public String getAllInfo(){
        StringBuilder sb=new StringBuilder ();
        sb.append(fullname);
        sb.append("\n\t\t");
        sb.append(getVersion());
        sb.append("\n\t\t"); 
        sb.append(author);
        sb.append("\n\t\t");
        sb.append(website);
        sb.append("\n\t\t");
        sb.append(support);
        sb.append("\n\t\t");
        sb.append("Last update "); 
        sb.append(update);
        sb.append("\n\t\t");
        sb.append(new UtilsVersion().getAllInfo());  
        return sb.toString();
    }
    
    public static String getVersion(){
                
        return PRODUCT_STRING + " " + major + "." + minor + "." + realase + " (b" +  build + ")" ;
    }
    
    public static String getAgentVersion(){
                
        return major + "." + minor + "." + realase + "." +  build;
    }  
    
    public static int getBuild(){
               
        return build;
    } 
    
    
      
    public static String getName(){
               
        return PRODUCT_STRING;
    } 
    
    public static String getFullName(){
               
        return fullname;
    }
    
     public static String getAuthor(){
               
        return author;
    }
    
    
    public static String getUpdate(){
               
        return update;
    }

}
