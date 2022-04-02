/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer.rules;

import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Dominik
 */
public class RulesEngine_0001 {
    static int  MAX_LEN=10;
    
    public static String execute(String line){
         String ret="default";
         
         if (line.contains("Status = Pending, Queue = queue/ics/FromZefir")){
             ret = StringUtils.substringBetween(line, "(Thread-", "(HornetQ-client-global-threads").replace("\\s+", "");
             if (ret.length()>MAX_LEN) ret="default";
         }
         
         return ret;
    }
    
      public static String getResults(Multimap<Integer, String> RulesHelper, int id){
           int ile=0;
           ile = RulesHelper.entries().stream().filter(entry -> (entry.getKey()==id)).map(_item -> 1).reduce(ile, Integer::sum);
           return Integer.toString(ile);
    }

 
}
