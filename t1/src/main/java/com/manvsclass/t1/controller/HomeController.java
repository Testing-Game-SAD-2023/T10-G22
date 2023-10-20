package com.manvsclass.t1.controller;

import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manvsclass.t1.model.filesystem.upload.FileUploadResponse;
import com.manvsclass.t1.model.filesystem.upload.FileUploadUtil;
import com.manvsclass.t1.model.filesystem.download.FileDownloadUtil;
import com.manvsclass.t1.model.Admin;
import com.manvsclass.t1.model.ClassUT;
import com.manvsclass.t1.model.interaction;
import com.manvsclass.t1.model.Operation;
import com.manvsclass.t1.model.repository.AdminRepository;
import com.manvsclass.t1.model.repository.ClassRepository;
import com.manvsclass.t1.model.repository.InteractionRepository;
import com.manvsclass.t1.model.repository.OperationRepository;
import com.manvsclass.t1.model.repository.SearchRepositoryImpl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
public class HomeController {
	
	@Autowired
	ClassRepository repo;
	@Autowired
	AdminRepository arepo;
	@Autowired
	InteractionRepository repo_int;
	@Autowired
	OperationRepository orepo;
	
	@Autowired
    private MongoTemplate mongoTemplate; 
	private final Admin userAdmin= new Admin("default","default","default","default");
	private final LocalDate today = LocalDate.now();
	private final SearchRepositoryImpl srepo;
	private static final String urlBaseT8 = "http://t8";
	//private static final String urlBaseT8_update = "http://t8_update";
	private static final String urlBaseT9 = "http://t9";


	
	public HomeController(SearchRepositoryImpl srepo)
	{
		this.userAdmin.setUsername("default");
		this.srepo=srepo;
	}
	
	@GetMapping("/interaction")
	public	List<interaction>	elencaInt()
	{
		return repo_int.findAll();
	}
	
	@GetMapping("/findreport")
	public	List<interaction> elencaReport()
	{
		return srepo.findReport();
	}
	
	//Solo x testing
	@GetMapping("/getLikes/{name}")
	public long likes(@PathVariable String name)
	{
		long likes=srepo.getLikes(name);
		
		return likes;
	}
	
	@PostMapping("/newinteraction")
	public interaction UploadInteraction(@RequestBody interaction interazione)
	{
		return repo_int.save(interazione);
	}

	public int API_id() {
	    Random random = new Random();
	    return random.nextInt(1000000 - 0 + 1) + 0;
	}
	
	public String API_email(int id_u) {
		
		String email = "prova."+id_u+"@email.com";
		return email;
	}
	
	@PostMapping("/newlike/{name}")
	public String newLike(@PathVariable String name) {
	    interaction newInteraction = new interaction();
	    //Finta chiamata all'API utente
	    int id_u = API_id();
	    String email_u = API_email(id_u);
	    LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String data = currentDate.format(formatter);
        
        newInteraction.setId_i(0);
	    newInteraction.setId(id_u);
	    newInteraction.setEmail(email_u);
	    newInteraction.setName(name);
	    newInteraction.setType(1);
	    newInteraction.setDate(data);
	    repo_int.save(newInteraction);

	    return "Nuova interazione di tipo 'like' inserita per la classe: " + name;
	}
	
	@PostMapping("/newReport/{name}")
	public String newReport(@PathVariable String name, @RequestBody String commento ) {
	    interaction newInteraction = new interaction();
	    
	    //Finta chiamata all'API utente
	    int id_u = API_id();
	    
	    //Finta chiamata API email
	    String email_u = API_email(id_u);
	    
	    //Generazione data del giorno
	    LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String data = currentDate.format(formatter);
        
        newInteraction.setId_i(0);
	    newInteraction.setId(id_u);
	    newInteraction.setEmail(email_u);
	    newInteraction.setName(name);
	    newInteraction.setType(0);
	    newInteraction.setDate(data);
	    newInteraction.setCommento(commento);
	    repo_int.save(newInteraction);

	    return "Nuova interazione di tipo 'report' inserita per la classe: " + name;
	}
	
	@PostMapping("/deleteint/{id_i}")
	public interaction eliminaInteraction(@PathVariable int id_i) {
		Query query= new Query(); 
	   query.addCriteria(Criteria.where("id_i").is(id_i));
	   return mongoTemplate.findAndRemove(query, interaction.class);
	}
	
	@GetMapping("/home")
	public	List<ClassUT>	elencaClassi()
	{
		return repo.findAll();
	}
	
	@GetMapping("/orderbydate")
	public List<ClassUT> ordinaClassi()
	{
		return srepo.orderByDate();
	}

