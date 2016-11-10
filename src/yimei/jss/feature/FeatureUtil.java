package yimei.jss.feature;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import yimei.jss.feature.ignore.Ignorer;
import yimei.jss.gp.GPNodeComparator;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.TerminalsChangable;
import yimei.jss.gp.terminal.BuildingBlock;
import yimei.jss.gp.terminal.ConstantTerminal;
import yimei.jss.niching.ClearingEvaluator;
import yimei.jss.niching.PhenoCharacterisation;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Utility functions for feature selection and construction.
 *
 * Created by YiMei on 5/10/16.
 */
public class FeatureUtil {

    /**
     * Select a diverse set of individuals from the current population.
     * @param state the current evolution state.
     * @param archive the archive from which the set will be chosen.
     * @param n the number of individuals in the diverse set.
     * @return the selected diverse set of individuals.
     */
    public static List<GPIndividual> selectDiverseIndis(EvolutionState state, Individual[] archive,
                                                        int n) {
        Arrays.sort(archive);

        ClearingEvaluator clearingEvaluator = (ClearingEvaluator)state.evaluator;
        PhenoCharacterisation pc = clearingEvaluator.getPhenoCharacterisation();
        pc.setReferenceRule(new GPRule(((GPIndividual)archive[0]).trees[0]));

        List<GPIndividual> selIndis = new ArrayList<>();
        List<int[]> selIndiCharLists = new ArrayList<>();

        for (Individual indi : archive) {
            boolean tooClose = false;

            GPIndividual gpIndi = (GPIndividual)indi;

            int[] charList = pc.characterise(new GPRule(gpIndi.trees[0]));

            for (int i = 0; i < selIndis.size(); i++) {
                double distance = PhenoCharacterisation.distance(charList, selIndiCharLists.get(i));
                if (distance <= clearingEvaluator.getRadius()) {
                    tooClose = true;
                    break;
                }
            }

            if (tooClose)
                continue;

            selIndis.add(gpIndi);
            selIndiCharLists.add(charList);

            if (selIndis.size() == n)
                break;
        }

        return selIndis;
    }

    public static void terminalsInTree(List<GPNode> terminals, GPNode tree) {
        if (tree.depth() == 0) {
            boolean duplicated = false;

            for (GPNode terminal : terminals) {
                if (terminal.toString().equals(tree.toString())) {
                    duplicated = true;
                    break;
                }
            }

            if (!duplicated)
                terminals.add(tree);
        }
        else {
            for (GPNode child : tree.children) {
                terminalsInTree(terminals, child);
            }
        }
    }

    public static List<GPNode> terminalsInTree(GPNode tree) {
        List<GPNode> terminals = new ArrayList<>();
        terminalsInTree(terminals, tree);

        return terminals;
    }

    /**
     * Calculate the contribution of a feature to an individual
     * using the current training set.
     * @param state the current evolution state (training set).
     * @param indi the individual.
     * @param feature the feature.
     * @return the contribution of the feature to the individual.
     */
    public static double contribution(EvolutionState state,
                                      GPIndividual indi,
                                      GPNode feature) {
        RuleOptimizationProblem problem =
                (RuleOptimizationProblem)state.evaluator.p_problem;
        Ignorer ignorer = ((FeatureIgnorable)state).getIgnorer();

        KozaFitness fit1 = (KozaFitness)indi.fitness;
        KozaFitness fit2 = (KozaFitness)fit1.clone();
        GPRule rule = new GPRule((GPTree)indi.trees[0].clone());
        rule.ignore(feature, ignorer);

        problem.getEvaluationModel().evaluate(fit2, rule, state);

        return fit2.standardizedFitness() - fit1.standardizedFitness();
    }

