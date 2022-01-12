# Generate a valid certificate for localhost
.PHONY: generate-certificate
generate-certificate:
	mkcert localhost
	openssl pkcs12 -inkey localhost-key.pem -in localhost.pem -export -name localhost -passout pass:password -out localhost.p12
	mv localhost.p12 keystore
