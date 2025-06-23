package com.Invnmgmt.services;

import com.Invnmgmt.dtos.LoginRequest;
import com.Invnmgmt.dtos.RegisterRequest;
import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.UserDTO;
import com.Invnmgmt.models.User;

public interface UserService {

	Response registerUser(RegisterRequest registerRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    User getCurrentLoggedInUser();

    Response getUserById(Long id);

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    Response getUserTransactions(Long id);
    
    
	
}
