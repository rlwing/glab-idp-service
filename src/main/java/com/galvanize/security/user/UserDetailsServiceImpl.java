package com.galvanize.security.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service   // It has to be annotated with @Service.
public class UserDetailsServiceImpl implements UserDetailsService {

    private JdbcTemplate jdbcTemplate;

    public UserDetailsServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return findUserByUserName(username);

    }

    public UserPrinciple findUserByUserName(String username) throws UsernameNotFoundException {
        UserPrinciple userPrinciple = jdbcTemplate.queryForObject(
                "select id, first_name || ' ' || last_name as name, email, password from users where email = ?",
                (rs, rowNum) -> new UserPrinciple(rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")),
                username);

        if(userPrinciple != null){
            List<String> roles = jdbcTemplate.query("select r.name from users u, user_roles ur, roles r where u.id = ur.user_id and ur.role_id = r.id and u.email = ? ",
                    (rs, rowNum) -> rs.getString("name")
                    , username);
            List<GrantedAuthority> authorities = new ArrayList<>();
            for(Object role : roles) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
            userPrinciple.setAuthorities(authorities);
            return userPrinciple;
        }else{
            // If user not found. Throw this exception.
            throw new UsernameNotFoundException("Username: " + username + " not found");
        }
    }

}