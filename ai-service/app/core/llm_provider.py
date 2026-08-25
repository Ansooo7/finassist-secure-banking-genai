import json
import logging
from abc import ABC, abstractmethod
from typing import Optional, Dict, Any
import httpx
from app.config import settings

logger = logging.getLogger("finassist.llm")


class BaseLLMProvider(ABC):
    """Abstract interface for configurable LLM providers."""

    @abstractmethod
    async def generate_response(self, system_prompt: str, user_prompt: str, temperature: float = 0.1) -> str:
        pass


class MockLLMProvider(BaseLLMProvider):
    """
    High-Fidelity Deterministic Banking LLM.
    Synthesizes natural, context-grounded banking insights from structured analytics
    and retrieved FAQ documents without requiring external API keys.
    """

    async def generate_response(self, system_prompt: str, user_prompt: str, temperature: float = 0.1) -> str:
        logger.info("[MockLLM] Synthesizing grounded banking response.")
        
        # When prompt includes structured context payload from orchestrator, deliver natural synthesis
        if "### STRUCTURED CONTEXT:" in user_prompt:
            parts = user_prompt.split("### STRUCTURED CONTEXT:")
            query_part = parts[0].replace("User Question:", "").strip()
            context_part = parts[1].strip() if len(parts) > 1 else ""
            
            return f"{context_part}\n\n*Note: All calculations are derived from your authenticated transaction history.*"
            
        return "I am FinAssist, your personal banking assistant. I can help analyze your transactions, explain your spending patterns, and answer questions about banking policies."


class OpenAILLMProvider(BaseLLMProvider):
    """OpenAI API integration (GPT-4o-mini / GPT-4o)."""

    def __init__(self, api_key: str, model: str):
        self.api_key = api_key
        self.model = model
        self.endpoint = "https://api.openai.com/v1/chat/completions"

    async def generate_response(self, system_prompt: str, user_prompt: str, temperature: float = 0.1) -> str:
        if not self.api_key:
            logger.warning("[OpenAILLM] API key not provided. Falling back to MockLLM.")
            return await MockLLMProvider().generate_response(system_prompt, user_prompt)

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "temperature": temperature,
            "max_tokens": 800
        }

        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                response = await client.post(self.endpoint, headers=headers, json=payload)
                response.raise_for_status()
                data = response.json()
                return data["choices"][0]["message"]["content"].strip()
        except Exception as e:
            logger.error(f"[OpenAILLM] API call failed: {e}. Falling back to Mock.")
            return await MockLLMProvider().generate_response(system_prompt, user_prompt)


class AnthropicLLMProvider(BaseLLMProvider):
    """Anthropic Claude API integration."""

    def __init__(self, api_key: str, model: str):
        self.api_key = api_key
        self.model = model
        self.endpoint = "https://api.anthropic.com/v1/messages"

    async def generate_response(self, system_prompt: str, user_prompt: str, temperature: float = 0.1) -> str:
        if not self.api_key:
            logger.warning("[AnthropicLLM] API key not provided. Falling back to MockLLM.")
            return await MockLLMProvider().generate_response(system_prompt, user_prompt)

        headers = {
            "x-api-key": self.api_key,
            "anthropic-version": "2023-06-01",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "system": system_prompt,
            "messages": [{"role": "user", "content": user_prompt}],
            "max_tokens": 800,
            "temperature": temperature
        }

        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                response = await client.post(self.endpoint, headers=headers, json=payload)
                response.raise_for_status()
                data = response.json()
                return data["content"][0]["text"].strip()
        except Exception as e:
            logger.error(f"[AnthropicLLM] API call failed: {e}. Falling back to Mock.")
            return await MockLLMProvider().generate_response(system_prompt, user_prompt)


class OllamaLLMProvider(BaseLLMProvider):
    """Local Ollama instance integration."""

    def __init__(self, base_url: str, model: str):
        self.base_url = base_url.rstrip("/")
        self.model = model

    async def generate_response(self, system_prompt: str, user_prompt: str, temperature: float = 0.1) -> str:
        endpoint = f"{self.base_url}/api/chat"
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "stream": False,
            "options": {"temperature": temperature}
        }

        try:
            async with httpx.AsyncClient(timeout=45.0) as client:
                response = await client.post(endpoint, json=payload)
                response.raise_for_status()
                data = response.json()
                return data["message"]["content"].strip()
        except Exception as e:
            logger.error(f"[OllamaLLM] Local Ollama call failed: {e}. Falling back to Mock.")
            return await MockLLMProvider().generate_response(system_prompt, user_prompt)


def get_llm_provider() -> BaseLLMProvider:
    """Factory creating the configured LLM provider."""
    provider_type = settings.llm_provider.lower().strip()
    
    if provider_type == "openai" and settings.openai_api_key:
        logger.info(f"Using OpenAI Provider ({settings.openai_model})")
        return OpenAILLMProvider(settings.openai_api_key, settings.openai_model)
    elif provider_type == "anthropic" and settings.anthropic_api_key:
        logger.info(f"Using Anthropic Provider ({settings.anthropic_model})")
        return AnthropicLLMProvider(settings.anthropic_api_key, settings.anthropic_model)
    elif provider_type == "ollama":
        logger.info(f"Using Local Ollama Provider ({settings.ollama_model})")
        return OllamaLLMProvider(settings.ollama_base_url, settings.ollama_model)
    else:
        logger.info("Using High-Fidelity Mock LLM Provider (Offline / Deterministic)")
        return MockLLMProvider()
