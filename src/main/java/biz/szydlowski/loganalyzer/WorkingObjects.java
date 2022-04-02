/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer;

import biz.szydlowski.loganalyzer.api.logApi;
import biz.szydlowski.loganalyzer.api.logCrawlerApi;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Dominik
 */
public class WorkingObjects {
    public static HashMap<String, Boolean> unique_hostname  = null;
  
    public static LogQuartz _LogQuartz=null;
    public static List<logApi>  logApiList  = null; 
    public static List<logCrawlerApi> logCrawlerApiList = null;
    public static List<String> allowedConn = null;
}
