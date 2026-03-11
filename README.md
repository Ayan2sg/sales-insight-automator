## Sales Insight Automator – Engineer’s Log

### Overview

This project is a **quick-response tool** for the sales team:

- **Frontend**: React SPA where team members upload `.csv` / `.xlsx` and provide a recipient email (plus optional instructions).
- **Backend**: Java **Spring Boot** API that:
  - Accepts the file upload.
  - Parses quarterly sales data.
  - Calls an **LLM** (Gemini or Groq) to generate an executive-ready narrative.
  - Sends the summary via **email** to the requested address.
- **Docs**: Live **Swagger/OpenAPI** UI for the backend.
- **DevOps**: Dockerfiles for both services, `docker-compose` for local stack, and GitHub Actions CI on PRs.

High-level flow: **Upload → AI Summary → Email Received**.

---

### Running the stack with docker-compose

1. **Clone the repo** and `cd` into it:

```bash
git clone <your_repo_url>.git
cd <repo-folder>
```

2. **Create a `.env` file** at the project root based on `.env.example`:

```bash
cp .env.example .env
```

Fill in:

- `API_KEY` – shared API key used by frontend → backend.
- `LLM_PROVIDER` and the corresponding `GOOGLE_API_KEY` or `GROQ_API_KEY`.
- SMTP credentials (e.g. SendGrid, Mailgun).

3. **Start the full stack**:

```bash
docker-compose up --build
```

4. **Access the app**:

- Frontend SPA: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health endpoint: `http://localhost:8080/actuator/health`

5. **Test the flow**:

- In the frontend:
  - Upload a sales file (`.csv` is supported; `.xlsx` is treated as CSV-like for the prototype).
  - Enter a recipient email.
  - Optionally add instructions (e.g. _“Focus on North region and highlight risks.”_).
  - Click **Send Summary**.
- The backend:
  - Parses the file, computes aggregates (totals, by region/category).
  - Sends a prompt to the configured LLM (Gemini or Groq).
  - Emails the narrative summary to the recipient.

---

### Backend details (Spring Boot)

- Located in `java-backend`.
- Key features:
  - `POST /api/v1/summaries` (multipart):
    - `file`: sales file (`.csv` preferred).
    - `metadata`: JSON with `recipientEmail`, optional `subject`, optional `instructions`.
  - Sales parsing:
    - Uses Apache Commons CSV.
    - Computes **total units**, **total revenue**, **units by region**, **revenue by category**.
  - LLM integration:
    - Configurable via `LLM_PROVIDER` env (`gemini` or `groq`).
    - Gemini: `GOOGLE_API_KEY` + `gemini-1.5-flash` REST API.
    - Groq: `GROQ_API_KEY` + `llama-3.x` chat completions API.
  - Email delivery:
    - Spring `JavaMailSender` with SMTP configuration.
  - Swagger/OpenAPI:
    - Enabled via `springdoc-openapi`.
    - UI: `http://localhost:8080/swagger-ui.html`.

To run just the backend locally with Java/Maven:

```bash
cd java-backend
mvn spring-boot:run
```

---

### Frontend details (React SPA)

- Located in `frontend` (Vite + React + TypeScript).
- Single-page UX:
  - File upload field for `.csv` / `.xlsx`.
  - Recipient email input.
  - Optional instructions textarea.
  - Submit button with **loading**, **success**, and **error** states.
- Environment variables (for local dev):

Create `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080
VITE_API_KEY=super-secret-api-key
```

- Run locally:

```bash
cd frontend
npm install
npm run dev
```

Then open `http://localhost:5173`.

The SPA calls:

- `POST {VITE_API_URL}/api/v1/summaries`
- Sends `X-API-KEY: VITE_API_KEY` header.
- Uses `multipart/form-data` with `file` and `metadata` (JSON blob).

---

### Securing the endpoints

The prototype includes several layers of protection:

- **API key header**:
  - All “business” endpoints (including `/api/v1/summaries`) require a header:
    - `X-API-KEY: <API_KEY>`
  - Implemented as a Spring `OncePerRequestFilter`:
    - Rejects requests with missing/invalid API key (`401 Unauthorized`).
    - Allows unauthenticated access only to:
      - `/v3/api-docs*` and `/swagger-ui*` (Swagger docs).
      - `/actuator/health` (health checks).

- **Upload safety**:
  - `spring.servlet.multipart.max-file-size` and `max-request-size` limited to **5MB**.
  - Sales data is parsed with a strict header-based CSV parser.
  - For brevity, `.xlsx` is handled as CSV-like input, but real deployments can plug in Apache POI readers.

- **LLM safety / resource usage**:
  - Prompts are structured and focused (aggregated stats + sample rows).
  - Token limits (`max_tokens`) are kept modest to avoid runaway usage.
  - LLM provider and keys are read from env, not hard-coded.

- **Email safety**:
  - SMTP credentials are environment-based.
  - No arbitrary email template injection beyond the generated summary.

In a production hardening pass, you could also add:

- Rate limiting (by API key or IP).
- MIME type checks for uploaded files.
- Authentication/authorization layer (e.g. OAuth/OpenID) in front of the API.

---

### CI/CD – GitHub Actions

A GitHub Actions workflow lives at `.github/workflows/ci.yml`:

- Triggers on **pull requests to `main`**.
- **Backend job**:
  - Checks out code.
  - Sets up Java 21.
  - Runs `mvn verify` in `java-backend` (build + tests).
- **Frontend job**:
  - Checks out code.
  - Sets up Node 20.
  - Runs `npm ci` and `npm run build` in `frontend`.

This ensures:

- API still compiles and tests pass when you open PRs.
- Frontend builds successfully with the current configuration.

You can extend this workflow with:

- Linting (ESLint, Checkstyle/Spotless).
- Docker image builds and push to a registry.
- Auto-deploy steps to your chosen cloud.

---

### Deployment suggestions

You can deploy this prototype using the following pattern:

- **Backend (Spring Boot)**:
  - Use a platform that supports Docker (e.g. Render, Fly.io, AWS ECS/Fargate).
  - Build and push an image from `java-backend/Dockerfile`.
  - Expose port `8080`.
  - Configure env vars and secrets based on `.env.example`.

- **Frontend (React)**:
  - Deploy static build to **Vercel**, **Netlify**, or similar.
  - Build locally (`npm run build`) or via platform.
  - Set `VITE_API_URL` to the public backend URL.
  - Set `VITE_API_KEY` to the same API key used by the backend.

Once deployed, your **submission artifacts** will be:

- **GitHub repo URL** (this project).
- **Frontend URL** (e.g. Vercel).
- **Swagger URL** (e.g. `https://your-backend-domain/swagger-ui.html`).

---

### Summary of environment variables

See `.env.example` for a complete list:

- **Core security / API**:
  - `API_KEY`
- **LLM**:
  - `LLM_PROVIDER` (`gemini` or `groq`)
  - `GOOGLE_API_KEY`
  - `GROQ_API_KEY`
  - `GROQ_MODEL`
- **Email / SMTP**:
  - `SMTP_HOST`
  - `SMTP_PORT`
  - `SMTP_USERNAME`
  - `SMTP_PASSWORD`

These should be set as secrets in your deployment platform.

