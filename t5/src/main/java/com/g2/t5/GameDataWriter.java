package com.g2.t5;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import com.g2.Model.Game;

public class GameDataWriter {

    private static String CSV_FILE_PATH = "/app/AUTName/StudentLogin/GameId/GameData.csv";
    //private static final String[] CSV_HEADER = { "GameId", "Username", "PlayerClass", "Robot" };
    public long getGameId() {
     long gameId = 0;

    try {
        // Crea il Reader per il file CSV
        Reader reader = new FileReader(CSV_FILE_PATH);

        // Crea il CSVParser con il Reader e il formato CSV
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

        // Ottieni tutte le righe del file CSV
        List<CSVRecord> records = csvParser.getRecords();

        // Verifica se ci sono righe nel file CSV
        if (!records.isEmpty()) {
            // Prendi l'ultima riga del file CSV
            CSVRecord lastRecord = records.get(records.size() - 1);

            // Ottieni l'ID dalla prima colonna della riga
            gameId = Long.parseLong(lastRecord.get(0));
        }

        // Chiudi il CSVParser e il Reader
        csvParser.close();
        reader.close();
    } catch (IOException e) {
        System.err.println("Errore durante la lettura del file CSV: " + e.getMessage());
    }

    return gameId;
}
    public void saveGame(Game game) {
        try {
            // Crea il file CSV se non esiste
            File file = new File(CSV_FILE_PATH);
            CSV_FILE_PATH = file.getAbsolutePath();

            // Crea il Writer per il file CSV
            Writer writer = new FileWriter(CSV_FILE_PATH, true);

            // Crea il CSVPrinter con il Writer e il formato CSV
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            // Scrivi i dati dell'oggetto Game come tupla CSV nel file
            csvPrinter.printRecord(game.getGameId(), game.getUsername(), game.getPlayerClass(), game.getRobot(), game.getData_creazione(), game.getOra_creazione());

            // Chiudi il CSVPrinter e il Writer
            csvPrinter.flush();
            csvPrinter.close();
            writer.close();

            System.out.println("L'oggetto Game è stato salvato correttamente nel file CSV.");
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file CSV: " + e.getMessage());
        }
    }
}