	@GetMapping("/orderbyname")
	public List<ClassUT> ordinaClassiNomi()
	{
		return srepo.orderByName();
	}
	
	@GetMapping("/Cfilterby/{category}")
	public List<ClassUT> filtraClassi(@PathVariable String category)
	{
		return srepo.filterByCategory(category);
	}
	
	@GetMapping("/Cfilterby/{text}/{category}")
	public	List<ClassUT>	filtraClassi(@PathVariable String text,@PathVariable String category)
	{
		return srepo.searchAndFilter(text,category);
	}
	
	@GetMapping("/Dfilterby/{difficulty}")
	public List<ClassUT> elencaClassiD(@PathVariable String difficulty)
	{
		return srepo. filterByDifficulty(difficulty);
	}
	
	@GetMapping("/Dfilterby/{text}/{difficulty}")
	public	List<ClassUT>	elencaClassiD(@PathVariable String text,@PathVariable String difficulty)
	{
		return srepo.searchAndDFilter(text,difficulty);
	}
	

	@PostMapping("/insert")
	public ClassUT UploadClasse(@RequestBody ClassUT classe)
	{
		LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String data = currentDate.format(formatter);
        Operation operation1= new Operation((int)orepo.count(),userAdmin.getUsername(),classe.getName(),0,data);
        orepo.save(operation1);
		return repo.save(classe);
	}

