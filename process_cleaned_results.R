#Run from command line with: "Rscript process_cleaned_results.R input_dir output_dir"
#eg: Rscript process_cleaned_results.R /Users/dyska/Desktop/Uni/COMP489/grid_results/cleaned/fixed-makespans-single /Users/dyska/Desktop/Uni/COMP489/grid_results/processed/fixed_makespans_single/
args = commandArgs(trailingOnly=TRUE)
if (length(args) == 0) {
  stop("At least one argument must be supplied (input file).n", call.=FALSE)
}
output_dir = args[1]
if (length(args) == 1) {
  print(paste("Only 1 argument supplied - will output results to",output_dir))
} else {
  output_dir = args[2]
}

#expecting two arguments, the file directory and the place to output results to
setwd(args[1])
filenames = list.files(args[1])

#create the matrix which will store all our results
output_matrix = matrix(, nrow = length(filenames), ncol = 5)
#output_matrix = matrix(, nrow = length(filenames), ncol = 7) - when (lb,ub) added
colnames(output_matrix) = c("filename", "best","median","mean","std dev")
#colnames(output_matrix) = c("filename","lb","ub","best","median","mean","std dev")

#lets process our results, and save them in the matrix we made
i = 1
for (filename in filenames) {
  if (endsWith(filename,".csv") && startsWith(filename,"FJSS")) {
    gp_results = read.csv(filename,header=TRUE);
    best_makespans = as.numeric(unlist(gp_results["Best"]));
    best_makespan_seed = which.min(best_makespans);
    best_makespan = best_makespans[best_makespan_seed];
    med_makespan = median(best_makespans);
    mean_makespan = mean(best_makespans);
    sd_makespan = sd(best_makespans);
    output_matrix[i,] = c(filename,best_makespan,med_makespan,mean_makespan,sd_makespan)
    i = i + 1  
  }
}

#time to save these results
setwd(output_dir)
write.csv(output_matrix,file="results.csv")

print(paste("Wrote results to directory",output_dir,"filename: results.csv"))


#let's see what a single run looked like
#should be strictly non-increasing
#seed = 19
#y = gp_results[seed,]
#x = c(0:(length(y)-1))
#plot(x,y,main=paste("Makespan for seed: ",seed),xlab="Generation number",ylab="Makespan")


