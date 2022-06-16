package com.FATCA.API.user;

import com.FATCA.API.fileStorage.FilesStorageService;
import com.FATCA.API.security.JWTUtility;
import com.FATCA.API.table.DataTable;
import com.FATCA.API.table.DataTableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/*
* The user controller contains the methods to make the http requests
* */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Slf4j
@CrossOrigin
public class UserController  {
    //DONE:upload csv file
    //DONE:get all the csv files
    //DONE:convert a csv file into an xml file (not zipped)
    //DONE: convert and zip the xml file with a password
    //DONE: get an existing csv file
    //DONE: save a csv file
    private final UserService userService;
    private final JWTUtility jwtUtility;
    private final DataTableService dataTableService;
    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }
    @PostMapping("/user/save")
    public ResponseEntity<AppUser> saveUser(@RequestBody UserInfo user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("api/v1/user/save").toUriString());
        AppUser newUser = new AppUser(user.getUsername(), user.getPassword(), user.getRole());
        return ResponseEntity.created(uri).body(userService.saveUser(newUser));
    }
    @GetMapping("/admin/role")
    public ResponseEntity<List<String>> getRoles(){
        return ResponseEntity.ok().body(Roles.getRoles());

    }
    @PostMapping("/admin/user/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String userId){
        System.out.println(userId);
        userService.deleteUser(Long.parseLong(userId));
        return ResponseEntity.ok().body("deleted");
    }

    @PostMapping("/admin/user/update")
    public ResponseEntity<?> updateUser(@RequestBody RoleToUser form) {
        userService.addRole(form.getUsername(), form.getRoleName(), form.getNewUsername());
        return ResponseEntity.ok().body("yes");
    }
    //this method is to refresh the token when it expires..
    @GetMapping("/token/refresh")
    public void refreshToken (HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {
                String refresh_token = jwtUtility.getToken(authorizationHeader);
                String username = jwtUtility.getUsername(refresh_token);
                System.out.println("username "+ username);
                UserDetails user = userService.loadUserByUsername(username);

                Map<String, String> tokens = jwtUtility.generateTokens(user, request.getRequestURL().toString());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }catch (Exception e){
                log.error("error loggin in {}", e.getMessage());
                response.setHeader("error", e.getMessage());
                response.setStatus( FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                response.setContentType(APPLICATION_JSON_VALUE);
                error.put("error_message", e.getMessage());
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }

        }else {
            throw new RuntimeException("refresh token missing");
        }
    }

    //upload csv file
    @PostMapping("/uploadcsv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file, String username) throws Exception {
        ArrayList<String[]> data = dataTableService.csvToArray(file);
        ArrayList<List<HashMap<String, String>>> obj = dataTableService.test(data);
        AppUser user = userService.getUser(username);
         DataTable table = dataTableService.addTable(new DataTable(user, obj, file.getOriginalFilename()));
        return ResponseEntity.ok().body(table);
    }


}
@Data
class RoleToUser {
    private String newUsername;
    private String username;
    private String roleName;
}
@Data
class  FileInfo {
    private DataTable dataTable;
    private String fileName;
}
@Data
class UserInfo {
    private String username;
    private String password;
    private String role;
}