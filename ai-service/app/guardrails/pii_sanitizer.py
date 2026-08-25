import re
from typing import Tuple


class PIISanitizer:
    """
    Tier 1 AI Guardrail: PII Detection and Redaction.
    Masks payment card PANs, sort codes, account numbers, and contact details.
    """

    # Regex patterns
    CARD_PAN_PATTERN = re.compile(r"\b(?:\d[ -]*?){13,19}\b")
    SORT_CODE_PATTERN = re.compile(r"\b\d{2}[- ]\d{2}[- ]\d{2}\b")
    EMAIL_PATTERN = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,7}\b")
    PHONE_PATTERN = re.compile(r"\b(?:\+44|0)7\d{9}\b")

    @classmethod
    def sanitize(cls, text: str) -> Tuple[str, bool]:
        """
        Redacts sensitive financial PII from text.
        Returns: (sanitized_text, was_redacted_flag)
        """
        if not text:
            return text, False

        sanitized = text
        redacted = False

        # 1. Mask 16-digit Card PANs (keep last 4)
        def mask_card(match):
            nonlocal redacted
            raw = re.sub(r"[- ]", "", match.group(0))
            if 13 <= len(raw) <= 19:
                redacted = True
                return f"[CARD_REDACTED_****{raw[-4:]}]"
            return match.group(0)

        sanitized = cls.CARD_PAN_PATTERN.sub(mask_card, sanitized)

        # 2. Mask Sort Codes
        if cls.SORT_CODE_PATTERN.search(sanitized):
            sanitized = cls.SORT_CODE_PATTERN.sub("[SORT_CODE_REDACTED]", sanitized)
            redacted = True

        # 3. Mask Phone Numbers
        if cls.PHONE_PATTERN.search(sanitized):
            sanitized = cls.PHONE_PATTERN.sub("[PHONE_REDACTED]", sanitized)
            redacted = True

        return sanitized, redacted


pii_sanitizer = PIISanitizer()
