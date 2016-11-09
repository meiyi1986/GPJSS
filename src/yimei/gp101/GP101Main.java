package yimei.gp101;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

/**
 * Created by YiMei on 12/09/16.
 */
public class GP101Main {

    /** Top-level evolutionary loop.  */

    public static void main(String[] args) {
        EvolutionState state;
        ParameterDatabase parameters;

        int currentJob = 0;                             // the next job number (0 by default)

        // A this point we've finished out any previously-checkpointed job.  If there was
        // one such job, we've updated the current job number (currentJob) to the next number.
        // Otherwise currentJob is 0.

        // Now we're going to load the parameter database to see if there are any more jobs.
        // We could have done this using the previous parameter database, but it's no big deal.
        parameters = Evolve.loadParameterDatabase(args);

        currentJob = parameters.getIntWithDefault(new Parameter("current-job"), null, 0);

        int numJobs = parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        if (numJobs < 1)
            Output.initialError("The 'jobs' parameter must be >= 1 (or not exist, which defaults to 1)");


        // Now we know how many jobs remain.  Let's loop for that many jobs.  Each time we'll
        // load the parameter database scratch (except the first time where we reuse the one we
        // just loaded a second ago).  The reason we reload from scratch each time is that the
        // experimenter is free to scribble all over the parameter database and it'd be nice to
        // have everything fresh and clean.  It doesn't take long to load the database anyway,
        // it's usually small.
        for(int job = currentJob ; job < numJobs; job++)
        {
            // We used to have a try/catch here to catch errors thrown by this job and continue to the next.
            // But the most common error is an OutOfMemoryException, and printing its stack trace would
            // just create another OutOfMemoryException!  Which dies anyway and has a worthless stack
            // trace as a result.

            // try
            {
                // load the parameter database (reusing the very first if it exists)
                if (parameters == null)
                    parameters = Evolve.loadParameterDatabase(args);

                // Initialize the EvolutionState, then set its job variables
                state = Evolve.initialize(parameters, job);                // pass in job# as the seed increment
                state.output.systemMessage("Job: " + job);
                state.job = new Object[1];                                  // make the job argument storage
                state.job[0] = Integer.valueOf(job);                    // stick the current job in our job storage
                state.runtimeArguments = args;                              // stick the runtime arguments in our storage
                if (numJobs > 1)                                                    // only if iterating (so we can be backwards-compatible),
                {
                    String jobFilePrefix = "job." + job + ".";
                    state.output.setFilePrefix(jobFilePrefix);     // add a prefix for checkpoint/output files
                    state.checkpointPrefix = jobFilePrefix + state.checkpointPrefix;  // also set up checkpoint prefix
                }

                // Here you can set up the EvolutionState's parameters further before it's setup(...).
                // This includes replacing the random number generators, changing values in state.parameters,
                // changing instance variables (except for job and runtimeArguments, please), etc.




                // now we let it go
                state.run(EvolutionState.C_STARTED_FRESH);
                Evolve.cleanup(state);  // flush and close various streams, print out parameters if necessary
                parameters = null;  // so we load a fresh database next time around
            }
            /*
              catch (Throwable e)  // such as an out of memory error caused by this job
              {
              e.printStackTrace();
              state = null;
              System.gc();  // take a shot!
              }
            */
        }

//        System.exit(0);
    }
}
