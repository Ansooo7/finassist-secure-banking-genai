import re
from typing import Tuple, Optional


class FinancialAdviceBoundaryGuardrail:
    """
    Tier 4 AI Guardrail: Financial Advice Boundary Enforcer.
    Identifies and strictly refuses speculative investment, cryptocurrency,
    stock picking, or regulated financial advice requests in compliance with FCA guidelines.
    """

    ADVICE_PATTERNS = [
        (re.compile(r"(should\s+i\s+(buy|sell|invest|trade|put|allocate).*?(bitcoin|btc|crypto|eth|stocks?|shares?|nfts?|forex|etfs?))", re.IGNORECASE),
         "Cryptocurrency / Stock Speculation Advice"),
        (re.compile(r"(which|what)\s+(stocks?|crypto|coins?|shares?|etfs?)\s+(should\s+i\s+(buy|invest(\s+in)?)|will\s+give\s+maximum\s+returns|to\s+invest\s+in)", re.IGNORECASE),
         "Stock / Asset Recommendation Request"),
        (re.compile(r"(give\s+me|provide)\s+(financial\s+advice|investment\s+advice|trading\s+tips|stock\s+tips)", re.IGNORECASE),
         "Direct Financial Advice Solicitation"),
        (re.compile(r"(how\s+to\s+get\s+rich|guaranteed\s+returns?|double\s+my\s+money)", re.IGNORECASE),
         "High-Risk Get-Rich Speculation")
    ]

    ADVICE_REFUSAL_MESSAGE = (
        "I cannot provide investment or financial advice. "
        "FinAssist is an informational banking assistant and is not authorized to give regulated financial, "
        "stock, or cryptocurrency recommendations. "
        "For personalized investment guidance, please speak with an independent, qualified financial adviser."
    )

    @classmethod
    def evaluate(cls, prompt: str) -> Tuple[bool, Optional[str]]:
        """
        Check if the prompt requests prohibited financial/investment advice.
        Returns: (is_advice_solicited, reason)
        """
        if not prompt or not prompt.strip():
            return False, None

        normalized = prompt.strip()

        for pattern, reason in cls.ADVICE_PATTERNS:
            if pattern.search(normalized):
                return True, reason

        return False, None


advice_guard = FinancialAdviceBoundaryGuardrail()
