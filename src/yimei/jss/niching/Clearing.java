package yimei.jss.niching;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import yimei.jss.rule.evolved.GPRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The clearing method for niching.
 *
 * Created by YiMei on 4/10/16.
 */
public class Clearing {

    public static void clearPopulation(final EvolutionState state,
                                       double radius, int capacity,
                                       PhenoCharacterisation pc) {
        for (Subpopulation subpop : state.population.subpops) {
            // sort the individuals from best to worst
            Individual[] sortedPop = subpop.individuals;
            Arrays.sort(sortedPop);

            pc.setReferenceRule(new GPRule(((GPIndividual)sortedPop[0]).trees[0]));

            List<int[]> sortedPopCharLists = new ArrayList<>();
            for (Individual indi : sortedPop) {
                int[] charList = pc.characterise(new GPRule(((GPIndividual)indi).trees[0]));

                sortedPopCharLists.add(charList);
            }

            // clear this subpopulation
            for (int i = 0; i < sortedPop.length; i++) {
                // skip the cleared individuals
                if (((Clearable)sortedPop[i].fitness).isCleared()) {
                    continue;
                }

                int numWinners = 1;
                for (int j = i+1; j < sortedPop.length; j++) {
                    // skip the cleared individuals
                    if (((Clearable)sortedPop[j].fitness).isCleared()) {
                        continue;
                    }

                    // calculate the distance between individuals i and j
                    double distance = PhenoCharacterisation.distance(
                            sortedPopCharLists.get(i), sortedPopCharLists.get(j));
                    if (distance > radius) {
                        // Individual j is not in the niche
                        continue;
                    }

                    if (numWinners < capacity) {
                        numWinners ++;
                    }
                    else {
                        // Clear the fitness of individual j
                        ((Clearable)sortedPop[j].fitness).clear();
                    }
                }
            }
        }
    }
}
