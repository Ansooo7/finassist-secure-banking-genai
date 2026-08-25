from typing import List, Dict, Any

BANKING_KNOWLEDGE_BASE: List[Dict[str, Any]] = [
    # 1. ACCOUNT FAQs
    {
        "doc_id": "KB-ACC-001",
        "category": "ACCOUNT",
        "title": "Arranged Overdraft & Minimum Balance Policy",
        "content": "Our Standard Current Account has no mandatory minimum monthly balance. Eligible customers can apply for an Arranged Overdraft up to £1,000 with a fee-free buffer of £25. Overdraft interest is calculated daily at an effective EAR of 19.9%. Unarranged overdrafts are automatically declined without penalty fees.",
        "keywords": ["overdraft", "minimum balance", "buffer", "interest", "fees", "limit"]
    },
    {
        "doc_id": "KB-ACC-002",
        "category": "ACCOUNT",
        "title": "Monthly Statements & Tax Certificates",
        "content": "Digital monthly bank statements are generated on the 1st business day of each calendar month. Customers can view and export statements in PDF and CSV format via the web console or mobile app for up to 7 years. Annual Interest Certificates for HMRC tax filing are released every May.",
        "keywords": ["statement", "download", "pdf", "csv", "tax", "hmrc", "export", "certificate"]
    },

    # 2. PAYMENT FAQs
    {
        "doc_id": "KB-PAY-001",
        "category": "PAYMENT",
        "title": "UK Faster Payments & Daily Transfer Limits",
        "content": "UK Faster Payments are processed 24/7/365 with typical settlement in under 15 seconds. The default single transaction limit is £25,000, and the daily cumulative transfer limit is £50,000. Transfers exceeding £50,000 must be submitted via CHAPS or authorized via telephone banking.",
        "keywords": ["faster payments", "transfer limit", "send money", "daily limit", "chaps", "payment speed"]
    },
    {
        "doc_id": "KB-PAY-002",
        "category": "PAYMENT",
        "title": "International SWIFT Transfers & Exchange Fees",
        "content": "International payments to over 180 countries are supported via SWIFT and SEPA networks. Standard international outbound transfers incur a flat processing fee of £9.50. Real-time mid-market exchange rates with a transparent 0.45% FX margin apply. Deliveries typically arrive within 1 to 3 business days.",
        "keywords": ["international transfer", "swift", "sepa", "fx", "foreign exchange", "overseas", "wire"]
    },
    {
        "doc_id": "KB-PAY-003",
        "category": "PAYMENT",
        "title": "Direct Debit Guarantee & Cancellation",
        "content": "Under the UK Direct Debit Guarantee, if an error is made in the payment of your Direct Debit, you are entitled to a full and immediate refund from your bank. You can cancel any scheduled Direct Debit mandate at any time up to 23:59 on the working day before payment is due.",
        "keywords": ["direct debit", "mandate", "cancel direct debit", "refund", "guarantee", "recurring bill"]
    },

    # 3. CARD FAQs
    {
        "doc_id": "KB-CRD-001",
        "category": "CARD",
        "title": "Contactless Spending Limits & Security",
        "content": "The standard UK contactless payment limit is £100 per single transaction. For fraud prevention, a Chip & PIN authorization is automatically requested whenever cumulative contactless spending reaches £300 without a PIN reset. Apple Pay and Google Pay transactions authenticated with biometrics do not have a £100 cap.",
        "keywords": ["contactless", "card limit", "pin", "apple pay", "google pay", "tap", "chip and pin"]
    },
    {
        "doc_id": "KB-CRD-002",
        "category": "CARD",
        "title": "Freezing Cards & Ordering Replacements",
        "content": "If you misplace your debit or credit card, you can instantly freeze or unfreeze it in the mobile app under Card Controls. If your card is permanently lost or stolen, report it immediately to deactivate the chip and order a free replacement card, which arrives by Royal Mail within 3-5 business days.",
        "keywords": ["freeze card", "lost card", "stolen card", "replacement", "block card", "order card"]
    },

    # 4. SECURITY & FRAUD FAQs
    {
        "doc_id": "KB-SEC-001",
        "category": "SECURITY",
        "title": "Reporting Fraud & Suspicious Transactions",
        "content": "If you notice an unrecognized charge or suspect unauthorized access to your account, notify our 24/7 Fraud Operations Center immediately at 0800-012-3456 or tap 'Report Suspicious Activity' in the app. Unauthorized transactions verified as fraudulent are protected under UK Payment Services Regulations with full reimbursement.",
        "keywords": ["fraud", "suspicious", "unauthorized", "stolen money", "scam", "reimbursement", "hotline"]
    },
    {
        "doc_id": "KB-SEC-002",
        "category": "SECURITY",
        "title": "Anti-Phishing & Safe Banking Guidelines",
        "content": "FinAssist Bank will NEVER ask for your full password, card PIN, or SMS One-Time Passcode (OTP) over phone, email, or text. Never click on unverified SMS links claiming your account has been locked. Always verify security alerts directly inside the authenticated web or mobile banking dashboard.",
        "keywords": ["phishing", "scam", "otp", "passcode", "security", "fake email", "safe banking"]
    },

    # 5. DISPUTE & CHARGEBACK FAQs
    {
        "doc_id": "KB-DSP-001",
        "category": "DISPUTE",
        "title": "Card Chargeback & Dispute Procedures",
        "content": "If you are charged twice for a single purchase, did not receive ordered goods, or the merchant refused a legitimate refund, you can initiate a Chargeback claim under Visa/Mastercard scheme rules within 120 days of the transaction date. Most dispute resolutions are completed within 10 to 14 business days.",
        "keywords": ["dispute", "chargeback", "double charge", "duplicate", "refund refused", "claim"]
    }
]
