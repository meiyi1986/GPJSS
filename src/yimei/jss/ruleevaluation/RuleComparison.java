package yimei.jss.ruleevaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yimei.jss.FJSSMain.getFileNames;

/**
 * This class should assess the files located in out/test, parse them, and analyse the comparative
 * best sequencing or routing rule.
 * Created by dyska on 28/05/17.
 */
public class RuleComparison {
    public static void EvaluateOutput(String dirPath, String ruleType) {
        //dirPath = out/test
        //ruleType = sequencingRule or routingRule

        String path = (new File("")).getAbsolutePath() + dirPath;
        List<String> fileNames = getFileNames(new ArrayList(), Paths.get(path), ".fjs");
        List<String> bestRulesTotal = new ArrayList<String>();
        for (String fileName: fileNames) {
            if (!fileName.contains("sdata")) {
                //sdata is not FJSS data, shouldn't inform decision
                //assess individual file
                List<String> bestRules = EvaluateFile(fileName, ruleType);
                String message = fileName + " best "+ruleType+" rule for this instance: ";
                for (String rule: bestRules) {
                    message += rule +", ";
                }
                System.out.println(message.substring(0, message.length()-2));
                bestRulesTotal.addAll(bestRules);
            }
        }

        List<String> bestOverallRules = getBestRules(bestRulesTotal);
        //at this stage, very likely one rule, if there are multiples then just pick one
        String bestRule = bestOverallRules.get(0);
        System.out.println("Best overall "+ruleType+" rule: "+bestRule);
    }

    public static List<String> EvaluateFile(String fileName, String ruleType) {
        //this method should return the best rule(s) of ruleType
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {

            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);

            String sCurrentLine;

            //The idea here is we have n sequencing rules and m routing rules
            //Say we want to assess routing rules, and we want to work out which
            //of the m routing rules is best
            //then we compare each of the routing rules to the same sequencing rule
            //and see which of them did best. The best one(s) each get a point.
            //The rule with the most points is the best

            List<RuleComparisonResult> bestResults = new ArrayList<>();

            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                RuleComparisonResult result = parseString(sCurrentLine);
                boolean firstRule = true;
                boolean replacedRule = false;
                for (int i = 0; i < bestResults.size() && !replacedRule; ++i) {
                    RuleComparisonResult br = bestResults.get(i);
                    if (br.getRule(ruleType).equals(result.getRule(ruleType))) {
                        firstRule = false;
                        if (result.getFitness() < br.getFitness()) {
                            //new result is superior
                            bestResults.remove(br);
                            bestResults.add(result);
                            replacedRule = true;
                        }
                    }
                }
                if (firstRule) {
                    bestResults.add(result);
                }
            }
            return getBestRules(bestResults,ruleType);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static RuleComparisonResult parseString(String input) {
        String routingRule = input.substring("RR:".length()+1,input.indexOf("SR:")-2);
        String dispatchingRule = input.substring(input.indexOf("SR:")+"SR:".length()+1,input.indexOf("-")-2);
        double fitness = Double.parseDouble(input.substring(input.indexOf("Fitness:")+"Fitness: [".length(),
                input.length()-2));
        return new RuleComparisonResult(routingRule, dispatchingRule, fitness);
    }

    public static List<String> getBestRules(List<RuleComparisonResult> bestResults, String ruleType) {
        Map<String, Integer> map = new HashMap<>();
        for (RuleComparisonResult result: bestResults) {
            String rule = "";
            if (ruleType.equals("RR")) {
                rule = result.getRoutingRule();
            } else if (ruleType.equals("SR")) {
                rule = result.getSequencingRule();
            }
            Integer val = map.get(rule);
            map.put(rule, val == null ? 1 : val + 1);
        }
        return getMaxOccuringEntry(map);
    }

    public static List<String> getBestRules(List<String> bestRules) {
        Map<String, Integer> map = new HashMap<>();
        for (String rule: bestRules) {
            Integer val = map.get(rule);
            map.put(rule, val == null ? 1 : val + 1);
        }
        return getMaxOccuringEntry(map);
    }

    public static List<String> getMaxOccuringEntry(Map<String, Integer> map) {
        List<String> bestRules = new ArrayList<String>();
        Map.Entry<String, Integer> max = null;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }
        bestRules.add(max.getKey());

        //check if any other rules had that many too
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() == max.getValue() && e.getKey() != max.getKey())
                bestRules.add(e.getKey());
        }
        return bestRules;
    }

    public static void main(String[] args) {
        EvaluateOutput("/out/test/", "RR");
    }
}

class RuleComparisonResult {
    private String routingRule;
    private String sequencingRule;
    private double fitness;

    public RuleComparisonResult(String routingRule, String sequencingRule, double fitness) {
        this.routingRule = routingRule;
        this.sequencingRule = sequencingRule;
        this.fitness = fitness;
    }

    public String getRoutingRule() { return routingRule; }

    public String getSequencingRule() { return sequencingRule; }

    public String getRule(String ruleType) {
        if (ruleType.equals("RR")) {
            //checking for routing rules, so compare with sequencing rules
            return sequencingRule;
        } else if (ruleType.equals("SR")) {
            //checking for sequencing rules, so compare with routing rules
            return routingRule;
        } return null;
    }

    public double getFitness() { return fitness; }
}
