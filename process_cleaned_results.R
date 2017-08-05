#Run from command line with: "Rscript process_cleaned_results.R folder_name
#eg: Rscript process_cleaned_results.R fixed_makespans_single

args = commandArgs(trailingOnly=TRUE)
if (length(args) == 0) {
  stop("At least one argument must be supplied", call.=FALSE)
}

base_directory = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/"
grid_directory = paste(base_directory, "grid_results/",sep="")
input_dir = paste(grid_directory,"cleaned/",sep="")
output_dir = paste(grid_directory,"processed/",sep="")
bounds_dir = paste(base_directory,"fjss_bounds/",sep="")
output_file = paste(args[1],"-results.csv",sep="")

input_dir = paste(input_dir, args[1], sep="")
output_dir = paste(output_dir, args[1], sep="")

#lets load in the lower and upper bounds for each fjss instance
setwd(bounds_dir)
fjss_bounds = read.csv("fjss_bounds.csv",header=TRUE)

#now lets read in filenames from input directory
setwd(input_dir)
filenames = list.files(input_dir)

#create the matrix which will store all our results
output_matrix = matrix(, nrow = length(filenames), ncol = 8)
colnames(output_matrix) = c("filename","lb","ub","best/lb","best","median","mean","std dev")

#lets process our results, and save them in the matrix we made

i = 1
for (filename in filenames) {
  if (endsWith(filename,".csv") && startsWith(filename,"FJSS")) {
    #shorten the file name to match instance names in bounds table
    instance_name = substring(filename,6,nchar(filename)-4)

    #get the bounds out of the table
    bounds = fjss_bounds[which(fjss_bounds["File"] == instance_name),]
    lb = bounds[["LB"]]
    ub = bounds[["UB"]]

    gp_results = read.csv(filename,header=TRUE)
    best_makespans = as.numeric(unlist(gp_results["Best"]))
    best_makespan_seed = which.min(best_makespans)
    best_makespan = best_makespans[best_makespan_seed]
    best_by_lb = best_makespan/lb
    med_makespan = median(best_makespans)
    mean_makespan = mean(best_makespans)
    sd_makespan = sd(best_makespans)
    output_matrix[i,] = c(filename,lb,ub,best_by_lb,best_makespan,med_makespan,mean_makespan,sd_makespan)
    i = i + 1
  }
}

#time to save these results
setwd(output_dir)
write.csv(output_matrix,file=output_file)

print(paste("Wrote results to directory",output_dir,"filename: ",output_file))




