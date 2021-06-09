kubectl create secret generic glab-identity-db-secret \
	--from-literal=DB_USER=iduser \
	--from-literal=DB_PWD=2t58chEeTL \
	--from-literal=DB_HOST=44.231.24.58:3306 \
	--from-literal=DB_NAME=identity
