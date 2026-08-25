import re
from typing import Tuple, Optional


class PromptInjectionDefender:
    """
    Tier 2 AI Guardrail: Prompt Injection & Adversarial Attack Defense.
    Detects jailbreaks, system prompt extraction, persona hijacking, and cross-customer query tampering.
    """

    # Adversarial pattern rules
    INJECTION_PATTERNS = [
        # 1. Instruction Overrides & Jailbreaks
        (re.compile(r"ignore\s+(all\s+)?(previous|prior|above)\s+(instructions|prompts|rules|commands)", re.IGNORECASE),
         "Instruction Override Attempt"),
        (re.compile(r"disregard\s+(all\s+)?(previous|prior|above|system)\s+(instructions|directives|rules)", re.IGNORECASE),
         "System Directive Disregard"),
        (re.compile(r"you\s+are\s+(now\s+)?(dan|unrestricted|godmode|jailbroken|evil|unfiltered)", re.IGNORECASE),
         "Persona Hijacking / DAN Jailbreak"),
        (re.compile(r"(you\s+are|act\s+as)\s+an?\s+(unrestricted|unfiltered|jailbroken|rogue)\s+ai", re.IGNORECASE),
         "Unrestricted Persona Roleplay"),

        # 2. System Prompt & Instruction Extraction
        (re.compile(r"(repeat|print|reveal|output|show|dump)\s+(your\s+)?(system\s+prompt|initial\s+instructions|internal\s+rules)", re.IGNORECASE),
         "System Prompt Extraction"),
        (re.compile(r"(what\s+are\s+your\s+instructions|show\s+everything\s+above)", re.IGNORECASE),
         "Instruction Exfiltration"),

        # 3. Cross-Customer Exfiltration & SQL Injection
        (re.compile(r"(show|fetch|get|dump|list)\s+(other|all|another|different)\s+(customer|user|account)('s|\s+)?\s*(balance|data|transactions)", re.IGNORECASE),
         "Cross-Customer Data Exfiltration Attempt"),
        (re.compile(r"(switch|change|masquerade\s+as)\s+(to\s+)?(user|customer)\s+[a-zA-Z0-9_\-]+", re.IGNORECASE),
         "Session Impersonation Attack"),
        (re.compile(r"(select\s+\*\s+from|union\s+select|drop\s+table|delete\s+from)", re.IGNORECASE),
         "SQL Injection Syntax in Natural Prompt"),

        # 4. Delimiter & Tag Injection
        (re.compile(r"(</?(system|assistant|user|human|prompt)>|\[system_message\])", re.IGNORECASE),
         "XML/Tag Delimiter Hijacking")
    ]

    @classmethod
    def evaluate(cls, prompt: str) -> Tuple[bool, Optional[str]]:
        """
        Evaluate prompt for malicious injection patterns.
        Returns: (is_injection_detected, reason)
        """
        if not prompt or not prompt.strip():
            return False, None

        normalized = prompt.strip()

        for pattern, reason in cls.INJECTION_PATTERNS:
            if pattern.search(normalized):
                return True, reason

        return False, None


injection_defender = PromptInjectionDefender()
