package yimei.jss.ruleanalysis;

import ec.gp.GPNode;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Scenario;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.evolved.GPRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuleTest {

    public static final long simSeed = 968356;

	protected String trainPath;
    protected RuleType ruleType;
    protected int numRuns;
    protected String testScenario;
    protected String testSetName;
    protected List<Objective> objectives; // The objectives to test.

    public RuleTest(String trainPath, RuleType ruleType, int numRuns,
                    String testScenario, String testSetName,
                    List<Objective> objectives) {
        this.trainPath = trainPath;
        this.ruleType = ruleType;
        this.numRuns = numRuns;
        this.testScenario = testScenario;
        this.testSetName = testSetName;
        this.objectives = objectives;
    }

    public RuleTest(String trainPath, RuleType ruleType, int numRuns,
                    String testScenario, String testSetName) {
        this(trainPath, ruleType, numRuns, testScenario, testSetName, new ArrayList<>());
    }

    public String getTrainPath() {
        return trainPath;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public int getNumRuns() {
        return numRuns;
    }

    public String getTestScenario() {
        return testScenario;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Objective> objectives) {
		this.objectives = objectives;
	}

	public void addObjective(Objective objective) {
		this.objectives.add(objective);
	}

	public void addObjective(String objective) {
		addObjective(Objective.get(objective));
	}

	public SchedulingSet generateTestSet() {
        return SchedulingSet.generateSet(simSeed, testScenario,
                testSetName, objectives, 50);
    }

	public void writeToCSV() {
        SchedulingSet testSet = generateTestSet();

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName + ".csv");

        List<TestResult> testResults = new ArrayList<>();

        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult result = TestResult.readFromFile(sourceFile, ruleType);

            File timeFile = new File(trainPath + "job." + i + ".time.csv");
            result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);

            for (int j = 0; j < result.getGenerationalRules().size(); j++) {
                result.getGenerationalRule(j).calcFitness(
                        result.getGenerationalTestFitness(j), null, testSet, objectives);

                System.out.println("Generation " + j + ": test fitness = " +
                        result.getGenerationalTestFitness(j).fitness());
            }

            long finish = System.currentTimeMillis();
            long duration = finish - start;
            System.out.println("Duration = " + duration + " ms.");

            testResults.add(result);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Run,Generation,Size,UniqueTerminals,Obj,TrainFitness,TestFitness,Time");
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                TestResult result = testResults.get(i);

                for (int j = 0; j < result.getGenerationalRules().size(); j++) {
                    GPRule rule = (GPRule)result.getGenerationalRule(j);

                    MultiObjectiveFitness trainFit =
                            (MultiObjectiveFitness)result.getGenerationalTrainFitness(j);
                    MultiObjectiveFitness testFit =
                            (MultiObjectiveFitness)result.getGenerationalTestFitness(j);

                    UniqueTerminalsGatherer gatherer = new UniqueTerminalsGatherer();
                    int numUniqueTerminals = rule.getGPTree().child.numNodes(gatherer);

                    if (objectives.size() == 1) {
                        writer.write(i + "," + j + "," +
                                rule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL) + "," +
                                numUniqueTerminals + ",0," +
                                trainFit.fitness() + "," +
                                testFit.fitness() + "," +
                                result.getGenerationalTime(j));
                        writer.newLine();
                    }
                    else {
                        writer.write(i + "," + j + "," +
                                rule.getGPTree().child.numNodes(GPNode.NODESEARCH_ALL) + "," +
                                numUniqueTerminals + ",");

                        for (int k = 0; k < objectives.size(); k++) {
                            writer.write(k + "," +
                                    trainFit.getObjective(k) + "," +
                                    testFit.getObjective(k) + ",");
                        }

                        writer.write("" + result.getGenerationalTime(j));
                        writer.newLine();
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		int idx = 0;
		String trainPath = args[idx];
        idx ++;
        RuleType ruleType = RuleType.get(args[idx]);
		idx ++;
        int numRuns = Integer.valueOf(args[idx]);
        idx ++;
        String testScenario = args[idx];
        idx ++;
        String testSetName = args[idx];
        idx ++;
		int numObjectives = Integer.valueOf(args[idx]);
		idx ++;
		RuleTest ruleTest = new RuleTest(trainPath, ruleType, numRuns, testScenario, testSetName);
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx]);
			idx ++;
		}

		ruleTest.writeToCSV();
	}
}
