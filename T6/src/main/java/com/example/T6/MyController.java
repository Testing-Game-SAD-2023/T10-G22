package com.example.T6;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.apache.http.client.config.RequestConfig;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;


 
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;
import org.apache.http.client.utils.URIBuilder;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
 
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Collections;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin
@Controller
public class MyController {
    private final RestTemplate restTemplate;

    @Autowired
    public MyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String indexPage(){
    return "index";
    }

    //MODIFIED: consente l'interazione con l'api esposta dal T1 per il download del corpo 
    // del file salvato su volume CUTRepo
    @GetMapping("/receiveClassUnderTest")
    public ResponseEntity<String> receiveClassUnderTest(@RequestParam("nomeCUT") String nomeCUT) {
        String url = "http://t1:90/downloadFile/" + nomeCUT;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            // Imposta gli header accettati
            httpGet.setHeader(HttpHeaders.ACCEPT, "text/x-java-source");

            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                HttpEntity responseEntity = response.getEntity();
                byte[] fileContent = EntityUtils.toByteArray(responseEntity);

                String javaCode = new String(fileContent, StandardCharsets.UTF_8);

                HttpHeaders headers = new HttpHeaders();
                headers.add("Access-Control-Allow-Origin", "*"); // Consentire da tutte le origini (* per qualsiasi origine)
                headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE"); // Metodi consentiti
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Header consentiti
                headers.add("Access-Control-Max-Age", "3600"); // Durata massima della cache delle risposte CORS
                return ResponseEntity.ok().headers(headers).body(javaCode);

                //return new ResponseEntity<>(javaCode, HttpStatus.OK);
            } else {
                System.err.println("Errore durante il recupero del file Java: ");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            System.err.println("Errore durante la richiesta HTTP: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private final HttpClient httpClient = HttpClientBuilder.create().build();

    //MODIFIED: aggiunti i campi user e difficoltà per la gestione del salvataggio su filesystem del 
    // corpo della testing class dell'utente: ad ogni sessione dell'utente, all'atto della compilazione 
    // verrà creato su filesystem al path /editorFileLog/user il file TestingClass.java (rinominato opportunamente)
    // per l'interazione con T8
    private static class RequestData {
 
        private String testingClassName;
        private String testingClassCode;
        private String underTestClassName;
        private String underTestClassCode;
 
        private String user;
        private int difficolta;
        private String nameCUT;
 
        public String getNameCUT() {
            return this.nameCUT;
        }

        public String getTestingClassName() {
            return this.testingClassName;
        }
 
        public String getuser() {
            return this.user;
        }
        public String getTestingClassCode() {
            return this.testingClassCode;
        }
 
        public String getUnderTestClassName() {
            return this.underTestClassName;
        }
 
        public String getUnderTestClassCode() {
            return this.underTestClassCode;
        }

        public int getDifficolta() {
            return this.difficolta;
        }
 
        public void setTestingClassName(String newName) {
            this.testingClassName = newName;
        }
    }
 
    //ADDED ~ integrazione T7 / T8: richiama la api compile and codecoverage messa a disposizione dal task
    // T7 per la compilazione del 
    @PostMapping("/sendInfo") // COMPILA IL CODICE DELL'UTENTE E RESTITUISCE OUTPUT DI COMPILAZIONE CON MVN
    public ResponseEntity<String> handleSendInfoRequest(@RequestBody RequestData requestData) {
        try {
 
            HttpPost httpPost = new HttpPost("http://t7:1234/compile-and-codecoverage");
            JSONObject obj = new JSONObject();
            obj.put("testingClassCode", requestData.getTestingClassCode());
            obj.put("underTestClassName", requestData.getUnderTestClassName());
            obj.put("underTestClassCode", requestData.getUnderTestClassCode());
 
            String className = getTestingClassName(requestData.getTestingClassCode());
            System.out.println("classname: "+className);
            
            if(className != null)
                obj.put("testingClassName", className);
                
            else {
                System.out.println("Classe principale non trovata.");
                obj.put("testingClassCode", requestData.getTestingClassName());
            }

            String testingClassNameNoTest = className.substring(0, className.length() - 5);

            generateJavaFile(requestData.getTestingClassCode(),className,"/editorFileLog/"+requestData.getuser()+"/"+testingClassNameNoTest+"SourceCode");
                
            StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(jsonEntity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(responseBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            String out_string = responseObj.getString("outCompile");

            if(!className.equals(requestData.getUnderTestClassName()+"_test"))
                out_string = "La main class deve essere "+ requestData.getUnderTestClassName() + "_test";
                
            return new ResponseEntity<>(out_string, headers, HttpStatus.OK);
        
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage()); // Log the error message
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //ADDED ~ integrazione T8: func per salvare su file .java il corpo della testing class dell'utente
    // per consentire la chiamata all'api del task T8 relativa la misura di coverage
    private void generateJavaFile(String body, String nomefile, String path){

        System.out.println("path: " + path + "; nomefile: " + nomefile);
        if(!Files.exists(Paths.get(path+"/"+nomefile+".java"))){
            try{
                Files.createDirectories(Path.of(path+"/"+nomefile).getParent());
                Files.createFile(Paths.get(path+"/"+nomefile+".java"));
            }catch (Exception ex){
                System.out.println("Errore nella creazione del file java");
                ex.printStackTrace();
            }
        }
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path+"/"+nomefile+".java")))){
            writer.println(body);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //ADDED: dato il corpo della testing class dell'utente, estrapola il nome della classe principale
    // per rinominare il file .java
    private static String getTestingClassName(String bodyTestingClass) {
        Pattern pattern = Pattern.compile("(public|private|protected|static)?\\s*class\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*(extends [A-Za-z_][A-Za-z0-9_]*)?\\s*");
        Matcher matcher = pattern.matcher(bodyTestingClass);
    
        // Trova la prima occorrenza della dichiarazione di classe
        String className = null;
        while (matcher.find()) {
            className = matcher.group(2);
            System.out.println("Nome della classe principale: " + className);
            return className;
        }
        return null;
    }
    
    private static String compareCSVFiles(String userFilePath, String robotFilePath) {
        String comparisonResult = "Confronto finale delle Coverage UTENTE vs ROBOT\n";
        int userWins = 0;
        int robotWins = 0;

        try (BufferedReader readerA = new BufferedReader(new FileReader(userFilePath));
             BufferedReader readerB = new BufferedReader(new FileReader(robotFilePath))) {

            String lineA, lineB;
            readerA.readLine(); // Ignora l'intestazione
            readerB.readLine();

            while ((lineA = readerA.readLine()) != null && (lineB = readerB.readLine()) != null) {
                String[] partsA = lineA.split(",");
                String[] partsB = lineB.split(",");
                String targetClassA = partsA[1];
                String targetClassB = partsB[1];
                int coveredGoalsA = Integer.parseInt(partsA[4]);
                int coveredGoalsB = Integer.parseInt(partsB[4]);

                String winner;
                if (coveredGoalsA > coveredGoalsB) {
                    winner = "user";
                    userWins++;
                } else if (coveredGoalsA < coveredGoalsB) {
                    winner = "robot";
                    robotWins++;
                } else {
                    winner = "pareggio";
                }

                comparisonResult += targetClassA + ">\t\t" + coveredGoalsA + "\t\t:\t\t" + coveredGoalsB + "\t\t" + winner + "\n";
            }

            comparisonResult += userWins > robotWins
                ? "Vince UTENTE"
                : userWins < robotWins
                ? "Vince ROBOT"
                : "PAREGGIO";

        } catch (IOException e) {
            e.printStackTrace();
        }

        return comparisonResult;
    }
 
    private static void deleteFolderContents(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Se è una sottocartella, elimina il suo contenuto in modo ricorsivo
                    deleteFolderContents(file);
                }
                if (file.delete()) {
                    System.out.println("Eliminato: " + file.getAbsolutePath());
                } else {
                    System.err.println("Impossibile eliminare: " + file.getAbsolutePath());
                }
            }
        }
    }

