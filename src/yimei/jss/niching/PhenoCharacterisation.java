package yimei.jss.niching;

import yimei.jss.jobshop.Operation;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.weighted.WSPT;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The phenotypic characterisation of rules.
 *
 * Created by YiMei on 3/10/16.
 */
public class PhenoCharacterisation {

    private List<DecisionSituation> decisionSituations;
    private AbstractRule referenceRule;
    private int[] referenceIndexes;

    public PhenoCharacterisation(List<DecisionSituation> decisionSituations,
                                 AbstractRule referenceRule) {
        this.decisionSituations = decisionSituations;
        this.referenceRule = referenceRule;
        referenceIndexes = new int[decisionSituations.size()];

        calcReferenceIndexes();
    }

    public List<DecisionSituation> getDecisionSituations() {
        return decisionSituations;
    }

    public AbstractRule getReferenceRule() {
        return referenceRule;
    }

    public int[] getReferenceIndexes() {
        return referenceIndexes;
    }

    private void calcReferenceIndexes() {
        for (int i = 0; i < decisionSituations.size(); i++) {
            DecisionSituation situation = decisionSituations.get(i);
            Operation op = referenceRule.priorOperation(situation);
            int index = situation.getQueue().indexOf(op);
            referenceIndexes[i] = index;
        }
    }

    public void setReferenceRule(AbstractRule rule) {
        this.referenceRule = rule;

        calcReferenceIndexes();
    }

    public int[] characterise(AbstractRule rule) {
        int[] charList = new int[decisionSituations.size()];

        for (int i = 0; i < decisionSituations.size(); i++) {
            DecisionSituation situation = decisionSituations.get(i);
            List<Operation> queue = situation.getQueue();

            int refIdx = referenceIndexes[i];

            // Calculate the priority for all the operations.
            for (Operation op : queue) {
                op.setPriority(rule.priority(
                        op, situation.getWorkCenter(), situation.getSystemState()));
            }

            // get the rank of the processing chosen by the reference rule.
            int rank = 1;
            for (int j = 0; j < queue.size(); j++) {
                if (queue.get(j).priorTo(queue.get(refIdx))) {
                    rank ++;
                }
            }

            charList[i] = rank;
        }

        return charList;
    }

    public static PhenoCharacterisation defaultPhenoCharacterisation() {
        AbstractRule refRule = new WSPT();
        int minQueueLength = 10;
        int numDecisionSituations = 20;
        long shuffleSeed = 8295342;

        DynamicSimulation simulation = DynamicSimulation.standardFull(0, refRule,
                10, 500, 0, 0.95, 4.0);

        List<DecisionSituation> situations = simulation.decisionSituations(minQueueLength);
        Collections.shuffle(situations, new Random(shuffleSeed));

        situations = situations.subList(0, numDecisionSituations);
        return new PhenoCharacterisation(situations, refRule);
    }

    public static double distance(int[] charList1, int[] charList2) {
        double distance = 0.0;
        for (int i = 0; i < charList1.length; i++) {
            double diff = charList1[i] - charList2[i];
            distance += diff * diff;
        }

        return Math.sqrt(distance);
    }
}