    /**
     * Feature selection by majority voting based on feature contributions.
     * @param state the current evolution state (training set).
     * @param selIndis the selected diverse set of individuals.
     * @param fitUB the upper bound of individual fitness.
     * @param fitLB the lower bound of individual fitness.
     * @return the set of selected features.
     */
    public static List<GPNode> featureSelection(EvolutionState state,
                                                List<GPIndividual> selIndis,
                                                double fitUB, double fitLB) {
        DescriptiveStatistics votingWeightStat = new DescriptiveStatistics();

        for (GPIndividual selIndi : selIndis) {
            double normFit = (selIndi.fitness.fitness() - fitLB) / (fitUB - fitLB);

            if (normFit  < 0)
                normFit = 0;

            double votingWeight = normFit;
            votingWeightStat.addValue(votingWeight);
        }

        double totalVotingWeight = votingWeightStat.getSum();

        List<DescriptiveStatistics> featureContributionStats = new ArrayList<>();
        List<DescriptiveStatistics> featureVotingWeightStats = new ArrayList<>();

        List<GPNode> terminals = ((TerminalsChangable)state).getTerminals();

        for (int i = 0; i < terminals.size(); i++) {
            featureContributionStats.add(new DescriptiveStatistics());
            featureVotingWeightStats.add(new DescriptiveStatistics());
        }

        for (int s = 0; s < selIndis.size(); s++) {
            GPIndividual selIndi = selIndis.get(s);

            for (int i = 0; i < terminals.size(); i++) {
                double c = contribution(state, selIndi, terminals.get(i));
                featureContributionStats.get(i).addValue(c);

                if (c > 0.001) {
                    featureVotingWeightStats.get(i).addValue(votingWeightStat.getElement(s));
                }
                else {
                    featureVotingWeightStats.get(i).addValue(0);
                }
            }
        }

        long jobSeed = ((GPRuleEvolutionState)state).getJobSeed();
        File featureInfoFile = new File("job." + jobSeed + ".fsinfo.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(featureInfoFile));
            writer.write("Feature,Fitness,Contribution,VotingWeights,NormFit,Size");
            writer.newLine();

            for (int i = 0; i < terminals.size(); i++) {
                for (int j = 0; j < selIndis.size(); j++) {
                    writer.write(terminals.get(i).toString() + "," +
                            selIndis.get(j).fitness.fitness() + "," +
                            featureContributionStats.get(i).getElement(j) + "," +
                            featureVotingWeightStats.get(i).getElement(j) + "," +
                            votingWeightStat.getElement(j) + "," +
                            selIndis.get(j).size());
                    writer.newLine();
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<GPNode> selFeatures = new LinkedList<>();

        for (int i = 0; i < terminals.size(); i++) {
            double votingWeight = featureVotingWeightStats.get(i).getSum();

            // majority voting
            if (votingWeight > 0.5 * totalVotingWeight) {
                selFeatures.add(terminals.get(i));
            }
        }

        File fsFile = new File("job." + jobSeed + ".terminals.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fsFile));

            for (GPNode terminal : selFeatures) {
                writer.write(terminal.toString());
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return selFeatures;
    }

    /**
     * Feature construction by majority voting based on contribution.
     * A constructed feature/building block is a depth-2 sub-tree.
     * @param state the current evolution state (training set).
     * @param selIndis the selected diverse set of individuals.
     * @param fitUB the upper bound of individual fitness.
     * @param fitLB the lower bound of individual fitness.
     * @return the constructed features (building blocks).
     */
    public static List<GPNode> featureConstruction(EvolutionState state,
                                                   List<GPIndividual> selIndis,
                                                   double fitUB, double fitLB) {
        List<GPNode> BBs = buildingBlocks(selIndis, 2);

        DescriptiveStatistics votingWeightStat = new DescriptiveStatistics();

        for (GPIndividual selIndi : selIndis) {
            double normFit = (selIndi.fitness.fitness() - fitLB) / (fitUB - fitLB);

            if (normFit  < 0)
                normFit = 0;

            double votingWeight = normFit;
            votingWeightStat.addValue(votingWeight);
        }

        double totalVotingWeight = votingWeightStat.getSum();

        List<DescriptiveStatistics> BBContributionStats = new ArrayList<>();
        List<DescriptiveStatistics> BBVotingWeightStats = new ArrayList<>();

        for (int i = 0; i < BBs.size(); i++) {
            BBContributionStats.add(new DescriptiveStatistics());
            BBVotingWeightStats.add(new DescriptiveStatistics());
        }

        for (int s = 0; s < selIndis.size(); s++) {
            GPIndividual selIndi = selIndis.get(s);

            for (int i = 0; i < BBs.size(); i++) {
                double c = contribution(state, selIndi, BBs.get(i));
                BBContributionStats.get(i).addValue(c);

                if (c > 0.001) {
                    BBVotingWeightStats.get(i).addValue(votingWeightStat.getElement(s));
                }
                else {
                    BBVotingWeightStats.get(i).addValue(0);
                }
            }
        }

        long jobSeed = ((GPRuleEvolutionState)state).getJobSeed();
        File BBInfoFile = new File("job." + jobSeed + ".fcinfo.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(BBInfoFile));
            writer.write("BB,Fitness,Contribution,VotingWeights,NormFit,Size");
            writer.newLine();

            for (int i = 0; i < BBs.size(); i++) {
                BuildingBlock bb = new BuildingBlock(BBs.get(i));

                for (int j = 0; j < selIndis.size(); j++) {
                    writer.write(bb.toString() + "," +
                            selIndis.get(j).fitness.fitness() + "," +
                            BBContributionStats.get(i).getElement(j) + "," +
                            BBVotingWeightStats.get(i).getElement(j) + "," +
                            votingWeightStat.getElement(j) + "," +
                            selIndis.get(j).size());
                    writer.newLine();
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<GPNode> selBBs = new LinkedList<>();
        for (int i = 0; i < BBs.size(); i++) {
            double votingWeight = BBVotingWeightStats.get(i).getSum();

            // majority voting
            if (votingWeight > 0.5 * totalVotingWeight) {
                selBBs.add(BBs.get(i));
            }
        }

        File fcFile = new File("job." + jobSeed + ".bbs.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fcFile));

            for (GPNode BB : selBBs) {
                BuildingBlock bb = new BuildingBlock(BB);
                writer.write(bb.toString());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return selBBs;
    }

    /**
     * Find all the depth-k sub-tree as building blocks from a set of individuals.
     * @param indis the set of individuals.
     * @param depth the depth of the sub-trees/building blocks.
     * @return the building blocks.
     */
    public static List<GPNode> buildingBlocks(List<GPIndividual> indis, int depth) {
        List<GPNode> bbs = new ArrayList<>();

        for (GPIndividual indi : indis) {
            collectBuildingBlocks(bbs, indi.trees[0].child, depth);
        }

        return bbs;
    }

    /**
     * Collect all the depth-k building blocks from a tree.
     * @param buildingBlocks the set of building blocks.
     * @param tree the tree.
     * @param depth the depth of the building blocks.
     */
    public static void collectBuildingBlocks(List<GPNode> buildingBlocks,
                                             GPNode tree,
                                             int depth) {
        if (tree.depth() == depth) {
            boolean duplicate = false;

            for (GPNode bb : buildingBlocks) {
                if (GPNodeComparator.equals(tree, bb)) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate)
                buildingBlocks.add(tree);
        }
        else {
            for (GPNode child : tree.children) {
                collectBuildingBlocks(buildingBlocks, child, depth);
            }
        }
    }

    /**
     * Adapt the current population into three parts based on a changed
     * terminal set.
     * @param state the current evolution state (new terminal set).
     * @param fracElites the fraction of elite (directly copy).
     * @param fracAdapted the fraction of adapted (fix the ignored features to 1.0).
     */
    public static void adaptPopulationThreeParts(EvolutionState state,
                                                 double fracElites,
                                                 double fracAdapted) {
        List<GPNode> terminals = ((TerminalsChangable)state).getTerminals();

        Individual[] newPop = state.population.subpops[0].individuals;
        int numElites = (int)(fracElites * newPop.length);
        int numAdapted = (int)(fracAdapted * newPop.length);

        // Sort the individuals from best to worst
        Arrays.sort(newPop);

        // Part 1: keep the elites from 0 to numElite-1
//        for (int i = 0; i < numElites; i++) {
//			System.out.println("Indi " + i + ", fitness = " + newPop[i].fitness.fitness());
//        }
        // Part 2: replace the unselected terminals by 1
        for (int i = numElites; i < numElites + numAdapted; i++) {
//			System.out.println("Indi " + i + ", fitness = " + newPop[i].fitness.fitness());
            adaptTree(((GPIndividual)newPop[i]).trees[0].child, terminals);
            newPop[i].evaluated = false;
        }

        // Part 3: reinitialize the remaining individuals
        for (int i = numElites + numAdapted; i < newPop.length; i++) {
//			System.out.println("Indi " + i + ", fitness = " + newPop[i].fitness.fitness());
            newPop[i] = state.population.subpops[0].species.newIndividual(state, 0);
            newPop[i].evaluated = false;
        }
    }

    /**
     * Adapt a tree using the new terminal set.
     * @param tree the tree.
     * @param terminals the new terminal set.
     */
    private static void adaptTree(GPNode tree, List<GPNode> terminals) {
        if (tree.children.length == 0) {
            // It's a terminal
            boolean selected = false;
            for (GPNode terminal : terminals) {
                if (tree.toString().equals(terminal.toString())) {
                    selected = true;
                    break;
                }
            }

            if (!selected) {
                GPNode newTree = new ConstantTerminal(1.0);
                newTree.parent = tree.parent;
                newTree.argposition = tree.argposition;
                if (newTree.parent instanceof GPNode) {
                    ((GPNode)(newTree.parent)).children[newTree.argposition] = newTree;
                }
                else {
                    ((GPTree)(newTree.parent)).child = newTree;
                }
            }
        }
        else {
            for (GPNode child : tree.children) {
                adaptTree(child, terminals);
            }
        }
    }
}
