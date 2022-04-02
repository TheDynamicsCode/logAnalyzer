/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.szydlowski.loganalyzer.rules;

import com.google.common.collect.Multimap;


/**
 *
 * @author Dominik
 */
public class RulesRouer {
    
    public static String routeResults(String rule, int id, Multimap<Integer, String> RulesHelper){
        if (rule.equals("R_0001")){
            return RulesEngine_0001.getResults(RulesHelper, id);
        } else if (rule.equals("R_0002")){
            return RulesEngine_0002.getResults(RulesHelper, id);
        } else if (rule.equals("CVE_2021_44228")){
            return RulesEngine_CVE_2021_44228.getResults(RulesHelper, id);
        }
        return "not found rule " + rule;
    }
     
    public static String routeLine(String rule, String line){
        if (rule.equals("R_0001")){
            return RulesEngine_0001.execute(line);
        } else if (rule.equals("R_0002")){
            return RulesEngine_0002.execute(line);
        } else if (rule.equals("CVE_2021_44228")){
            return RulesEngine_CVE_2021_44228.execute(line);
        }
        return "default";
    }
    
}
