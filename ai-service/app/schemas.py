from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field
from datetime import datetime


class TransactionItem(BaseModel):
    id: str
    amount: float
    currency: str = "GBP"
    direction: str = "DEBIT"  # DEBIT or CREDIT
    category: str
    merchant_name: str
    description: Optional[str] = None
    is_recurring: bool = False
    transaction_time: datetime


class CustomerContext(BaseModel):
    customer_id: str
    customer_name: str
    account_number: str
    current_balance: float
    currency: str = "GBP"


class ChatHistoryItem(BaseModel):
    sender: str  # USER, ASSISTANT
    text: str
    timestamp: Optional[datetime] = None


class AIOrchestrationRequest(BaseModel):
    message: str = Field(..., description="User input prompt in natural language")
    customer_context: CustomerContext
    recent_transactions: List[TransactionItem] = Field(default_factory=list)
    session_id: Optional[str] = None
    conversation_history: List[ChatHistoryItem] = Field(default_factory=list)


class FAQDocumentSource(BaseModel):
    doc_id: str
    category: str
    title: str
    content_snippet: str
    similarity_score: float


class MoMVarianceItem(BaseModel):
    category: str
    previous_amount: float
    current_amount: float
    delta_amount: float
    percentage_change: float


class ExplainabilityAttribution(BaseModel):
    data_points_used: List[str] = Field(default_factory=list)
    retrieved_faq_sources: List[FAQDocumentSource] = Field(default_factory=list)
    intent_detected: str
    intent_confidence: float
    guardrail_checks: Dict[str, str] = Field(default_factory=dict)
    variance_breakdown: Optional[List[MoMVarianceItem]] = None
    is_grounded: bool = True


class AIOrchestrationResponse(BaseModel):
    answer: str
    intent: str  # TRANSACTION_ANALYTICS, BANKING_FAQ, HYBRID, OFF_TOPIC, INJECTION_BLOCKED, ADVICE_REFUSED
    guardrail_status: str  # PASSED, INJECTION_BLOCKED, ADVICE_REFUSED, HALLUCINATION_REFUSED
    explainability: ExplainabilityAttribution
    latency_ms: int


class GuardrailEvaluateRequest(BaseModel):
    prompt: str


class GuardrailEvaluateResponse(BaseModel):
    sanitized_prompt: str
    is_prompt_injection: bool
    injection_reason: Optional[str] = None
    is_financial_advice_request: bool
    advice_reason: Optional[str] = None
    pii_redacted: bool
    overall_status: str  # PASSED, INJECTION_BLOCKED, ADVICE_REFUSED
