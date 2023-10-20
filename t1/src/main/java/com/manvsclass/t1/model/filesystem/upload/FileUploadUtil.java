package com.manvsclass.t1.model.filesystem.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
public class FileUploadUtil {
	
	public static void saveCLassFile(String fileName,String cname,MultipartFile multipartFile) throws IOException {
		
		String directoryPath = "CUTRepo/"+cname+"/"+cname+"SourceCode";
	    Path directory = Paths.get(directoryPath);
	        
	    try {
	            // Verifica se la directory esiste già
	        if (!Files.exists(directory)) {
	                // Crea la directory
	            Files.createDirectories(directory);
	            System.out.println("La directory è stata creata con successo.");
	        } else {
	            System.out.println("La directory esiste già.");
	        }
	    } catch (Exception e) {
	        System.out.println("Errore durante la creazione della directory: " + e.getMessage());
	    }
	    
		Path uploadDirectory = Paths.get("CUTRepo/"+cname+"/"+cname+"SourceCode");
		
		// MODIFICHE INTEGRAZIONE

		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadDirectory.resolve(fileName);

			// Verifica se il file esiste
    		boolean fileExists = Files.exists(filePath);

			// Leggi il contenuto del file se esiste
			String fileContent = "";
			if (fileExists) {
				fileContent = new String(Files.readAllBytes(filePath));
			}

			// Verifica se la riga predefinita è già presente in tutto il file
			boolean defaultLineExists = fileContent.contains("package " + cname + "SourceCode;");

			// Aggiungi la riga predefinita solo se non è già presente
			if (!defaultLineExists) {
				String defaultLine = "package " + cname + "SourceCode;" + System.lineSeparator() + fileContent;
				try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath.toFile(), false))) {
					outputStream.write(defaultLine.getBytes());
				}
			}

			// Copia il resto del contenuto del file se esiste
			
			try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePath, StandardOpenOption.APPEND))) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1)
					outputStream.write(buffer, 0, bytesRead);
				
			}

			// MODIFICHE INTEGRAZIONE

			//Files.copy(inputStream,filePath,StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Impossibile eliminare il file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        else {
        	directory.delete();
        }
        if (!directory.delete()) {
            throw new IOException("Impossibile eliminare la cartella: " + directory.getAbsolutePath());
        }
    }
	
}

