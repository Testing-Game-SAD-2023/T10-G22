package com.g2.t5;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.List;

@Controller
public class FileController {
    private ArrayList<String> Class = new ArrayList<>();
    
    public void listFilesInFolder(String folderPath) {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("file:" + folderPath + "/*");
            for (Resource resource : resources) {
                if (resource.isFile()) {
                    //gestisco il nome del file eliminando l'estensione
                    String fileName = resource.getFilename();
                    int extensionIndex = fileName.lastIndexOf('.');
                    if (extensionIndex > 0) {
                        String fileNameWithoutExtension = fileName.substring(0, extensionIndex);
                        //verifico che la classe non sia già stata inserita
                        if (!Class.contains(fileNameWithoutExtension)) {
                            Class.add(fileNameWithoutExtension);
                            System.out.println(fileNameWithoutExtension);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkFilesExistence(String filename) {

		String basePath = "/app/CUTRepo";  
        String filePath = basePath + File.separator + filename + File.separator + "RobotTest" + File.separator + "EvoSuiteTest";

        // VERIFICA PERCORSO
        String[] requiredFiles = { "01Level" + File.separator + "TestReport" + File.separator + "statistics.csv", "02Level" + File.separator + "TestReport" + File.separator + "statistics.csv", "03Level" + File.separator + "TestReport" + File.separator + "statistics.csv"};

        for (String requiredFile : requiredFiles) {
            System.out.println(requiredFile);
            System.out.println(filePath + File.separator + requiredFile);
            File file = new File(filePath + File.separator + requiredFile);
            if (!file.exists()) {
                System.out.println("non esiste il file");
                return false;  // Se un file richiesto non esiste, restituisci false
            }
        }
        return true;  // Tutti i file richiesti esistono
    }

    public static ArrayList<String> listFolderNames(String path) {
        ArrayList<String> folderNames = new ArrayList<>();
        
        System.out.println(path);

        File directory = new File(path);
        File[] folders = directory.listFiles(File::isDirectory);

        if (folders != null) {
            for (File folder : folders) {
                System.out.println(folder);
                folderNames.add(folder.getName());
            }
        }
        
        return folderNames;
    }
     
    public int getClassSize() { return Class.size(); }

    public String getClass(int i) { return Class.get(i); }

}