    @PostMapping("/misuraCoverageT8")
    public ResponseEntity<String> misuraCoverage(@RequestBody RequestData requestData){

        String testingClassName = getTestingClassName(requestData.getTestingClassCode());
        String testingClassNameNoTest = testingClassName.substring(0, testingClassName.length() - 5);
        String pathTestingClass = "/editorFileLog/"+requestData.getuser()+"/"+testingClassNameNoTest+"SourceCode/"+testingClassName+".java";
        System.out.println("Path Class Test: " + pathTestingClass);

        String pathCUT = "/CUTRepo/"+requestData.getNameCUT()+"/"+requestData.getNameCUT()+"SourceCode/"+requestData.getNameCUT()+".java";
        System.out.println("CUT path: " + pathCUT);

        String pathStatistics = "/editorFileLog/"+requestData.getuser();
        System.out.println("path statistics: " + pathStatistics);

        String livello;
        if (requestData.getDifficolta() == 1)
            livello = "01Level";
        else if (requestData.getDifficolta() == 2)
            livello = "02Level";
        else
            livello = "03Level";

        String url = "http://t8_mis:3000/api/" + pathCUT + "+" + pathTestingClass + "+" + pathStatistics;
        
        int timeoutMilliseconds = 540000; // 9 mins
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false); // Disabilita la streaming
        requestFactory.setConnectTimeout(timeoutMilliseconds);
        requestFactory.setReadTimeout(timeoutMilliseconds);

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		
		if (response.getStatusCode().is2xxSuccessful()){
                    
            String runResult = compareCSVFiles(pathStatistics + "/statistics.csv", "/CUTRepo/" + requestData.getUnderTestClassName() + "/RobotTest/EvoSuiteTest/" + livello + "/TestReport/statistics.csv");
            System.out.println(runResult);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            //Delete del contenuto della cartella dell'user
            String folderPath = "/editorFileLog/"+requestData.getuser();

            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                deleteFolderContents(folder);
                // Dopo aver eliminato i contenuti, elimina la cartella stessa
                if (folder.delete()) {
                    System.out.println("Cartella eliminata con successo.");
                } else {
                    System.err.println("Impossibile eliminare la cartella.");
                }
            } else {
                System.err.println("La cartella specificata non esiste o non è una directory.");
            }