	@PostMapping("/uploadFile")
	public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile,@RequestParam String model) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		ClassUT classe = mapper.readValue(model, ClassUT.class);
		
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		long size = multipartFile.getSize();
		
		FileUploadUtil.saveCLassFile(fileName,classe.getName() ,multipartFile);
		
		FileUploadResponse response = new FileUploadResponse();
		response.setFileName(fileName);
		response.setSize(size);
		response.setDownloadUri("/downloadFile");
		
		classe.setUri("CUTRepo/"+classe.getName()+"/"+ classe.getName() + "SourceCode" + "/" + fileName);
		classe.setDate(today.toString());
		LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String data = currentDate.format(formatter);
        Operation operation1= new Operation((int)orepo.count(),userAdmin.getUsername(),classe.getName(),0,data);
        orepo.save(operation1);
		repo.save(classe);

		// da aggiungere una chiamata ad un altro task

		if(post_t8(urlBaseT8 + ":6969/t1_t8", classe.getName(), classe.getName() + "SourceCode", "/CUTRepo/" + classe.getName() + "/" + classe.getName() + "SourceCode", 3) && get_t9(urlBaseT9 + ":50/t1_t9"))
			return new ResponseEntity<>(response,HttpStatus.OK);
		else
			return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@PostMapping("/delete/{name}")
	public ClassUT eliminaClasse(@PathVariable String name) {
		Query query= new Query(); 
	   query.addCriteria(Criteria.where("name").is(name));
	   this.eliminaFile(name);
	   LocalDate currentDate = LocalDate.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
       String data = currentDate.format(formatter);
       Operation operation1= new Operation((int)orepo.count(),userAdmin.getUsername(),name,2,data);
       orepo.save(operation1);
	   return mongoTemplate.findAndRemove(query, ClassUT.class);
	}

	@PostMapping("/deleteFile/{fileName}")
	public ResponseEntity<String> eliminaFile(@PathVariable String fileName) {
	  String folderPath = "CUTRepo/"+ fileName; 
	  
	        File folderToDelete = new File(folderPath);
	        if (folderToDelete.exists() && folderToDelete.isDirectory()) {
	        	try {
	        		FileUploadUtil.deleteDirectory(folderToDelete);
	                return new ResponseEntity<>("Cartella eliminata con successo.", HttpStatus.OK);
	            } catch (IOException e) {
	                return new ResponseEntity<>("Impossibile eliminare la cartella.", HttpStatus.INTERNAL_SERVER_ERROR);
	            }
	        } else {
	            return new ResponseEntity<>("Cartella non trovata.", HttpStatus.NOT_FOUND);
	        }
	 }
	    
	@GetMapping("/home/{text}")
	public	List<ClassUT>	ricercaClasse(@PathVariable String text)
	{
		return srepo.findByText(text);
	}
	
	//MODIFIED: aggiunto il check dell'esistenza dei test per la CUT
	@GetMapping("/downloadFile/{name}")
	public ResponseEntity<?> downloadClasse(@PathVariable String name) throws Exception {
		 	List<ClassUT> classe= srepo.findByText(name);

			if(checkFilesExistence(name))
		 		return FileDownloadUtil.downloadClassFile(classe.get(0).getcode_Uri(), name);
			else
				return new ResponseEntity<>("File not ready", HttpStatus.NOT_FOUND);
	}
	
	//ADDED: utility per controllare che esistano i report/livelli per una determinata CUT prima di fare il download
	// del codice (utilizzata nell'interazione con il t5 per mostrare all'utente solo le CUT pronte per esser
	// giocate)
    private static boolean checkFilesExistence(String filename) {

		String basePath = "/CUTRepo";  
        String filePath = basePath + File.separator + filename + File.separator + "RobotTest" + File.separator + "EvoSuiteTest" + File.separator;

        // VERIFICA PERCORSO
        String[] requiredFiles = { "01Level" + File.separator + "TestReport" + File.separator + "statistics.csv", "02Level" + File.separator + "TestReport" + File.separator + "statistics.csv", "03Level" + File.separator + "TestReport" + File.separator + "statistics.csv"};

        for (String requiredFile : requiredFiles) {
            File file = new File(filePath + File.separator + requiredFile);
            if (!file.exists()) {
                return false;  // Se un file richiesto non esiste, restituisci false
            }
        }
        return true;  // Tutti i file richiesti esistono
    }


	@PostMapping("/update/{name}")
		public ResponseEntity<String> modificaClasse(@PathVariable String name, @RequestBody ClassUT newContent) {
			Query query= new Query();
			
			//System.out.println("Sono QUI: " + name);
		   	query.addCriteria(Criteria.where("name").is(name));
		    Update update = new Update().set("name", newContent.getName())
	                .set("date", newContent.getDate())
	                .set("difficulty", newContent.getDifficulty())
	                .set("description", newContent.getDescription())
	                .set("category", newContent.getCategory());
		    long modifiedCount = mongoTemplate.updateFirst(query, update, ClassUT.class).getModifiedCount();

	        if (modifiedCount > 0) {
	        	LocalDate currentDate = LocalDate.now();
	    	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	            String data = currentDate.format(formatter);
	            Operation operation1= new Operation((int)orepo.count(),userAdmin.getUsername(),newContent.getName(),1,data);
	            orepo.save(operation1);
	            
				return new ResponseEntity<>("Aggiornamento eseguito correttamente.", HttpStatus.OK);
			} else
	        	return new ResponseEntity<>("Nessuna classe trovata o nessuna modifica effettuata.", HttpStatus.NOT_FOUND);
	        
	    }

	@PostMapping("/registraAdmin")
	public Admin registraAdmin(@RequestBody Admin admin1)
	{
		this.userAdmin.setUsername(admin1.getUsername());
		this.userAdmin.setPassword(admin1.getPassword());
		return arepo.save(admin1);
	}

	@PostMapping("/loginAdmin")
	public String loginAdmin(@RequestBody Admin admin1) {
		Admin admin = srepo.findAdminByUsername(admin1.getUsername());
		System.out.println(admin.getPassword());
		System.out.println(admin1.getPassword());
		if (admin.getPassword().equals(admin1.getPassword())) {   	
			this.userAdmin.setUsername(admin.getUsername());
			this.userAdmin.setPassword(admin.getPassword());
			return "ok";
		} else {
			return "utente non loggato";
		}
	}

	@GetMapping("/admins/{username}")
	public Admin getAdminByUsername(@PathVariable String username) {
		return srepo.findAdminByUsername(username);
	}

	@GetMapping("/index")
	public String getIndex() {
		return "index";
	}

	private record Parametri_gen(String nome_classe, String nome_package, String percorso_package, int n_liv) {}
	private record Parametri_upd(String nome_classe, String nome_classe_old) {}


	private static boolean post_t8(String url, String param1, String param2, String param3, int param4) {
		
		RestTemplate restTemplate = new RestTemplate(); 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> response;

		if(param4 > 0){

			Parametri_gen parametri_gen = new Parametri_gen(param1, param2, param3, param4);
			HttpEntity<Parametri_gen> requestEntity = new HttpEntity<>(parametri_gen, headers);
			response = restTemplate.postForEntity(url, requestEntity, String.class);

		} else {

			Parametri_upd parametri_upd = new Parametri_upd(param1, param2);
			HttpEntity<Parametri_upd> requestEntity = new HttpEntity<>(parametri_upd, headers);
			response = restTemplate.postForEntity(url, requestEntity, String.class);

		}
		
		if (response.getStatusCode().is2xxSuccessful())
			return true;
		else 
			return false;
				
	}

	public static boolean get_t9(String url) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		
		if (response.getStatusCode().is2xxSuccessful())
			return true;
		else 
			return false;
	}
}
