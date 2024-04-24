 #!/bin/sh

echo ""
echo "Loading azd .env file from current environment"
echo ""

while IFS='=' read -r key value; do
    value=$(echo "$value" | sed 's/^"//' | sed 's/"$//')
    export "$key=$value"
done <<EOF
$(azd env get-values)
EOF


echo 'Building java indexer'
mvn package -f ../../app/indexer/pom.xml

echo 'Running the java indexer cli.jar'
java -jar ../../app/indexer/cli/target/cli.jar '../../data' --verbose --storageaccount "$AZURE_STORAGE_ACCOUNT" --container "$AZURE_STORAGE_CONTAINER" --searchservice "$AZURE_SEARCH_SERVICE"  --openai-service-name "$AZURE_OPENAI_SERVICE"  --openai-emb-deployment "$AZURE_OPENAI_EMB_DEPLOYMENT"  --index "$AZURE_SEARCH_INDEX" --formrecognizerservice "$AZURE_FORMRECOGNIZER_SERVICE"  add
