from email.message import EmailMessage
import smtplib
from typing import Iterable

from ..config import get_settings


def send_summary_email(recipient: str, subject: str, body: str) -> None:
    """
    Send the generated summary via SMTP.

    For production, point SMTP_* env vars at a reliable provider (e.g. SendGrid, Mailgun).
    """
    settings = get_settings()

    msg = EmailMessage()
    msg["From"] = str(settings.mail_from)
    msg["To"] = recipient
    msg["Subject"] = subject
    msg.set_content(body)

    with smtplib.SMTP(settings.smtp_host, settings.smtp_port, timeout=30) as server:
        if settings.smtp_use_tls:
            server.starttls()
        if settings.smtp_username and settings.smtp_password:
            server.login(settings.smtp_username, settings.smtp_password)
        server.send_message(msg)


def send_bulk(recipients: Iterable[str], subject: str, body: str) -> None:
    for r in recipients:
        send_summary_email(r, subject, body)

