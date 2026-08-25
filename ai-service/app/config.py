import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "FinAssist AI & RAG Microservice"
    app_version: str = "1.0.0"
    port: int = 8000
    host: str = "0.0.0.0"
    
    # LLM Provider: "mock", "openai", "anthropic", "ollama"
    llm_provider: str = os.getenv("LLM_PROVIDER", "mock")
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    openai_model: str = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    anthropic_api_key: str = os.getenv("ANTHROPIC_API_KEY", "")
    anthropic_model: str = os.getenv("ANTHROPIC_MODEL", "claude-3-haiku-20240307")
    ollama_base_url: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    ollama_model: str = os.getenv("OLLAMA_MODEL", "llama3.2")
    
    # RAG Settings
    rag_top_k: int = 3
    similarity_threshold: float = 0.25
    embedding_dimension: int = 384
    
    # Guardrail Thresholds
    injection_confidence_threshold: float = 0.70
    advice_confidence_threshold: float = 0.65

    class Config:
        env_file = ".env"
        extra = "allow"


settings = Settings()
