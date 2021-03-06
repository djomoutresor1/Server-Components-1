package com.nbp.bear.components.service;

import com.nbp.bear.components.constant.NbpConstant;
import com.nbp.bear.components.constant.NbpResponse;
import com.nbp.bear.components.model.NbpUser;
import com.nbp.bear.components.repository.NbpUserRepository;
import com.nbp.bear.components.response.NbpUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NbpUserService {

    @Autowired
    private NbpUserRepository nbpUserRepository;

    public List<NbpUser> NbpGetAllUsers() {
        return nbpUserRepository.findAll();
    }

    public ResponseEntity<Object> NbpUserRegisterService(NbpUser nbpUser) {
        Optional<NbpUser> userByUserName = nbpUserRepository.findByUserName(nbpUser.getUserName());

        if (!userByUserName.isPresent()) {
            Optional<NbpUser> userByEmail = nbpUserRepository.findByEmail(nbpUser.getEmail());
            if(!userByEmail.isPresent()) {
                nbpUserRepository.save(nbpUser);
                return new ResponseEntity<Object>(new NbpUserResponse(NbpResponse.NBP_USER_CREATED, ""), HttpStatus.CREATED);
            }
            return new ResponseEntity<Object>(new NbpUserResponse(NbpResponse.NBP_USER_EMAIL_EXISTS, ""), HttpStatus.FOUND);
        }
        return new ResponseEntity<Object>(new NbpUserResponse(NbpResponse.NBP_USER_USERNAME_EXISTS, ""), HttpStatus.FOUND);
    }

    public List<String> NbpGetRolesByLoggedInUser(Principal principal) {
        String roles = NbpGetLoggedInUser(principal).getRoles();
        List<String> assignRoles = Arrays.stream(roles.split(",")).collect(Collectors.toList());
        if (assignRoles.contains(NbpConstant.NBP_ROLE_ADMIN)) {
            return Arrays.stream(NbpConstant.NBP_ADMIN_ACCESS).collect(Collectors.toList());
        }
        if (assignRoles.contains(NbpConstant.NBP_ROLE_MODERATOR)) {
            return Arrays.stream(NbpConstant.NBP_MODERATOR_ACCESS).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public NbpUser NbpGetLoggedInUser(Principal principal) {
        return nbpUserRepository.findByUserName(principal.getName()).get();
    }

    public String NbpGetAccessService(int userId, String userRole, Principal principal) {
        NbpUser nbpUser = nbpUserRepository.findById(userId).get();
        List<String> activeRoles = NbpGetRolesByLoggedInUser(principal);
        String newRole = "";
        if (activeRoles.contains(userRole)) {
            newRole = nbpUser.getRoles() + "," + userRole;
            nbpUser.setRoles(newRole);
        }
        nbpUserRepository.save(nbpUser);
        return "Hi " + nbpUser.getUserName() + " New Role assign to you by " + principal.getName();
    }
}
