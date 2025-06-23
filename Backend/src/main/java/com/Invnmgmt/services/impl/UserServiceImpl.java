package com.Invnmgmt.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Invnmgmt.dtos.LoginRequest;
import com.Invnmgmt.dtos.RegisterRequest;
import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.UserDTO;
import com.Invnmgmt.enums.UserRole;
import com.Invnmgmt.exceptions.InvalidCredentialsException;
import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.User;
import com.Invnmgmt.repositories.UserRepository;
import com.Invnmgmt.security.JwtUtils;
import com.Invnmgmt.services.UserService;


@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;

    
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           ModelMapper modelMapper, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Response registerUser(RegisterRequest registerRequest) {
    	 if (userRepository.existsByEmail(registerRequest.getEmail())) {
    	        Response response = new Response();
    	        response.setStatus(400);
    	        response.setMessage("Email already exists. Please use a different email.");
    	        return response;
    	    }
    	
        UserRole role = (registerRequest.getRole() != null) ? registerRequest.getRole() : UserRole.MANAGER;
        User userToSave = new User();
        userToSave.setName(registerRequest.getName());
        userToSave.setEmail(registerRequest.getEmail());
        userToSave.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userToSave.setPhoneNumber(registerRequest.getPhoneNumber());
        userToSave.setRole(role);

        userRepository.save(userToSave);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("User was successfully registered");
        return response;
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
    	 System.out.println("Attempting login for: " + loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
        		.orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
        	System.out.println("Invalid password for: " + loginRequest.getEmail());
        	throw new InvalidCredentialsException("Password Does Not Match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("User Logged in Successfully");
        response.setToken(token);
        response.setRole(user.getRole());
        response.setExpirationTime("6 months");
        return response;
    }


    
    @Override
    public Response getAllUsers() {
        // Get all users sorted by ID descending
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // Get the currently logged-in username (email)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Filter: only users with role MANAGER and not the currently logged-in admin
        List<User> filteredUsers = users.stream()
                .filter(user -> user.getRole() == UserRole.MANAGER && !user.getEmail().equals(currentUserEmail))
                .toList();

        // Remove transactions to avoid recursion
        filteredUsers.forEach(user -> user.setTransactions(null));

        // Map to DTOs
        List<UserDTO> userDTOS = modelMapper.map(filteredUsers, new TypeToken<List<UserDTO>>() {}.getType());

        // Clean nested DTO relationships
        userDTOS.forEach(dto -> {
            if (dto.getTransactions() != null) {
                dto.getTransactions().forEach(tx -> {
                    tx.setUser(null);
                    tx.setSupplier(null);
                });
            }
        });

        // Prepare response
        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setUsers(userDTOS);
        return response;
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        user.setTransactions(null);
        return user;
    }

    @Override
    public Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);      
        if (userDTO.getTransactions() != null) {
            userDTO.getTransactions().forEach(tx -> {
                tx.setUser(null);
                tx.setSupplier(null);
            });
        }
        
        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setUser(userDTO);
        return response;
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("User successfully updated");
        return response;
    }


    
    @Override
    public Response deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Manager Not Found"));

        // Check if the user is a manager
        if (!user.getRole().equals(UserRole.MANAGER)) {
            throw new IllegalStateException("Only users with role MANAGER can be deleted.");
        }

        // Check if the manager has any transactions
        if (user.getTransactions() != null && !user.getTransactions().isEmpty()) {
            throw new IllegalStateException("Cannot delete manager with existing transactions.");
        }
         userRepository.deleteById(id);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Manager successfully deleted");
        return response;
    }
    
    @Override
    public Response getUserTransactions(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (userDTO.getTransactions() != null) {
            userDTO.getTransactions().forEach(transactionDTO -> {
                transactionDTO.setUser(null);
                transactionDTO.setSupplier(null);
            });
        }
        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setUser(userDTO);
        return response;
    }
}
