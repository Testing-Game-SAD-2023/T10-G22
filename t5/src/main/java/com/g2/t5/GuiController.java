package com.g2.t5;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.g2.Model.Game;
import com.g2.Model.Player;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class GuiController {

    Player p1 = Player.getInstance();
    Game g = new Game();
    long globalID;

    String valueclass = "NULL";
    String valuerobot = "NULL";
    String valueUser = "NULL";
    private Integer myClass = null;
    private Integer myRobot = null;
    private String myMail = null;
    private Map<Integer, String> hashMap = new HashMap<>();
    private Map<Integer, String> hashMap2 = new HashMap<>();
    private final FileController fileController;

    public GuiController(FileController fileController) {
        this.fileController = fileController;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Nome del template Thymeleaf per la pagina1.html
    }

    //MODIFIED: aggiunto il check dell'esistenza dei livelli per ogni CUT
    @GetMapping("/main")
    public String GUIController(@RequestParam ("mail") String mail, Model model) {

        System.out.println("sono quii");
        ArrayList<String> levels = FileController.listFolderNames("/app/CUTRepo");
        int i = 0;

        for (String folderName : levels) {
            System.out.println(folderName);

            if(FileController.checkFilesExistence(folderName)){
                hashMap.put(i, folderName);
                i = i+1;
                System.out.println("inserito nella hashmap - " + Integer.toString(i-1) + " : " + folderName);

                // System.out.println("il file ESISTE");
                // fileController.listFilesInFolder("/app/CUTRepo/" + folderName);
                // int size = fileController.getClassSize();

                // for (int i = 0; i < size; i++) {
                //     String valore = fileController.getClass(i);
                //     System.out.println(valore);
                //     hashMap.put(i, valore);
                // }
            }
        }
        
        model.addAttribute("hashMap", hashMap);
        hashMap2 = com.g2.Interfaces.t8.RobotList();
        model.addAttribute("hashMap2", hashMap2);
        
        model.addAttribute("mail", mail);

        return "main";
    }

    @PostMapping("/sendVariable")
    public ResponseEntity<String> receiveVariableClasse(@RequestParam("myVariable") Integer myClassa,
            @RequestParam("myVariable2") Integer myRobota, @RequestParam("myVariable3") String myMaila) {
        // Fai qualcosa con la variabile ricevuta
        System.out.println("Variabile ricevuta: " + myClassa);
        System.out.println("Variabile ricevuta: " + myRobota);
        System.out.println("Variabile ricevuta: " + myMaila);

        myClass = myClassa;
        myRobot = myRobota;
        myMail = myMaila;

        // Restituisci una risposta al client (se necessario)
        return ResponseEntity.ok("Dati ricevuti con successo");
    }

    @GetMapping("/report")
    public String reportPage(Model model) {

        valueclass = hashMap.get(myClass);
        valuerobot = hashMap2.get(myRobot);
        valueUser = this.myMail;

        System.out.println("IL VALORE DEL ROBOT " + valuerobot + " " + myRobot);
        System.out.println("Il VALORE DELLA CLASSE " + valueclass + " " + myClass);
        System.out.println("IL VALORE DELLA UTENTAH: " + valueUser);
        model.addAttribute("classe", valueclass);
        model.addAttribute("robot", valuerobot);
        model.addAttribute("mail", valueUser);

        return "report";
    }

    @GetMapping("/editorApi")
    public RedirectView redirectEditor(@RequestParam("difficoltà") String difficolta) {

        valueclass = hashMap.get(myClass);
        valuerobot = hashMap2.get(myRobot);
        valueUser = this.myMail;

        RedirectView redirectView = new RedirectView();

        System.out.println("IL VALORE DEL ROBOT " + valuerobot + " " + myRobot);
        System.out.println("Il VALORE DELLA CLASSE " + valueclass + " " + myClass);
        System.out.println("IL VALORE DELLA UTENTAH: " + valueUser);

        String hostIp = System.getenv("HOST_IP");
        
        String externalSiteUrl = "http://" + hostIp + ":100/?nameCUT="+valueclass+"&idUtente="+valueUser+"&difficolta="+difficolta;
        redirectView.setUrl(externalSiteUrl);
        return redirectView;
    }

    @PostMapping("/login-variabiles")
    public ResponseEntity<String> receiveLoginData(@RequestParam("var1") String username,
            @RequestParam("var2") String password) {
 
        System.out.println("username : " + username);
        System.out.println("password : " + password);
 
        p1.setUsername(username);
        p1.setPassword(password);
 
        // Salva i valori in una variabile o esegui altre operazioni necessarie
        if (com.g2.Interfaces.t2_3.verifyLogin(username, password)) {
            return ResponseEntity.ok("Dati ricevuti con successo");
        }
 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Si è verificato un errore interno");
 
    }

    @PostMapping("/save-data")
    public ResponseEntity<String> saveGame() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime oraCorrente = LocalTime.now();
        String oraFormattata = oraCorrente.format(formatter);

        GameDataWriter gameDataWriter = new GameDataWriter();
        g.setGameId(gameDataWriter.getGameId());
        g.setUsername(p1.getUsername());
        g.setPlayerClass(valueclass);
        g.setRobot(valuerobot);
        g.setData_creazione(LocalDate.now());
        g.setOra_creazione(oraFormattata);

        System.out.println(g.getUsername() + " " + g.getGameId());

        globalID = g.getGameId();

        gameDataWriter.saveGame(g);

        return ResponseEntity.ok("Oggetto creato con successo");

    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("elementId") Integer elementId) {
        // Effettua la logica necessaria per ottenere il nome del file
        // a partire dall'elementId ricevuto, ad esempio, recuperandolo dal database
        System.out.println("elementId : " + elementId);
        String filename = hashMap.get(elementId);
        System.out.println("filename : " + filename);
        String basePath = "/app/CUTRepo/" + filename + "/";
        String filePath = basePath + filename + ".java";
        System.out.println("filePath : " + filePath);
        Resource fileResource = new FileSystemResource(filePath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".java");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }

    @GetMapping("/change_password")
    public String showChangePasswordPage() {
        return "change_password";
    }

    // @GetMapping("/editor")
    // public String editorPage(Model model) {
    //     model.addAttribute("username", p1.getUsername());
    //     model.addAttribute("robot", valuerobot);
    //     model.addAttribute("classe", valueclass);

    //     model.addAttribute("gameIDj", globalID);

    //     return "editor";
    //}

}
