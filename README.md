# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* This is a package for algorithms for automatic rule design in Job Shop Scheduling (JSS) using Genetic Programming (GP). Written by Yi Mei.
* The package is based on the Java ECJ package, which is available from https://cs.gmu.edu/~eclab/projects/ecj/.
* Version 1.0.0

### How do I get set up? ###

1. Download the source files in the `src/` folder and the dependencies in the `libraries/` folder.
2. Create a Java Project using `src/` and `libraries/` (the repository is a IDEA IntelliJ project by default).
3. The ECJ functions are located at `src/ec/`, and the functions for JSS and GP are in `src/yimei/jss/`. Now you are ready to run a number of different algorithms.
4. Before starting, it is highly recommended to get yourself familiar with the ECJ package, especially the GP part. You can start from the four tutorials located at `src/ec/app/tutorialx` (x = 1, ..., 4). Turorial 4 is about GP for symbolic regression, which is very useful for understanding this project. A more thorough manual can be found in https://cs.gmu.edu/~eclab/projects/ecj/docs/manual/manual.pdf.

### Project structure ###

The main project is located in `/src/yimei/jss/`. It contains the following packages:

* `algorithm/` contains a number of algorithms to run. They are good entry points to start with.
* `gp/` contains the core classes for GP for evolving dispatching rules.
* `jobshop/` contains the core classes for representing a job shop.
* `niching/` contains classes for niching-based GP, i.e. using the Clearing method.
* `rule/` contains the core classes for representing dispatching rules.
* `ruleanalysis/` contains the classes for rule analysis, e.g. reading rules from the ECJ result file, testing rules, calculating the program length, depth, number of unique terminals, etc.
* `ruleevalulation/` contains the evaluation models for dispatching rules, such as discrete event simulation, static job shop instances, etc.
* `ruleoptimisation/` contains the classes for optimisation dispatching rules, e.g. RuleOptimisationProblem.
* `simulation/` contains the classes for discrete event simulation for dynamic job shop scheduling.
* `surrogate/` contains the classes for surrogate models and evaluators.

### Running experiments ###

**Example 1 (Simple GP):**

1. Locate the param file `src/yimei/jss/algorithm/simplegp/simplegp.params`
2. Run `src/yimei/jss/gp/GPRun` with argument "*-file [pathofparamsfile]/simplegp.params*". You can also set other parameters using "*-p xxx*", in the same way as any ECJ applications.
3. Finally you will get a result file `job.[x].out.stat` in the project home directory, where [x] is the job id.

**Example 2 (Calculate feature contributions for feature selection):**

This work was published on 

Yi Mei, Mengjie Zhang, Su Nguyen, "<a href="http://homepages.ecs.vuw.ac.nz/~yimei/papers/GECCO16-MeiSuZhang.pdf">Feature Selection in Evolving Job Shop Dispatching Rules with Genetic Programming</a>," *Genetic and Evolutionary Computation Conference (GECCO)*, Denver, USA, 2016.

1. Suppose you already obtained a number of result files (e.g. by running simple GP with different random seeds). These files are named `job.[x].out.stat`, where [x] is the run id. Suppose you have 30 runs, and thus x = 0, ..., 29. All the 30 result files are stored in some directory `dir/`.
2. Run `src/yimei/ruleanalysis/RuleTestFeatureContribution` using the argument "*dir/ simple-rule 30 dynamic-job-shop missing-0.85-4 1 max-tardiness relative-terminals*".
3. Finally you will get a csv file in the project home directory, listing the contribution of each feature for the best rule of each run.

**Example 3 (GP using Surrogate model):**

The surrogate model is the HalfShop model proposed by Su Nguyen:

Nguyen, S., Zhang, M., Tan, K.C., 2016. <a href="http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=7473913">Surrogate-Assisted Genetic Programming With Simplified Models for Automated Design of Dispatching Rules</a>. IEEE Transactions on Cybernetics 1–15. doi:10.1109/TCYB.2016.2562674

1. Run the params file `src/yimei/algorithms/surrogategp/surrogategp.params`
2. The result file is the same as that obtained by simple GP.


### Who do I talk to? ###

* Email: yi.mei@ecs.vuw.ac.nz
