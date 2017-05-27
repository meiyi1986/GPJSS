package yimei.jss.gp;

import yimei.jss.rule.evolved.GPRule;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static yimei.jss.FJSSMain.getFileNames;

/**
 * Created by dyska on 21/05/17.
 */
public class GPMain {

//    //-file + path to params file
//
//    //want to evaluate every file
//
//    String path = "";
//        if (args.length > 2) {
//        //allow more specific folder or file paths to be used
//        path = args[2];
//    }
//    path = (new File("")).getAbsolutePath() + "/data/FJSS/" + path;
//    List<String> fileNames = getFileNames(new ArrayList(), Paths.get(path));

    /*
    Arguments should be a params file, number of runs per
     */
    public static void main(String[] args) {
        //TODO: use args
//        -file
//                /Users/dyska/Desktop/Uni/COMP489/GPJSS/src/yimei/jss/algorithm/simplegp/simplegp.params
//                -p
//        seed.0=2
//                -p
//        filePath=/Users/dyska/Desktop/Uni/COMP489/GPJSS/data/FJSS/Hurink_Data/Text/sdata/la10.fjs
        //example file path: Hurink_Data/Text/sdata/la10.fjs

        String path = "";
        if (args.length > 0) {
            //allow more specific folder or file paths to be used
            path = args[0];
        }
        path = (new File("")).getAbsolutePath() + "/data/FJSS/" + path;

        //will need to path the same parameters to GPRun.main()
        //include path to params file
        List<String> gpRunArgs = new ArrayList<>();
        gpRunArgs.add("-file");
        gpRunArgs.add("/Users/dyska/Desktop/Uni/COMP489/GPJSS/src/yimei/jss/algorithm/simplegp/simplegp.params");
        gpRunArgs.add("-p");

        List<String> fileNames = getFileNames(new ArrayList(), Paths.get(path));
        for (String fileName: fileNames) {
            //worry about saving output later
            gpRunArgs.add("filePath="+fileName);
            gpRunArgs.add("-p");
            for (int i = 1; i <= 30; ++i) {
                gpRunArgs.add("seed.0="+String.valueOf(i));
                //convert list to array
                GPRun.main(gpRunArgs.toArray(new String[0]));
                //now remove the seed, we will add new value in next loop
                gpRunArgs.remove(gpRunArgs.size()-1);
            }
            //now remove filePath etc
            gpRunArgs = gpRunArgs.subList(0,3);
        }
    }
}
