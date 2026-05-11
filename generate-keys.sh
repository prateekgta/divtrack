# Generate RSA keys for JWT
# Run this script to create keys, then export them as env vars

openssl genrsa -out private.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem
openssl rsa -pubout -in private.pem -out public.pem

echo ""
echo "=== EXPORT THESE ENV VARS ==="
echo "export JWT_PRIVATE_KEY='$(cat private_pkcs8.pem)'"
echo "export JWT_PUBLIC_KEY='$(cat public.pem)'"
echo ""
echo "==== OR use single-line format (paste into .env): ===="
echo "JWT_PRIVATE_KEY=$(cat private_pkcs8.pem | tr '\n' '\\n')"
echo "JWT_PUBLIC_KEY=$(cat public.pem | tr '\n' '\\n')"

# Cleanup
rm private.pem private_pkcs8.pem public.pem
