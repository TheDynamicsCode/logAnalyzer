/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package biz.szydlowski.loganalyzer.rules;

import com.google.common.collect.Multimap;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
/**
 *
 * @author Dominik
 */
public class RulesEngine_CVE_2021_44228 {
    
    public static String execute(String line){
         String [] rule = {"${jndi:ldap:",
             "${jndi:rmi:/", "${jndi:ldaps:/", "${jndi:dns:/", "${jndi:nis:/",
             "${jndi:nds:/", "${jndi:corba:/", "${jndi:iiop:/"};
         
         int MAX_DISTANCE=10;
         
         int [] level = {0,0,0,0,0,0,0,0};
         int [] distance = {0,0,0,0,0,0,0,0};
         
        line = line.toLowerCase().replaceAll("%7B", "{");
         
         int detected=0;
         for (int i=0; i<rule.length; i++){
             if (line.contains(rule[i])){
                  detected++;
             }
         }
         
         
        // Walk over characters
        
        for (char c :  line.toCharArray()){
             for (int i=0; i<rule.length; i++){
                 //   # If the character in the line matches the character in the detection
                if (c == (rule[i]).charAt(level[i])){
                   level[i] ++;
                   distance[i] = 0;
               }
                // If level > 0 count distance to the last char
                if (level[i] > 0){
                    distance[i]++;
                    // If distance is too big, reset level to zero
                    if (distance[i] > MAX_DISTANCE ){
                        distance[i] = 0;
                        level[i] = 0 ;
                   }
                }
               // # Is the pad completely empty?
                if (rule[i].length() == level[i]){
                     detected++;
                }
            }
        }
        
         return  Integer.toString(detected);
    }
    
      public static String getResults(Multimap<Integer, String> RulesHelper, int id){
         int ile=0;
         for (Map.Entry <Integer, String> entry : RulesHelper.entries()) { 
               if (entry.getKey()==id){
                   ile = ile + Integer.parseInt(entry.getValue());
                   
               }
         }
         return Integer.toString(ile);
    }

}
