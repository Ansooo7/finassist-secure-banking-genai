export interface Account {
  id: string;
  accountNumber: string;
  sortCode: string;
  accountType: string;
  currency: string;
  balance: number;
  status: string;
}

export interface CustomerProfile {
  customerId: string;
  customerNumber: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  homeCity: string;
  currency: string;
  totalBalance: number;
  accounts: Account[];
}

export interface Transaction {
  id: string;
  accountId: string;
  amount: number;
  currency: string;
  direction: 'DEBIT' | 'CREDIT';
  category: string;
  merchantName: string;
  description?: string;
  isRecurring: boolean;
  transactionTime: string;
}

export interface FAQDocumentSource {
  doc_id: string;
  category: string;
  title: string;
  content_snippet: string;
  similarity_score: number;
}

export interface MoMVarianceItem {
  category: string;
  previous_amount: number;
  current_amount: number;
  delta_amount: number;
  percentage_change: number;
}

export interface ExplainabilityDTO {
  data_points_used: string[];
  retrieved_faq_sources: FAQDocumentSource[];
  intent_detected: string;
  intent_confidence: number;
  guardrail_checks: Record<string, string>;
  variance_breakdown?: MoMVarianceItem[];
  is_grounded: boolean;
}

export interface ChatMessage {
  id: string;
  sender: 'USER' | 'ASSISTANT';
  text: string;
  intent?: string;
  guardrailStatus?: string;
  explainability?: ExplainabilityDTO;
  timestamp: string;
}

export interface CategorySpend {
  category: string;
  amount: number;
  percentage: number;
  transactionCount: number;
}

export interface RecurringExpense {
  merchantName: string;
  category: string;
  amount: number;
  currency: string;
  latestDate: string;
}

export interface SpendingSummary {
  currentMonth: string;
  previousMonth: string;
  currentMonthSpend: number;
  previousMonthSpend: number;
  spendDelta: number;
  percentageChange: number;
  topCategory: string;
  topCategoryAmount: number;
  categoryBreakdown: CategorySpend[];
  recurringExpenses: RecurringExpense[];
}

export interface GuardrailEvaluation {
  sanitized_prompt: string;
  is_prompt_injection: boolean;
  injection_reason?: string;
  is_financial_advice_request: boolean;
  advice_reason?: string;
  pii_redacted: boolean;
  overall_status: 'PASSED' | 'INJECTION_BLOCKED' | 'ADVICE_REFUSED';
}
