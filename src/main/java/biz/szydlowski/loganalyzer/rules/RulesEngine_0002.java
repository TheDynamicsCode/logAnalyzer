/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer.rules;

import com.google.common.collect.Multimap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Dominik
 */
public class RulesEngine_0002 {
     static int  MAX_LEN=10;
    
    public static String execute(String line){
         String ret="default";
         
         if (line.contains("*** doDigitalSign end service :")){
             ret = StringUtils.substringBetween(line, "*** doDigitalSign end service :", " [s]").replace("\\s+", "");
             if (ret.length()>MAX_LEN) ret="default";
         }
         
         return ret;
    }
    
      public static String getResults(Multimap<Integer, String> RulesHelper, int id){
         double max=0;
         for (Map.Entry <Integer, String> entry : RulesHelper.entries()) { 
               if (entry.getKey()==id){
                   if (Double.parseDouble(entry.getValue())>max){
                       max=Double.parseDouble(entry.getValue());
                   }
               }
         }
         return Integer.toString((int)(max*1000));
    }

}
