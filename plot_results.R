#Run from command line with: "Rscript process_cleaned_results.R folder_name
#eg: Rscript process_cleaned_results.R fixed_makespans_single

base_directory = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/"
grid_directory = paste(base_directory, "grid_results/",sep="")
input_dir = paste(grid_directory,"cleaned/",sep="")
bounds_dir = paste(base_directory,"fjss_bounds/",sep="")
folder_name = "fixed_makespans_single"
input_dir = paste(input_dir, folder_name, sep="")

#lets load in the lower and upper bounds for each fjss instance
setwd(bounds_dir)
fjss_bounds = read.csv("fjss_bounds.csv",header=TRUE)

#now lets read in filenames from input directory
setwd(input_dir)
filenames = list.files(input_dir)

#lets check out some graphs
file_num = 251
filename = filenames[file_num]
#shorten the file name to match instance names in bounds table
instance_name = substring(filename,6,nchar(filename)-4)

#get the bounds out of the table
bounds = fjss_bounds[which(fjss_bounds["File"] == instance_name),]
lb = bounds[["LB"]]
ub = bounds[["UB"]]

gp_results = read.csv(filename,header=TRUE)
seed = 19
y = gp_results[seed,]
x = c(0:(length(y)-1))
title = paste(filename,"\nMakespan for seed: ",seed,sep="")
ymax = max(y)
ymin = min(min(y),lb)
plot(x,y,main=title,xlab="Generation number",ylab="Makespan", ylim=c(ymin, ymax))
abline(h = lb, col = "red")
if (lb != ub) {
  abline(h = ub, col = "red")  
}
