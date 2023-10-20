package RemoteCCC;

import java.io.File;

public class Config {
    //private static String usr_path = System.getProperty("/app"); 
    private static String sep = File.separator;
    final static String packageDeclaration  = "package ClientProject;\n";


    final static String pathCompiler   =  sep + "ClientProject" + sep;
    final static String testingClassPath   = sep + "ClientProject" +sep + "src" + sep + "test" + sep + "java"+sep +"ClientProject" + sep;
    final static String underTestClassPath = sep + "ClientProject" +  sep + "src" +  sep + "main" +  sep + "java" +  sep + "ClientProject" + sep;
    final static String coverageFolder = sep + "ClientProject" + sep + "target" + sep + "site" + sep + "jacoco" + sep + "jacoco.xml" + sep;


    public static String getTestingClassPath ()   {return testingClassPath;}
    public static String getUnderTestClassPath()  {return underTestClassPath; }

    public static String getpathCompiler(){return pathCompiler;}
    public static String getpackageDeclaretion(){return packageDeclaration;}
    public static String getCoverageFolder(){return coverageFolder;}

}
