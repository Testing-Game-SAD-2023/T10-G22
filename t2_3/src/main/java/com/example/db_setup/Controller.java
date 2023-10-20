package com.example.db_setup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.example.db_setup.Authentication.AuthenticatedUser;
import com.example.db_setup.Authentication.AuthenticatedUserRepository;

@RestController
public class Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticatedUserRepository authenticatedUserRepository;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${recaptcha.secretkey}")
    private String recaptchaSecret;

    @Value("${recaptcha.url}")
    private String recaptchaServerURL;

    @Bean 
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }
    
    @Autowired
    private RestTemplate restTemplate;


    String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{3,14}$"; // maiuscola, minuscola e numero
    Pattern p = Pattern.compile(regex);


    // Registrazione
    @PostMapping("/register")
    public RedirectView register(@RequestParam("name") String name,
                                            @RequestParam("surname") String surname,
                                            @RequestParam("email") String email,
                                            @RequestParam("password") String password,
                                            @RequestParam("check_password") String check_password,
                                            @RequestParam("studies") Studies studies,
                                            @RequestParam("g-recaptcha-response") String gRecaptchaResponse) {
        
        //verifica del recaptcha
        verifyReCAPTCHA(gRecaptchaResponse);
        
        User n = new User();

        RedirectView redirectView = new RedirectView();

        // NOME
        if ((name.length() >= 2) && (name.length() <= 30) && (Pattern.matches("[a-zA-Z]+", name))) {
            n.setName(name);
        } else {
            
            redirectView.setUrl("/register?msg=name_not_valid");
            return redirectView;
        }

        // COGNOME
        if ((name.length() >= 2) && (surname.length() <= 30) && (Pattern.matches("[a-zA-Z]+", surname))) {
            n.setSurname(surname);
        } else {

            redirectView.setUrl("/register?msg=surname_not_valid");
            return redirectView;
        }

        // EMAIL
        if ((email.contains("@")) && (email.contains("."))) {
            User user = userRepository.findByEmail(email);
            if (user != null) {
                redirectView.setUrl("/register?msg=email_in_use");
                return redirectView;
            }
            n.setEmail(email);
        } else {
            redirectView.setUrl("/register?msg=email_not_valid");
            return redirectView;
        }

        // PASSWORD
        Matcher m = p.matcher(password);

        if ((password.length() >16) || (password.length() < 8) || !(m.matches())) {

            redirectView.setUrl("/register?msg=psw_not_valid");
            return redirectView;
        }

        if (password.equals(check_password)) {
            String crypted = myPasswordEncoder.encoder().encode(password);
            n.setPassword(crypted);
        } else {

            redirectView.setUrl("/register?msg=check_psw_not_valid");
            return redirectView;
        }

        // STUDIES
        n.setStudies(studies);

        userRepository.save(n);
        Integer ID = n.getID();

        try {
            emailService.sendMailRegister(email, ID);
            
            String externalSiteUrl = "http://localhost:8080/login";
            redirectView.setUrl(externalSiteUrl);
            return redirectView;

        } catch (MessagingException e) {
            redirectView.setUrl("/register?msg=failure_registration");
            return redirectView;
        }
    }

    //Verifica del recaptcha
    private void verifyReCAPTCHA(String gRecaptchaResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secretkey", recaptchaSecret);
        map.add("response", gRecaptchaResponse);
    
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(recaptchaServerURL, request, String.class);
    
        System.out.println(response);
    }
        
    // Autenticazione
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("email") String email,
                                        @RequestParam("password") String password,
                                        HttpServletResponse response) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email not found");
        }

        boolean passwordMatches = myPasswordEncoder.matches(password, user.password);
        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
        }

        String token = generateToken(user);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user, token);
        authenticatedUserRepository.save(authenticatedUser);

        Cookie cok = new Cookie("jwt",token);
        cok.setMaxAge(3600);
        response.addCookie(cok);
        try{
            String hostIp = System.getenv("HOST_IP");
            response.sendRedirect("http://" + hostIp + ":8080/selection/main?mail="+email);
        }catch (Exception ex){
            System.out.println(ex);
        }
        return ResponseEntity.ok(token);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<Boolean> validateToke(@RequestParam("jwt") String token){
        if(isJwtValid(token)) return ResponseEntity.ok(true);
        return ResponseEntity.ok(false);
    }
    private Boolean isJwtValid(String token){
        try{
            Claims c = Jwts.parser().setSigningKey("mySecretKey").parseClaimsJws(token).getBody();
            if(new Date().before(c.getExpiration())){
                return true;
            }
        }catch (Exception ex){
            System.err.println(ex);
        }
        return false;
    }
    public static String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(1, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("userId", user.getID())
                .signWith(SignatureAlgorithm.HS256, "mySecretKey")
                .compact();

        return token;
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam("authToken") String authToken,
                                         HttpServletResponse response) {
        AuthenticatedUser authenticatedUser = authenticatedUserRepository.findByAuthToken(authToken);

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        Cookie ck = new Cookie("jwt",null);
        ck.setMaxAge(0);
        response.addCookie(ck);

        authenticatedUserRepository.delete(authenticatedUser);
        return ResponseEntity.ok("Logout successful");
    }

    //Non so se serve anche la Get

    
    //Recupera Password
    @PostMapping("/password_reset")
    public ResponseEntity<String> resetPassword(@RequestParam("email") String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found");
        }

        String resetToken = generateToken(user);
        user.setResetToken(resetToken);
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(email, resetToken);
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send password reset email");
        }

    }

    @PostMapping("/password_change")
    public ResponseEntity<String> changePassword(@RequestParam("email") String email,
                                                @RequestParam("token") String resetToken,
                                                @RequestParam("newPassword") String newPassword,
                                                @RequestParam("confirmPassword") String confirmPassword) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email not found");
        }

        if (!resetToken.equals(user.getResetToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid reset token");
        }

        Matcher m = p.matcher(newPassword);

        if ((newPassword.length() >= 15) || (newPassword.length() <= 2) || !(m.matches())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password not valid");
        }

        if (newPassword.equals(confirmPassword)) {
            String cryptedPassword = myPasswordEncoder.encoder().encode(newPassword);
            user.setPassword(cryptedPassword);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Check_Password not valid");
        }

        user.setResetToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password change successful");
    }

    // ID per il task 5
    @GetMapping("/get_ID")
    public Integer getID(@RequestParam("email") String email, @RequestParam("password") String password){
        
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return -1;
        }

        boolean passwordMatches = myPasswordEncoder.matches(password, user.password);
        if (!passwordMatches) {
            return -1;
        }

        Integer ID= user.ID;

        return ID;
    }

    /* GET PER LE VIEW */

    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView showLoginForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    
    @GetMapping("/password_reset")
    public ModelAndView showResetForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("password_reset");
        return modelAndView;
    }

    
    @GetMapping("/password_change")
    public ModelAndView showChangeForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("password_change");
        return modelAndView;
    }

    @GetMapping("/mail_register")
    public ModelAndView showMailForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("mail_register");
        return modelAndView;
    }


}

