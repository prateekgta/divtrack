# Cloud Run deployment — backend
# Deploy: gcloud run deploy divtrack-backend \
#   --source . \
#   --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://<supabase-host>:5432/postgres?currentSchema=public" \
#   --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" \
#   --set-env-vars "SPRING_DATASOURCE_PASSWORD=<supabase-password>" \
#   --set-env-vars "JWT_PRIVATE_KEY=<your-rsa-private-key>" \
#   --set-env-vars "JWT_PUBLIC_KEY=<your-rsa-public-key>" \
#   --memory 512Mi \
#   --cpu 1 \
#   --min-instances 0 \
#   --max-instances 2 \
#   --region us-central1 \
#   --allow-unauthenticated

# Cloud Run deployment — frontend (Vercel alternative)
# 1. Connect frontend/ repo to Vercel
# 2. Set env: NEXT_PUBLIC_API_URL=https://divtrack-backend-xxxxx-uc.a.run.app
# 3. Deploy
