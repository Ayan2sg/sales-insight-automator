from datetime import datetime
from pydantic import BaseModel, EmailStr, Field


class SummaryRequest(BaseModel):
    recipient_email: EmailStr = Field(..., description="Email address to send the summary to")
    subject: str = Field(default="Quarterly Sales Insight Summary", max_length=200)
    instructions: str | None = Field(
        default=None,
        description="Optional additional guidance for the AI (e.g. tone, focus regions, etc.)",
        max_length=1000,
    )


class SummaryResponse(BaseModel):
    request_id: str
    recipient_email: EmailStr
    status: str = "queued"
    created_at: datetime


class HealthResponse(BaseModel):
    status: str
    timestamp: datetime

