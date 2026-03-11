from __future__ import annotations

import os
from textwrap import shorten

import requests

from ..config import get_settings


def _build_prompt(structured_insights: str, sample_rows: str, instructions: str | None) -> str:
    extra = instructions or (
        "Write a concise, executive-ready narrative (4–7 paragraphs) focusing on trends, "
        "risks, and opportunities. Avoid raw CSV dumps; synthesize insights."
    )
    return (
        "You are a senior sales strategist asked to summarize quarterly performance.\n\n"
        "STRUCTURED INSIGHTS:\n"
        f"{structured_insights}\n\n"
        "SAMPLE ROWS (truncated):\n"
        f"{sample_rows}\n\n"
        "GUIDANCE:\n"
        f"{extra}\n"
    )


def generate_summary(structured_insights: str, sample_rows: str, instructions: str | None = None) -> str:
    """
    Generate a natural language summary using the configured LLM provider.

    This implementation keeps HTTP usage simple so it can be adapted
    to Gemini, Groq, or any other LLM gateway.
    """
    settings = get_settings()
    prompt = _build_prompt(
        structured_insights=shorten(structured_insights, width=4000, placeholder="..."),
        sample_rows=shorten(sample_rows, width=4000, placeholder="..."),
        instructions=instructions,
    )

    provider = settings.llm_provider.lower()
    if provider == "gemini":
        return _call_gemini(prompt, api_key=settings.google_api_key)
    if provider == "groq":
        return _call_groq(prompt, api_key=settings.groq_api_key)

    # Fallback: echo-style response for local testing without an API key
    return (
        "LLM provider not fully configured. Here is a deterministic placeholder summary.\n\n"
        f"Prompt received (truncated):\n{prompt[:1500]}"
    )


def _call_gemini(prompt: str, api_key: str | None) -> str:
    if not api_key:
        raise RuntimeError("GOOGLE_API_KEY must be set to use Gemini.")

    url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    headers = {"x-goog-api-key": api_key, "Content-Type": "application/json"}
    payload = {"contents": [{"parts": [{"text": prompt}]}]}

    resp = requests.post(url, json=payload, headers=headers, timeout=60)
    resp.raise_for_status()
    data = resp.json()
    try:
        return data["candidates"][0]["content"]["parts"][0]["text"]
    except (KeyError, IndexError) as exc:
        raise RuntimeError(f"Unexpected Gemini response format: {data}") from exc


def _call_groq(prompt: str, api_key: str | None) -> str:
    if not api_key:
        raise RuntimeError("GROQ_API_KEY must be set to use Groq.")

    url = "https://api.groq.com/openai/v1/chat/completions"
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
    payload = {
        "model": os.getenv("GROQ_MODEL", "llama-3.1-8b-instant"),
        "messages": [
            {"role": "system", "content": "You are a helpful assistant for sales analytics."},
            {"role": "user", "content": prompt},
        ],
        "temperature": 0.4,
        "max_tokens": 800,
    }

    resp = requests.post(url, json=payload, headers=headers, timeout=60)
    resp.raise_for_status()
    data = resp.json()
    try:
        return data["choices"][0]["message"]["content"]
    except (KeyError, IndexError) as exc:
        raise RuntimeError(f"Unexpected Groq response format: {data}") from exc

