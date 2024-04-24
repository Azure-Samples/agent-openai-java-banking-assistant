#!/bin/sh

echo ""
echo "Loading azd .env file from current environment"
echo ""

while IFS='=' read -r key value; do
    value=$(echo "$value" | sed 's/^"//' | sed 's/"$//')
    export "$key=$value"
    echo "export $key=$value"
done <<EOF
$(azd env get-values)
EOF

if [ $? -ne 0 ]; then
    echo "Failed to load environment variables from azd environment"
    exit $?
fi

echo ""
echo "Restoring frontend npm packages"
echo ""

cd ./frontend
npm install
if [ $? -ne 0 ]; then
    echo "Failed to restore frontend npm packages"
    exit $?
fi

echo ""
echo "Building frontend"
echo ""

npm run build
if [ $? -ne 0 ]; then
    echo "Failed to build frontend"
    exit $?
fi

echo ""
echo "Starting spring boot api backend and react spa from backend/public static content"
echo "Spring-boot application.properties use values from azd .env file which have been exported as environment variables"
echo ""

cd ../backend
#xdg-open http://localhost:8080
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
if [ $? -ne 0 ]; then
    echo "Failed to start backend"
    exit $?
fi
