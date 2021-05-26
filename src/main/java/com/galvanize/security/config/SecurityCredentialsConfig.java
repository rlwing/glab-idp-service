package com.galvanize.security.config;

import com.galvanize.security.filter.JwtTokenAuthenticationFilter;
import com.galvanize.security.filter.JwtUsernameAndPasswordAuthenticationFilter;
import com.galvanize.security.user.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity 	// Enable security config. This annotation denotes config for spring security.
public class SecurityCredentialsConfig extends WebSecurityConfigurerAdapter {

//    @Qualifier("userDetailsServiceImpl")
    private UserDetailsServiceImpl userDetailsService;
    private JwtProperties jwtProperties;

    public SecurityCredentialsConfig(UserDetailsServiceImpl userDetailsService, JwtProperties jwtProperties) {
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                // make sure we use stateless session; session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // handle an authorized attempts
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                .addFilterBefore(new JwtTokenAuthenticationFilter(jwtProperties), UsernamePasswordAuthenticationFilter.class)
                // Add a filter to validate user credentials and add token in the response header
                // What's the authenticationManager()?
                // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
                // The filter needs this auth manager to authenticate the user.
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), userDetailsService, jwtProperties))
                .authorizeRequests()
                // HEALTH
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                // LOGIN
                .antMatchers(HttpMethod.POST, jwtProperties.getUri()).permitAll()
                // REGISTER
                .antMatchers(HttpMethod.POST, "/register/**").permitAll()
                // CHANGE PASSWORD
                .antMatchers(HttpMethod.PUT, "/password/**").hasRole("USER")
                // ADMIN ACTUATOR ENDPOINTS (ALL ARE EXPOSED)
                .antMatchers(HttpMethod.GET,"/actuator/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, jwtProperties.getUri()).hasRole("ADMIN")
                // any other requests must be authenticated
                .anyRequest().authenticated();
    }

    // Spring has UserDetailsService interface, which can be overriden to provide our implementation for fetching user from database (or any other source).
    // The UserDetailsService object is used by the auth manager to load the user from database.
    // In addition, we need to define the password encoder also. So, auth manager can compare and verify passwords.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtProperties getJwtProperties(){
        return new JwtProperties();
    }

}
