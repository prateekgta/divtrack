#!/bin/bash
set -euo pipefail

PRIVATE_KEY_FILE="jwt_private.pem"
PUBLIC_KEY_FILE="jwt_public.pem"

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$PRIVATE_KEY_FILE" 2>/dev/null
openssl pkey -in "$PRIVATE_KEY_FILE" -pubout -out "$PUBLIC_KEY_FILE" 2>/dev/null

# Format private key for .env (single line with \n)
PRIVATE_SINGLE=$(awk 'BEGIN {ORS="\\n"} {print}' "$PRIVATE_KEY_FILE")
PUBLIC_SINGLE=$(awk 'BEGIN {ORS="\\n"} {print}' "$PUBLIC_KEY_FILE")

echo "=== Add these to your .env file ==="
echo "JWT_PRIVATE_KEY=$PRIVATE_SINGLE"
echo "JWT_PUBLIC_KEY=$PUBLIC_SINGLE"
echo ""
echo "Keys saved to $PRIVATE_KEY_FILE and $PUBLIC_KEY_FILE"
