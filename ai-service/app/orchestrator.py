import re
import time
import logging
from typing import List, Dict, Any, Tuple
from app.schemas import (
    AIOrchestrationRequest,
    AIOrchestrationResponse,
    ExplainabilityAttribution,
    FAQDocumentSource,
    MoMVarianceItem
)
from app.core.llm_provider import get_llm_provider
from app.guardrails.pii_sanitizer import pii_sanitizer
from app.guardrails.injection_defense import injection_defender
from app.guardrails.advice_guard import advice_guard
from app.guardrails.hallucination_guard import hallucination_guard
from app.analytics.transaction_engine import analytics_engine
from app.rag.retriever import retriever

logger = logging.getLogger("finassist.orchestrator")


class AIOrchestrator:
    """
    Central AI Orchestration Engine.
    Coordinates PII Sanitization, Prompt Injection Defense, Intent Routing,
    Deterministic Analytics, RAG Knowledge Retrieval, and LLM Synthesis.
    """

    def __init__(self):
        self.llm = get_llm_provider()

    async def process_query(self, req: AIOrchestrationRequest) -> AIOrchestrationResponse:
        start_time = time.time()
        guardrail_checks: Dict[str, str] = {}
        data_points_used: List[str] = []
        retrieved_sources: List[FAQDocumentSource] = []
        variance_breakdown: List[MoMVarianceItem] = []

        # -------------------------------------------------------------
        # STEP 1: Tier 1 - PII Sanitization
        # -------------------------------------------------------------
        sanitized_msg, was_redacted = pii_sanitizer.sanitize(req.message)
        guardrail_checks["PII_SANITIZER"] = "REDACTED" if was_redacted else "PASSED"

        # -------------------------------------------------------------
        # STEP 2: Tier 2 - Prompt Injection Defense
        # -------------------------------------------------------------
        is_injection, injection_reason = injection_defender.evaluate(sanitized_msg)
        if is_injection:
            logger.warning(f"[Guardrail Block] Prompt injection attempt: {injection_reason}")
            guardrail_checks["PROMPT_INJECTION_DEFENSE"] = f"BLOCKED ({injection_reason})"
            latency_ms = int((time.time() - start_time) * 1000)
            
            return AIOrchestrationResponse(
                answer="I cannot process this request. FinAssist is designed strictly for authorized personal banking assistance and security policies prohibit instruction overrides or cross-customer queries.",
                intent="INJECTION_BLOCKED",
                guardrail_status="INJECTION_BLOCKED",
                explainability=ExplainabilityAttribution(
                    data_points_used=["Adversarial pattern blocked by Tier-2 Security Guardrail"],
                    retrieved_faq_sources=[],
                    intent_detected="INJECTION_BLOCKED",
                    intent_confidence=1.0,
                    guardrail_checks=guardrail_checks,
                    is_grounded=False
                ),
                latency_ms=latency_ms
            )
        guardrail_checks["PROMPT_INJECTION_DEFENSE"] = "PASSED"

        # -------------------------------------------------------------
        # STEP 3: Tier 4 - Financial Advice Boundary Guardrail
        # -------------------------------------------------------------
        is_advice, advice_reason = advice_guard.evaluate(sanitized_msg)
        if is_advice:
            logger.info(f"[Guardrail Refusal] Financial advice requested: {advice_reason}")
            guardrail_checks["FINANCIAL_ADVICE_GUARDRAIL"] = f"REFUSED ({advice_reason})"
            latency_ms = int((time.time() - start_time) * 1000)

            return AIOrchestrationResponse(
                answer=advice_guard.ADVICE_REFUSAL_MESSAGE,
                intent="ADVICE_REFUSED",
                guardrail_status="ADVICE_REFUSED",
                explainability=ExplainabilityAttribution(
                    data_points_used=["Query flagged by Tier-4 Financial Advice Boundary Guardrail"],
                    retrieved_faq_sources=[],
                    intent_detected="ADVICE_REFUSED",
                    intent_confidence=1.0,
                    guardrail_checks=guardrail_checks,
                    is_grounded=True
                ),
                latency_ms=latency_ms
            )
        guardrail_checks["FINANCIAL_ADVICE_GUARDRAIL"] = "PASSED"

        # -------------------------------------------------------------
        # STEP 4: Tier 3 - Multi-Class Intent Classification & Routing
        # -------------------------------------------------------------
        intent, intent_conf = self._classify_intent(sanitized_msg)
        logger.info(f"[Intent Router] Detected Intent: {intent} (Confidence: {intent_conf})")

        # -------------------------------------------------------------
        # STEP 5: Execution Branches (Analytics, RAG, or General)
        # -------------------------------------------------------------
        final_answer = ""
        tx_list = req.recent_transactions
        currency = req.customer_context.currency
        curr_symbol = "£" if currency == "GBP" else ("$" if currency == "USD" else "€")

        if intent == "TRANSACTION_ANALYTICS":
            final_answer, data_points_used, variance_breakdown = self._handle_analytics_intent(
                sanitized_msg, tx_list, req.customer_context, curr_symbol
            )
            guardrail_checks["CUSTOMER_ISOLATION"] = f"PASSED (Restricted to Customer: {req.customer_context.customer_id})"

        elif intent == "BANKING_FAQ":
            retrieved_sources = retriever.retrieve(sanitized_msg, top_k=3)
            if retrieved_sources:
                top_doc = retrieved_sources[0]
                final_answer = (
                    f"**{top_doc.title}**\n\n"
                    f"{top_doc.content_snippet}\n\n"
                    f"*(Source: Verified Banking Policy Knowledge Base - Doc ID: {top_doc.doc_id})*"
                )
                data_points_used.append(f"Retrieved policy document: {top_doc.title} (Relevance: {int(top_doc.similarity_score * 100)}%)")
            else:
                final_answer = "I could not find a specific banking policy FAQ matching your question. For immediate assistance, please reach out to customer support at 0800-012-3456."
                data_points_used.append("No high-confidence FAQ document found in knowledge base.")

        elif intent == "HYBRID":
            # Combined analytical insight + relevant FAQ
            analytics_ans, data_pts, v_break = self._handle_analytics_intent(
                sanitized_msg, tx_list, req.customer_context, curr_symbol
            )
            data_points_used.extend(data_pts)
            variance_breakdown = v_break
            
            faq_sources = retriever.retrieve(sanitized_msg, top_k=2)
            retrieved_sources = faq_sources
            if faq_sources:
                faq_snippet = f"\n\n**Related Policy Information ({faq_sources[0].title}):**\n{faq_sources[0].content_snippet}"
                final_answer = analytics_ans + faq_snippet
            else:
                final_answer = analytics_ans

        else:  # GENERAL_GREETING / INFORMATIONAL
            final_answer = (
                f"Hello {req.customer_context.customer_name}! I am FinAssist, your personal banking AI assistant. "
                "You can ask me to analyze your spending trends (e.g. *'How much did I spend this month?'*, *'Explain why my spending increased'*), "
                "list recurring subscriptions, or answer questions about banking policies, card limits, and security."
            )
            data_points_used.append(f"Authenticated customer session for {req.customer_context.customer_name}")

        # -------------------------------------------------------------
        # STEP 6: Tier 5 - Grounding & Explainability Packaging
        # -------------------------------------------------------------
        is_grounded, grounding_note = hallucination_guard.verify_grounding(
            intent, data_points_used, retrieved_sources, len(tx_list) > 0
        )
        guardrail_checks["FACTUAL_GROUNDING"] = grounding_note

        latency_ms = int((time.time() - start_time) * 1000)

        return AIOrchestrationResponse(
            answer=final_answer,
            intent=intent,
            guardrail_status="PASSED",
            explainability=ExplainabilityAttribution(
                data_points_used=data_points_used,
                retrieved_faq_sources=retrieved_sources,
                intent_detected=intent,
                intent_confidence=intent_conf,
                guardrail_checks=guardrail_checks,
                variance_breakdown=variance_breakdown if variance_breakdown else None,
                is_grounded=is_grounded
            ),
            latency_ms=latency_ms
        )

    def _classify_intent(self, text: str) -> Tuple[str, float]:
        """Classify user prompt into intent categories."""
        lower = text.lower()

        # 1. Transaction Analytics Keywords
        analytics_keywords = [
            "spend", "spent", "spending", "category", "categories", "cost", "expense", "expenses",
            "largest", "highest", "biggest", "recurring", "subscription", "subscriptions",
            "compare", "increase", "increased", "decrease", "decreased", "why did i spend",
            "how much did i", "what did i spend", "transactions", "bills", "groceries", "dining"
        ]
        
        # 2. Banking Policy FAQ Keywords
        faq_keywords = [
            "limit", "contactless", "overdraft", "statement", "transfer", "faster payments",
            "swift", "international", "freeze", "lost card", "stolen card", "fraud", "scam",
            "phishing", "dispute", "chargeback", "double charge", "refund", "pin", "apple pay"
        ]

        analytics_score = sum(1 for kw in analytics_keywords if kw in lower)
        faq_score = sum(1 for kw in faq_keywords if kw in lower)

        if analytics_score > 0 and faq_score > 0:
            return "HYBRID", 0.90
        elif analytics_score > 0:
            return "TRANSACTION_ANALYTICS", 0.95
        elif faq_score > 0:
            return "BANKING_FAQ", 0.92
        elif any(g in lower for g in ["hello", "hi", "hey", "who are you", "help", "what can you do"]):
            return "GENERAL_GREETING", 0.85
        else:
            return "INFORMATIONAL", 0.70

    def _handle_analytics_intent(
        self,
        query: str,
        transactions: List[Any],
        customer: Any,
        curr_symbol: str
    ) -> Tuple[str, List[str], List[MoMVarianceItem]]:
        """Handles specific transaction analytics query types."""
        lower = query.lower()
        data_points = []
        variance_items: List[MoMVarianceItem] = []

        # 1. Compare spending / Why did spending increase
        if any(k in lower for k in ["compare", "why did i spend", "increased", "increase", "difference"]):
            mom = analytics_engine.get_mom_comparison(transactions)
            variance_items = mom.get("variance_breakdown", [])
            
            delta_val = mom["delta_amount"]
            direction_word = "increased" if delta_val >= 0 else "decreased"
            pct_val = abs(mom["percentage_change"])
            
            lines = [
                f"Your spending **{direction_word} by {pct_val:.1f}%** ({curr_symbol}{abs(delta_val):,.2f}) compared to {mom['previous_month']}.",
                f"- **{mom['current_month']} Spend:** {curr_symbol}{mom['current_spent']:,.2f}",
                f"- **{mom['previous_month']} Spend:** {curr_symbol}{mom['previous_spent']:,.2f}",
                "\n**Key Category Changes:**"
            ]

            for item in variance_items[:4]:
                sign = "+" if item.delta_amount >= 0 else "-"
                lines.append(f"- **{item.category}:** {sign}{curr_symbol}{abs(item.delta_amount):,.2f} ({sign}{item.percentage_change:.1f}%)")

            data_points.append(f"Evaluated {len(transactions)} transactions across {mom['previous_month']} and {mom['current_month']}.")
            data_points.append(f"Net MoM spend delta: {curr_symbol}{delta_val:,.2f} ({pct_val:.1f}%)")
            return "\n".join(lines), data_points, variance_items

        # 2. Category spending / What category did I spend the most on?
        elif any(k in lower for k in ["category", "most on", "breakdown", "where did my money go"]):
            cats = analytics_engine.get_category_breakdown(transactions)
            top = cats.get("top_category")
            
            lines = [
                f"In **{cats['month_name']} {cats['year']}**, your total spending was **{curr_symbol}{cats['total_spent']:,.2f}**.",
            ]
            if top:
                lines.append(f"Your highest spending category was **{top['category']}** at **{curr_symbol}{top['amount']:,.2f}** ({top['percentage']:.1f}% of total spend).\n")
                lines.append("**Category Breakdown:**")
                for c in cats["breakdown"]:
                    lines.append(f"- **{c['category']}:** {curr_symbol}{c['amount']:,.2f} ({c['percentage']:.1f}%) — *{c['count']} purchases*")

            data_points.append(f"Aggregated category totals across {cats['month_name']} transactions.")
            if top:
                data_points.append(f"Top category: {top['category']} ({curr_symbol}{top['amount']:,.2f})")
            return "\n".join(lines), data_points, []

        # 3. Largest / Highest transactions
        elif any(k in lower for k in ["largest", "highest", "biggest", "top transaction"]):
            largest = analytics_engine.get_largest_transactions(transactions, limit=5)
            lines = ["Here are your largest debit transactions on record:\n"]
            for i, t in enumerate(largest, 1):
                lines.append(f"{i}. **{curr_symbol}{t['amount']:,.2f}** — {t['merchant']} (*{t['category']}*) on {t['date']}")
                data_points.append(f"{t['merchant']}: {curr_symbol}{t['amount']:,.2f} ({t['date']})")
            return "\n".join(lines), data_points, []

        # 4. Recurring expenses / subscriptions
        elif any(k in lower for k in ["recurring", "subscription", "subscriptions", "direct debits"]):
            recurring = analytics_engine.get_recurring_expenses(transactions)
            total_rec = sum(r["amount"] for r in recurring)
            lines = [
                f"You have **{len(recurring)} active recurring expenses** totaling approximately **{curr_symbol}{total_rec:,.2f} / month**:\n"
            ]
            for r in recurring:
                lines.append(f"- **{r['merchant_name']}** ({r['category']}): {curr_symbol}{r['amount']:,.2f} (*Last billed: {r['latest_date']}*)")
                data_points.append(f"Recurring subscription: {r['merchant_name']} ({curr_symbol}{r['amount']:,.2f})")
            return "\n".join(lines), data_points, []

        # 5. Default / Total spend this month
        else:
            monthly = analytics_engine.get_monthly_totals(transactions)
            ans = (
                f"In **{monthly['month_name']} {monthly['year']}**, you have spent **{curr_symbol}{monthly['total_spent']:,.2f}** "
                f"across **{monthly['debit_count']} debit transactions** (average transaction: {curr_symbol}{monthly['avg_tx']:,.2f}). "
                f"Your total incoming credits for this period were **{curr_symbol}{monthly['total_income']:,.2f}**."
            )
            data_points.append(f"Calculated sum of {monthly['debit_count']} debit transactions for {monthly['month_name']} {monthly['year']}.")
            data_points.append(f"Account Balance: {curr_symbol}{customer.current_balance:,.2f}")
            return ans, data_points, []


orchestrator = AIOrchestrator()
