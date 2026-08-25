import logging
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.schemas import (
    AIOrchestrationRequest,
    AIOrchestrationResponse,
    GuardrailEvaluateRequest,
    GuardrailEvaluateResponse
)
from app.orchestrator import orchestrator
from app.guardrails.pii_sanitizer import pii_sanitizer
from app.guardrails.injection_defense import injection_defender
from app.guardrails.advice_guard import advice_guard
from app.rag.retriever import retriever

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [%(name)s] %(message)s"
)
logger = logging.getLogger("finassist.main")

app = FastAPI(
    title="FinAssist — AI & RAG Microservice",
    description="Intelligent Conversational Banking Assistant with RAG, Guardrails, and Explainability",
    version=settings.app_version,
    docs_url="/docs",
    openapi_url="/api-docs"
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint exposing microservice and vector store metadata."""
    return {
        "status": "UP",
        "service": settings.app_name,
        "version": settings.app_version,
        "llm_provider": settings.llm_provider,
        "indexed_faq_documents": len(retriever.documents)
    }


@app.post(
    "/api/v1/ai/orchestrate",
    response_model=AIOrchestrationResponse,
    status_code=status.HTTP_200_OK,
    tags=["AI Orchestrator"]
)
async def orchestrate_query(request: AIOrchestrationRequest):
    """
    Main conversational AI entrypoint.
    Executes PII sanitization, Prompt Injection Defense, Financial Advice Boundary enforcement,
    deterministic transaction analytics, RAG vector retrieval, and grounded response synthesis.
    """
    try:
        response = await orchestrator.process_query(request)
        return response
    except Exception as e:
        logger.error(f"Error orchestrating query: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to process AI query: {str(e)}"
        )


@app.post(
    "/api/v1/ai/guardrails/evaluate",
    response_model=GuardrailEvaluateResponse,
    status_code=status.HTTP_200_OK,
    tags=["Safety Guardrails"]
)
async def evaluate_guardrails(request: GuardrailEvaluateRequest):
    """
    Standalone guardrail evaluation endpoint used for security auditing
    and interactive testing in the Safety Test Bench.
    """
    sanitized, pii_redacted = pii_sanitizer.sanitize(request.prompt)
    is_injection, injection_reason = injection_defender.evaluate(sanitized)
    is_advice, advice_reason = advice_guard.evaluate(sanitized)

    overall_status = "PASSED"
    if is_injection:
        overall_status = "INJECTION_BLOCKED"
    elif is_advice:
        overall_status = "ADVICE_REFUSED"

    return GuardrailEvaluateResponse(
        sanitized_prompt=sanitized,
        is_prompt_injection=is_injection,
        injection_reason=injection_reason,
        is_financial_advice_request=is_advice,
        advice_reason=advice_reason,
        pii_redacted=pii_redacted,
        overall_status=overall_status
    )
