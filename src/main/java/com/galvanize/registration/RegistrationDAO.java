package com.galvanize.registration;

import com.galvanize.model.PasswordRequest;
import com.galvanize.model.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings({"ConstantConditions", "SqlResolve"})
@Service
public class RegistrationDAO {

    Logger LOGGER = LoggerFactory.getLogger(RegistrationDAO.class);

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegistrationDAO(JdbcTemplate jdbcTemplate, BCryptPasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username){
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)",
                Boolean.class, username);
    }

    public void registerUser(RegisterRequest request){
        if (usernameExists(request.getUsername())){
            throw new UsernameAlreadyExistsException();
        }
        jdbcTemplate.update("insert into users(first_name, last_name, username, email, password) " +
                                           "values(?,?,?,?,?)",
                request.getFirstName(), request.getLastName(), request.getUsername(),
                request.getEmail(), passwordEncoder.encode(request.getPassword()));
        long userid = findUserIdByUsername(request.getUsername());
        Long userRoleId = -1L;
        for(Role role : getRoles()){
            if (role.name.contains("USER")){
                userRoleId = role.id;
                break;
            }
        }
        if(userRoleId < 0){
            LOGGER.error("USER role not found in table");
        }
        jdbcTemplate.update("insert into user_roles (user_id, role_id) values( ?, ?)", userid, userRoleId);

    }

    public void changePassword(PasswordRequest request) {
        if(!authenticate(request.getUsername(), request.getCurrentPassword())){
            LOGGER.error("Username or password or combination is invalid");
            throw new UserAuthenticationException();
        }
        int i = jdbcTemplate.update("UPDATE users SET password = ? where username = ?",
                passwordEncoder.encode(request.getNewPassword()) ,
                request.getUsername());
        if (i != 1){
            LOGGER.warn(String.format("Failed to update password for user %s",request.getUsername()));
            throw new RegistrationFailureException();
        }
    }

    private boolean authenticate(String username, String password){
        User user = findUser(username);
        return passwordEncoder.matches(password, user.password);
    }

    private User findUser(String username){
        return jdbcTemplate.queryForObject("select id, username, password from users where username = ?",
                (rs,rowNum) -> new User(rs.getLong("id"), rs.getString("username"), rs.getString("password")),username);
    }

    private Long findUserIdByUsername(String username){
        return jdbcTemplate.queryForObject("select id from users where username = ?", Long.class, username);
    }

    private List<Role> getRoles(){
        return jdbcTemplate.query("select id, name from roles",
                (rs, rowNum) -> {
                    return new Role(rs.getLong("id"), rs.getString("name"));
                });
    }

    static class Role {
        long id;
        String name;

        public Role(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class User{
        long id;
        String username;
        String password;

        public User(long id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
    }
}
