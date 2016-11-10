package yimei.jss.gp;

import java.io.*;
import java.util.*;

import ec.Individual;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.DoubleERC;
import yimei.jss.gp.terminal.JobShopAttribute;
import ec.EvolutionState;
import ec.gp.GPNode;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;

/**
 * The evolution state of evolving dispatching rules with GP.
 *
 * @author yimei
 *
 */

public class GPRuleEvolutionState extends SimpleEvolutionState {

	/**
	 * Read the file to specify the terminals.
	 */
	public final static String P_TERMINALS_FROM = "terminals-from";
	public final static String P_INCLUDE_ERC = "include-erc";

	protected String terminalFrom;
	protected boolean includeErc;
	protected long jobSeed;
	protected List<GPNode> terminals;

	List<Double> genTimes = new ArrayList<>();

	public List<GPNode> getTerminals() {
		return terminals;
	}

	public long getJobSeed() {
		return jobSeed;
	}

	public void setTerminals(List<GPNode> terminals) {
		this.terminals = terminals;
	}

	/**
	 * Initialize the terminal set with all the job shop attributes.
	 */
	public void initTerminalSet() {
		if (terminalFrom.equals("basic")) {
			initBasicTerminalSet();
		}
		else if (terminalFrom.equals("relative")) {
			initRelativeTerminalSet();
		}
		else {
			String terminalFile = "terminals/" + terminalFrom;
			initTerminalSetFromCsv(new File(terminalFile));
		}

		if (includeErc)
			terminals.add(new DoubleERC());
	}

	public void initBasicTerminalSet() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.basicAttributes()) {
			terminals.add(new AttributeGPNode(a));
		}
	}

	public void initRelativeTerminalSet() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
			terminals.add(new AttributeGPNode(a));
		}
	}

	public void initTerminalSetFromCsv(File csvFile) {
		terminals = new LinkedList<GPNode>();

		BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
            	JobShopAttribute a = JobShopAttribute.get(line);
				terminals.add(new AttributeGPNode(a));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	/**
	 * Return the index of an attribute in the terminal set.
	 * @param attribute the attribute.
	 * @return the index of the attribute in the terminal set.
	 */
	public int indexOfAttribute(JobShopAttribute attribute) {
		for (int i = 0; i < terminals.size(); i++) {
			JobShopAttribute terminalAttribute = ((AttributeGPNode)terminals.get(i)).getJobShopAttribute();
			if (terminalAttribute == attribute) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Randomly pick a terminal from the terminal set.
	 * @return the selected terminal, which is a GPNode.
	 */
	public GPNode pickTerminalRandom() {
    	int index = random[0].nextInt(terminals.size());
    	return terminals.get(index);
    }

	// the best individual in subpopulation
	public Individual bestIndi(int subpop) {
		int best = 0;
		for(int x = 1; x < population.subpops[subpop].individuals.length; x++)
			if (population.subpops[subpop].individuals[x].fitness.betterThan(population.subpops[subpop].individuals[best].fitness))
				best = x;

		return population.subpops[subpop].individuals[best];
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		Parameter p;

		// Get the job seed.
		p = new Parameter("seed").push(""+0);
		jobSeed = parameters.getLongWithDefault(p, null, 0);

 		p = new Parameter(P_TERMINALS_FROM);
 		terminalFrom = parameters.getStringWithDefault(p, null, "basic");

		p = new Parameter(P_INCLUDE_ERC);
		includeErc = parameters.getBoolean(p, null, false);

		initTerminalSet();

		super.setup(this, base);
	}

	@Override
	public void run(int condition)
    {
		double totalTime = 0;

		if (condition == C_STARTED_FRESH) {
			startFresh();
        }
		else {
			startFromCheckpoint();
        }

		int result = R_NOTDONE;
		while ( result == R_NOTDONE )
        {
			long start = yimei.util.Timer.getCpuTime();

			result = evolve();

			long finish = yimei.util.Timer.getCpuTime();
			double duration = (finish - start) / 1000000000;
			genTimes.add(duration);
			totalTime += duration;

			output.message("Generation " + (generation-1) + " elapsed " + duration + " seconds.");
        }

		output.message("The whole program elapsed " + totalTime + " seconds.");

		File timeFile = new File("job." + jobSeed + ".time.csv");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(timeFile));
			writer.write("Gen,Time");
			writer.newLine();
			for (int gen = 0; gen < genTimes.size(); gen++) {
				writer.write(gen + "," + genTimes.get(gen));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finish(result);
    }

    @Override
	public int evolve() {
	    if (generation > 0)
	        output.message("Generation " + generation);

	    // EVALUATION
	    statistics.preEvaluationStatistics(this);
	    evaluator.evaluatePopulation(this);
	    statistics.postEvaluationStatistics(this);

	    // SHOULD WE QUIT?
	    if (evaluator.runComplete(this) && quitOnRunComplete)
	        {
	        output.message("Found Ideal Individual");
	        return R_SUCCESS;
	        }

	    // SHOULD WE QUIT?
	    if (generation == numGenerations-1)
	        {
	        return R_FAILURE;
	        }

	    // PRE-BREEDING EXCHANGING
	    statistics.prePreBreedingExchangeStatistics(this);
	    population = exchanger.preBreedingExchangePopulation(this);
	    statistics.postPreBreedingExchangeStatistics(this);

	    String exchangerWantsToShutdown = exchanger.runComplete(this);
	    if (exchangerWantsToShutdown!=null)
	        {
	        output.message(exchangerWantsToShutdown);
	        /*
	         * Don't really know what to return here.  The only place I could
	         * find where runComplete ever returns non-null is
	         * IslandExchange.  However, that can return non-null whether or
	         * not the ideal individual was found (for example, if there was
	         * a communication error with the server).
	         *
	         * Since the original version of this code didn't care, and the
	         * result was initialized to R_SUCCESS before the while loop, I'm
	         * just going to return R_SUCCESS here.
	         */

	        return R_SUCCESS;
	        }

	    // BREEDING
	    statistics.preBreedingStatistics(this);

	    population = breeder.breedPopulation(this);

	    // POST-BREEDING EXCHANGING
	    statistics.postBreedingStatistics(this);

	    // POST-BREEDING EXCHANGING
	    statistics.prePostBreedingExchangeStatistics(this);
	    population = exchanger.postBreedingExchangePopulation(this);
	    statistics.postPostBreedingExchangeStatistics(this);

	    // Generate new instances if needed
		RuleOptimizationProblem problem = (RuleOptimizationProblem)evaluator.p_problem;
	    if (problem.getEvaluationModel().isRotatable()) {
			problem.rotateEvaluationModel();
		}

	    // INCREMENT GENERATION AND CHECKPOINT
	    generation++;
	    if (checkpoint && generation%checkpointModulo == 0)
	        {
	        output.message("Checkpointing");
	        statistics.preCheckpointStatistics(this);
	        Checkpoint.setCheckpoint(this);
	        statistics.postCheckpointStatistics(this);
	        }

	    return R_NOTDONE;
	}
}
