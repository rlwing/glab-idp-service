# [gLab Identity Service](https://gitlab.com/galvanize-labs/glab-idp-service)

Simple identity provider with Username/Password and JWT authentication.

To install the database, use $PROJECT_ROOT/db_setup.sql.  This will create a 
database with the standard users and roles tables as well as an associative table. 
Script will seed two users, `admin@glab.com` & `user@glab.com`.  Both passwords are 
'password' and should be changed before deployment.

Default login endpoint is `/auth/` which requires either a body with a `username`,
and `password` as a json object, OR a header element `Authorization` with a valid JWT 
token.

Health endpoint, `/actuator/health` is accessible to the public, while any other
actuator endpoint requires the `ADMIN` role.  All actuator endpoints can be configured 
via the application properties, or the config map in K8s.



