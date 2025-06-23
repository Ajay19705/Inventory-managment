package com.Invnmgmt.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.UserDTO;
import com.Invnmgmt.enums.UserRole;
import com.Invnmgmt.models.User;
import com.Invnmgmt.repositories.UserRepository;
import com.Invnmgmt.services.UserService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/users")
public class UserController {
@Autowired
	private final UserService userService;
@Autowired
private UserRepository userRepository;

@Autowired
private ModelMapper modelMapper;

    // Manual constructor instead of Lombok @RequiredArgsConstructor
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<Response> getUserAndTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserTransactions(userId));
    }

//    @GetMapping("/current")
//    public ResponseEntity<User> getCurrentUser() {
//        return ResponseEntity.ok(userService.getCurrentLoggedInUser());
//    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = userService.getCurrentLoggedInUser();

        UserDTO dto = modelMapper.map(user, UserDTO.class);
        if (dto.getTransactions() != null) {
            dto.getTransactions().forEach(tx -> {
                tx.setUser(null);
                tx.setSupplier(null);
            });
        }

        return ResponseEntity.ok(dto);
        
        
        
    }
    
    @GetMapping("/managers")
    public ResponseEntity<List<UserDTO>> getAllManagers() {
        List<User> managers = userRepository.findByRole(UserRole.MANAGER);
        List<UserDTO> result = managers.stream()
            .map(user -> {
                UserDTO dto = modelMapper.map(user, UserDTO.class);
                dto.setTransactions(null); // 👈 remove transactions
                return dto;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
 
    
   
    
}