            return new ResponseEntity<>(runResult, headers, HttpStatus.OK);

        } else {
            System.err.println("Errore durante la misura della coverage.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] getFileBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    // @PostMapping("/getResultXml")
    // public String handleGetResultXmlRequest() {
    // // try {
    // // // Esegui la richiesta HTTP al servizio di destinazione
    // // HttpPost httpPost = new HttpPost("URL_DEL_SERVIZIO_DESTINAZIONE");
    // // JSONObject obj = new JSONObject();
    // // obj.put("testingClassName", request.getParameter("testingClassName"));
    // // obj.put("testingClassCode", request.getParameter("testingClassCode"));
    // // obj.put("underTestClassName", request.getParameter("underTestClassName"));
    // // obj.put("underTestClassCode", request.getParameter("underTestClassCode"));
    // // StringEntity jsonEntity = new StringEntity(obj.toString(),
    // // ContentType.APPLICATION_JSON);
    // // httpPost.setEntity(jsonEntity);
    // // HttpResponse targetServiceResponse = httpClient.execute(httpPost);
    // // // Verifica lo stato della risposta
    // // int statusCode = targetServiceResponse.getStatusLine().getStatusCode();
    // // if (statusCode == HttpStatus.OK.value()) {
    // // // Leggi il contenuto del file XML dalla risposta
    // // HttpEntity entity = targetServiceResponse.getEntity();
    // // String compileContent = EntityUtils.toString(entity);
    // // String responseBody = EntityUtils.toString(entity);
    // // JSONObject responseObj = new JSONObject(responseBody);
    // // // Restituisci il contenuto del file XML come risposta al client
    // // return xmlContent;
    // // } else {
    // // // Restituisci un messaggio di errore al client
    // // return "Errore durante il recupero del file XML.";
    // // }
    // // } catch (Exception e) {
    // // // Gestisci eventuali errori e restituisci un messaggio di errore al
    // client
    // // return "Si è verificato un errore durante la richiesta del file XML.";
    // // }
    // }
    // FUNZIONE CHE DOVREBBE RICEVERE I RISULTATI DEI ROBOT

    @PostMapping("/run") // NON ESISTE NESSUN INTERFACCIA VERSO I COMPILATORI DEI ROBOT EVOSUITE E RANDOOP
    public ResponseEntity<String> runner(HttpServletRequest request) {
        try {
            // Esegui la richiesta HTTP al servizio di destinazione
            // RISULTATI UTENTE VERSO TASK 7
            HttpPost httpPost = new HttpPost("http://remoteccc-app-1:1234/compile-and-codecoverage");

            JSONObject obj = new JSONObject();
            obj.put("testingClassName", request.getParameter("testingClassName"));
            obj.put("testingClassCode", request.getParameter("testingClassCode"));
            obj.put("underTestClassName", request.getParameter("underTestClassName"));
            obj.put("underTestClassCode", request.getParameter("underTestClassCode"));

            StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

            httpPost.setEntity(jsonEntity);

            HttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                System.out.println("Errore in compilecodecoverage");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpEntity entity = response.getEntity();

            String responseBody = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(responseBody);

            String xml_string = responseObj.getString("coverage");
            String outCompile = responseObj.getString("outCompile");
            // PRESA DELLO SCORE UTENTE
            int userScore = ParseUtil.LineCoverage(xml_string);

            // RISULTATI ROBOT VERSO TASK4
            URIBuilder builder = new URIBuilder("http://t4-g18-app-1:3000/robots");
            builder.setParameter("testClassId", request.getParameter("testClassId"))
                    .setParameter("type", request.getParameter("type"))
                    .setParameter("difficulty", request.getParameter("difficulty"));

            HttpGet get = new HttpGet(builder.build());
            response = httpClient.execute(get);
            get.releaseConnection();
            // Verifica lo stato della risposta
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                System.out.println("Errore in robots");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Leggi il contenuto dalla risposta
            entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            responseObj = new JSONObject(responseBody);

            String score = responseObj.getString("scores");
            Integer roboScore = Integer.parseInt(score);

            // conclusione e salvataggio partita
            // chiusura turno con vincitore
            HttpPut httpPut = new HttpPut("http://t4-g18-app-1:3000/turns/" + String.valueOf(request.getParameter("turnId")));

            obj = new JSONObject();
            obj.put("scores", String.valueOf(userScore));

            if (roboScore > userScore) {
                obj.put("isWinner", false);
            } else {
                obj.put("isWinner", true);
            }
            String time = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            obj.put("closedAt", time);

            jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

            httpPut.setEntity(jsonEntity);

            response = httpClient.execute(httpPut);
            httpPut.releaseConnection();

            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                System.out.println("Errore in put turn");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // chiusura round
            httpPut = new HttpPut("http://t4-g18-app-1:3000/rounds/" + String.valueOf(request.getParameter("roundId")));

            obj = new JSONObject();

            obj.put("closedAt", time);

            jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

            httpPut.setEntity(jsonEntity);

            response = httpClient.execute(httpPut);
            httpPut.releaseConnection();

            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                System.out.println("Errore in put round");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // chiusura gioco
            httpPut = new HttpPut("http://t4-g18-app-1:3000/games/" + String.valueOf(request.getParameter("gameId")));

            obj = new JSONObject();
            obj.put("closedAt", time);

            jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

            httpPut.setEntity(jsonEntity);

            response = httpClient.execute(httpPut);
            httpPut.releaseConnection();
            
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                System.out.println("Errore in put game");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // costruzione risposta verso task5
            JSONObject result = new JSONObject();
            result.put("outCompile", outCompile);
            result.put("coverage", xml_string);
            result.put("win", userScore >= roboScore);
            result.put("robotScore", roboScore);
            result.put("score", userScore);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(result.toString(), headers, HttpStatus.OK);
        } catch (Exception e) {
            // Gestisci eventuali errori e restituisci un messaggio di errore al client
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // FUNZIONE CHE DOVREBBE RICEVERE I RISULTATI DEI ROBOT
    // @GetMapping("/getResultRobot")
    // public String handleGetResultRobotRequest() {
    //     try {
    //         // Esegui la richiesta HTTP al servizio di destinazione
    //         HttpGet httpGet = new HttpGet("URL_DEL_SERVIZIO_DESTINAZIONE");
    //         HttpResponse targetServiceResponse = httpClient.execute(httpGet);
    //         // Verifica lo stato della risposta
    //         int statusCode = targetServiceResponse.getStatusLine().getStatusCode();
    //         if (statusCode == HttpStatus.OK.value()) {
    //             // Leggi il contenuto del file XML dalla risposta
    //             HttpEntity entity = targetServiceResponse.getEntity();
    //             String xmlContent = EntityUtils.toString(entity);
    //             // Restituisci il contenuto del file XML come risposta al client
    //             return xmlContent;
    //         } else {
    //             // Restituisci un messaggio di errore al client
    //             return "Errore durante il recupero del file XML.";
    //         }
    //     } catch (Exception e) {
    //         // Gestisci eventuali errori e restituisci un messaggio di errore al client
    //         return "Si è verificato un errore durante la richiesta del file XML.";
    //     }
    // }

    @PostMapping("/getJaCoCoReport")
    public ResponseEntity<String> getJaCoCoReport(HttpServletRequest request) {
        try {
            HttpPost httpPost = new HttpPost("http://remoteccc-app-1:1234/compile-and-codecoverage");

            JSONObject obj = new JSONObject();

            obj.put("testingClassName", request.getParameter("testingClassName"));
            obj.put("testingClassCode", request.getParameter("testingClassCode"));
            obj.put("underTestClassName", request.getParameter("underTestClassName"));
            obj.put("underTestClassCode", request.getParameter("underTestClassCode"));

            StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

            httpPost.setEntity(jsonEntity);

            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() > 299) {
                System.err.println("Erorre compilazione");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject responseObj = new JSONObject(responseBody);

            String xml_string = responseObj.getString("coverage");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            // headers.setContentDisposition(ContentDisposition.attachment().filename("index.html").build());

            return new ResponseEntity<>(xml_string, headers, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // @PostMapping("/inviaDatiEFile")
    // public ResponseEntity<String> handleInviaDatiEFileRequest(
    //         @RequestParam("idUtente") String idUtente,
    //         @RequestParam("idPartita") String idPartita,
    //         @RequestParam("idTurno") String idTurno,
    //         @RequestParam("robotScelto") String robotScelto,
    //         @RequestParam("difficolta") String difficolta,
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam("playerTestClass") String playerTestClass) {
    //     try {
    //         // Creazione di una richiesta HTTP POST al servizio di destinazione
    //         HttpPost httpPost = new HttpPost("URL_DEL_SERVIZIO_DESTINAZIONE");// CHIAMA UPDATE TURN TASK4
    //         // Creazione del corpo della richiesta multipart
    //         MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
    //                 .addTextBody("idUtente", idUtente)
    //                 .addTextBody("idPartita", idPartita)
    //                 .addTextBody("idTurno", idTurno)
    //                 .addTextBody("robotScelto", robotScelto)
    //                 .addTextBody("difficolta", difficolta)
    //                 .addTextBody("playerTestClass", playerTestClass); // Aggiungi la classe Java come parte del corpo
    //                                                                   // della richiesta
    //         // .addBinaryBody("file", file.getBytes(), ContentType.APPLICATION_OCTET_STREAM,
    //         // file.getOriginalFilename());
    //         // Esecuzione della richiesta HTTP al servizio di destinazione
    //         HttpResponse targetServiceResponse = httpClient.execute(httpPost);
    //         // Restituisci una risposta di successo
    //         return ResponseEntity.ok("Dati, file e classe Java inviati con successo");
    //     } catch (Exception e) {
    //         // Gestisci eventuali errori e restituisci una risposta di errore
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Errore durante l'invio dei dati, del file e della classe Java");
    //     }
    // }
}