package com.galvanize.registration;

import com.galvanize.TokenDetails;
import com.galvanize.TokenUtils;
import com.galvanize.model.PasswordRequest;
import com.galvanize.model.RegisterRequest;
import com.galvanize.security.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RegistrationController {

    Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    private final TokenUtils tokenUtils;
    private final JwtProperties jwtProperties;
    private final RegistrationDAO registrationDAO;

    public RegistrationController(TokenUtils tokenUtils, JwtProperties jwtProperties,
                                  RegistrationDAO registrationDAO) {
        this.tokenUtils = tokenUtils;
        this.jwtProperties = jwtProperties;
        this.registrationDAO = registrationDAO;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@RequestBody RegisterRequest registerRequest){
        registrationDAO.registerUser(registerRequest);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changePassword(@RequestBody PasswordRequest passwordRequest,
                               HttpServletRequest request){
        TokenDetails td = tokenUtils.getTokenDetails(request.getHeader(jwtProperties.getHeader()));
        if(td ==null) {
            throw new IllegalArgumentException();
        }
        if(!td.getUsername().equals(passwordRequest.getUsername())){
            if (!td.getAuthorities().contains("ADMIN")){
                throw new IllegalArgumentException();
            }
        }

        registrationDAO.changePassword(passwordRequest);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleUserExists(UsernameAlreadyExistsException ue){
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadData(IllegalArgumentException e){
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleException(Exception e){
    }

}
