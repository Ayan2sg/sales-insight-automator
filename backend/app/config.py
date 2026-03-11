import os
from functools import lru_cache
from pydantic import BaseModel, EmailStr


class Settings(BaseModel):
    env: str = os.getenv("APP_ENV", "development")
    api_key: str = os.getenv("API_KEY", "dev-secret-key")

    # LLM settings
    llm_provider: str = os.getenv("LLM_PROVIDER", "gemini")  # or "groq"
    google_api_key: str | None = os.getenv("GOOGLE_API_KEY")
    groq_api_key: str | None = os.getenv("GROQ_API_KEY")

    # Email settings
    mail_from: EmailStr = EmailStr(os.getenv("MAIL_FROM", "no-reply@example.com"))
    smtp_host: str = os.getenv("SMTP_HOST", "smtp.example.com")
    smtp_port: int = int(os.getenv("SMTP_PORT", "587"))
    smtp_username: str | None = os.getenv("SMTP_USERNAME")
    smtp_password: str | None = os.getenv("SMTP_PASSWORD")
    smtp_use_tls: bool = os.getenv("SMTP_USE_TLS", "true").lower() == "true"

    # Upload limits (bytes)
    max_upload_size: int = int(os.getenv("MAX_UPLOAD_SIZE", str(5 * 1024 * 1024)))  # 5MB


@lru_cache
def get_settings() -> Settings:
    return Settings()

