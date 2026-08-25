import axios from 'axios';
import {
  CustomerProfile,
  Transaction,
  SpendingSummary,
  ChatMessage,
  GuardrailEvaluation
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('finassist_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Fallback Mock Data for Standalone Preview / Vercel
const MOCK_PROFILE: CustomerProfile = {
  customerId: 'c0000001-0000-0000-0000-000000000001',
  customerNumber: 'CUST-UK-1001',
  firstName: 'Oliver',
  lastName: 'Twist',
  fullName: 'Oliver Twist',
  email: 'oliver.twist@gmail.com',
  phoneNumber: '+447911123456',
  homeCity: 'London',
  currency: 'GBP',
  totalBalance: 15420.50,
  accounts: [
    {
      id: 'a0000001-0000-0000-0000-000000000001',
      accountNumber: '12345678',
      sortCode: '20-45-14',
      accountType: 'CURRENT',
      currency: 'GBP',
      balance: 15420.50,
      status: 'ACTIVE'
    }
  ]
};

const MOCK_SUMMARY: SpendingSummary = {
  currentMonth: 'August 2026',
  previousMonth: 'July 2026',
  currentMonthSpend: 2843.98,
  previousMonthSpend: 2155.48,
  spendDelta: 688.50,
  percentageChange: 31.9,
  topCategory: 'Dining',
  topCategoryAmount: 590.00,
  categoryBreakdown: [
    { category: 'Rent', amount: 1450.00, percentage: 51.0, transactionCount: 1 },
    { category: 'Dining', amount: 590.00, percentage: 20.7, transactionCount: 2 },
    { category: 'Shopping', amount: 350.00, percentage: 12.3, transactionCount: 1 },
    { category: 'Transport', amount: 145.00, percentage: 5.1, transactionCount: 1 },
    { category: 'Groceries', amount: 115.00, percentage: 4.0, transactionCount: 1 },
    { category: 'Utilities', amount: 110.00, percentage: 3.9, transactionCount: 1 },
    { category: 'Entertainment', amount: 83.98, percentage: 3.0, transactionCount: 3 }
  ],
  recurringExpenses: [
    { merchantName: 'London Residential Properties', category: 'Rent', amount: 1450.00, currency: 'GBP', latestDate: '01 Aug 2026' },
    { merchantName: 'British Gas Energy', category: 'Utilities', amount: 110.00, currency: 'GBP', latestDate: '20 Aug 2026' },
    { merchantName: 'PureGym London', category: 'Entertainment', amount: 55.00, currency: 'GBP', latestDate: '05 Aug 2026' },
    { merchantName: 'Netflix UK', category: 'Entertainment', amount: 17.99, currency: 'GBP', latestDate: '02 Aug 2026' },
    { merchantName: 'Spotify UK', category: 'Entertainment', amount: 10.99, currency: 'GBP', latestDate: '03 Aug 2026' }
  ]
};

const MOCK_TRANSACTIONS: Transaction[] = [
  { id: 'd-22', accountId: 'a-1', amount: 4200.00, currency: 'GBP', direction: 'CREDIT', category: 'Income', merchantName: 'FinTech Global Ltd', description: 'Monthly Employment Salary', isRecurring: true, transactionTime: '2026-08-25T07:00:00Z' },
  { id: 'd-21', accountId: 'a-1', amount: 110.00, currency: 'GBP', direction: 'DEBIT', category: 'Utilities', merchantName: 'British Gas Energy', description: 'Monthly Gas & Electricity Bill', isRecurring: true, transactionTime: '2026-08-20T09:00:00Z' },
  { id: 'd-20', accountId: 'a-1', amount: 145.00, currency: 'GBP', direction: 'DEBIT', category: 'Transport', merchantName: 'Uber & Heathrow Express', description: 'Airport Transportation', isRecurring: false, transactionTime: '2026-08-20T06:45:00Z' },
  { id: 'd-19', accountId: 'a-1', amount: 350.00, currency: 'GBP', direction: 'DEBIT', category: 'Shopping', merchantName: 'Apple Regent Street', description: 'Accessories & Electronics', isRecurring: false, transactionTime: '2026-08-17T14:00:00Z' },
  { id: 'd-18', accountId: 'a-1', amount: 270.00, currency: 'GBP', direction: 'DEBIT', category: 'Dining', merchantName: 'Sketch London', description: 'Weekend Tasting Menu', isRecurring: false, transactionTime: '2026-08-14T21:00:00Z' },
  { id: 'd-17', accountId: 'a-1', amount: 320.00, currency: 'GBP', direction: 'DEBIT', category: 'Dining', merchantName: 'Hawksmoor Steakhouse', description: 'Celebration Dinner', isRecurring: false, transactionTime: '2026-08-10T20:30:00Z' },
  { id: 'd-16', accountId: 'a-1', amount: 115.00, currency: 'GBP', direction: 'DEBIT', category: 'Groceries', merchantName: 'Waitrose & Partners', description: 'Specialty Groceries', isRecurring: false, transactionTime: '2026-08-07T16:30:00Z' }
];

export const bankingApi = {
  login: async (username = 'oliver', password = 'Password123!') => {
    try {
      const res = await apiClient.post('/auth/login', { username, password });
      if (res.data.data?.token) {
        localStorage.setItem('finassist_token', res.data.data.token);
      }
      return res.data.data;
    } catch {
      localStorage.setItem('finassist_token', 'demo_jwt_token_2026');
      return {
        username: 'oliver',
        fullName: 'Oliver Twist',
        role: 'ROLE_CUSTOMER',
        token: 'demo_jwt_token_2026'
      };
    }
  },

  getProfile: async (): Promise<CustomerProfile> => {
    try {
      const res = await apiClient.get('/customers/me');
      return res.data.data;
    } catch {
      return MOCK_PROFILE;
    }
  },

  getSpendingSummary: async (): Promise<SpendingSummary> => {
    try {
      const res = await apiClient.get('/analytics/spending-summary');
      return res.data.data;
    } catch {
      return MOCK_SUMMARY;
    }
  },

  getTransactions: async (): Promise<Transaction[]> => {
    try {
      const res = await apiClient.get('/transactions/my-transactions?page=0&size=20');
      return res.data.data?.content || MOCK_TRANSACTIONS;
    } catch {
      return MOCK_TRANSACTIONS;
    }
  },

  sendMessage: async (message: string, sessionId?: string): Promise<ChatMessage> => {
    try {
      const res = await apiClient.post('/chat/message', { message, sessionId });
      const d = res.data.data;
      return {
        id: d.messageId,
        sender: 'ASSISTANT',
        text: d.answer,
        intent: d.intent,
        guardrailStatus: d.guardrailStatus,
        explainability: d.explainability,
        timestamp: d.timestamp || new Date().toISOString()
      };
    } catch {
      // Standalone intelligent client-side simulation for offline preview
      const lower = message.toLowerCase();
      if (lower.includes('ignore') || lower.includes('dan') || lower.includes('prompt')) {
        return {
          id: Math.random().toString(),
          sender: 'ASSISTANT',
          text: 'I cannot process this request. FinAssist is designed strictly for authorized personal banking assistance and security policies prohibit instruction overrides or cross-customer queries.',
          intent: 'INJECTION_BLOCKED',
          guardrailStatus: 'INJECTION_BLOCKED',
          explainability: {
            data_points_used: ['Adversarial pattern blocked by Tier-2 Security Guardrail'],
            retrieved_faq_sources: [],
            intent_detected: 'INJECTION_BLOCKED',
            intent_confidence: 1.0,
            guardrail_checks: { PROMPT_INJECTION_DEFENSE: 'BLOCKED (Instruction Override Attempt)' },
            is_grounded: false
          },
          timestamp: new Date().toISOString()
        };
      }

      if (lower.includes('invest') || lower.includes('bitcoin') || lower.includes('crypto') || lower.includes('stock')) {
        return {
          id: Math.random().toString(),
          sender: 'ASSISTANT',
          text: 'I cannot provide investment or financial advice. FinAssist is an informational banking assistant and is not authorized to give regulated financial, stock, or cryptocurrency recommendations. For personalized investment guidance, please speak with an independent, qualified financial adviser.',
          intent: 'ADVICE_REFUSED',
          guardrailStatus: 'ADVICE_REFUSED',
          explainability: {
            data_points_used: ['Query flagged by Tier-4 Financial Advice Boundary Guardrail'],
            retrieved_faq_sources: [],
            intent_detected: 'ADVICE_REFUSED',
            intent_confidence: 1.0,
            guardrail_checks: { FINANCIAL_ADVICE_GUARDRAIL: 'REFUSED (Cryptocurrency / Stock Speculation Advice)' },
            is_grounded: true
          },
          timestamp: new Date().toISOString()
        };
      }

      if (lower.includes('contactless') || lower.includes('limit')) {
        return {
          id: Math.random().toString(),
          sender: 'ASSISTANT',
          text: '**Contactless Spending Limits & Security**\n\nThe standard UK contactless payment limit is £100 per single transaction. For fraud prevention, a Chip & PIN authorization is automatically requested whenever cumulative contactless spending reaches £300 without a PIN reset.\n\n*(Source: Verified Banking Policy Knowledge Base - Doc ID: KB-CRD-001)*',
          intent: 'BANKING_FAQ',
          guardrailStatus: 'PASSED',
          explainability: {
            data_points_used: ['Retrieved policy document: Contactless Spending Limits & Security (Relevance: 89%)'],
            retrieved_faq_sources: [
              {
                doc_id: 'KB-CRD-001',
                category: 'CARD',
                title: 'Contactless Spending Limits & Security',
                content_snippet: 'The standard UK contactless payment limit is £100 per single transaction...',
                similarity_score: 0.89
              }
            ],
            intent_detected: 'BANKING_FAQ',
            intent_confidence: 0.92,
            guardrail_checks: {
              PII_SANITIZER: 'PASSED',
              PROMPT_INJECTION_DEFENSE: 'PASSED',
              FACTUAL_GROUNDING: 'Grounded in 1 verified banking policy FAQ.'
            },
            is_grounded: true
          },
          timestamp: new Date().toISOString()
        };
      }

      // Default MoM spending explanation
      return {
        id: Math.random().toString(),
        sender: 'ASSISTANT',
        text: 'Your spending **increased by 31.9%** (£688.50) compared to July.\n- **August Spend:** £2,843.98\n- **July Spend:** £2,155.48\n\n**Key Category Changes:**\n- **Shopping:** +£350.00 (+100.0%)\n- **Dining:** +£320.00 (+118.5%)\n- **Transport:** +£80.00 (+123.1%)',
        intent: 'TRANSACTION_ANALYTICS',
        guardrailStatus: 'PASSED',
        explainability: {
          data_points_used: [
            'Evaluated 22 transactions across July and August.',
            'Net MoM spend delta: £688.50 (31.9%)'
          ],
          retrieved_faq_sources: [],
          intent_detected: 'TRANSACTION_ANALYTICS',
          intent_confidence: 0.95,
          guardrail_checks: {
            PII_SANITIZER: 'PASSED',
            PROMPT_INJECTION_DEFENSE: 'PASSED',
            FINANCIAL_ADVICE_GUARDRAIL: 'PASSED',
            CUSTOMER_ISOLATION: 'PASSED (Restricted to Customer: c0000001-0000-0000-0000-000000000001)',
            FACTUAL_GROUNDING: 'Grounded in 2 verifiable transaction data points.'
          },
          variance_breakdown: [
            { category: 'Shopping', previous_amount: 0.0, current_amount: 350.0, delta_amount: 350.0, percentage_change: 100.0 },
            { category: 'Dining', previous_amount: 270.0, current_amount: 590.0, delta_amount: 320.0, percentage_change: 118.5 },
            { category: 'Transport', previous_amount: 65.0, current_amount: 145.0, delta_amount: 80.0, percentage_change: 123.1 }
          ],
          is_grounded: true
        },
        timestamp: new Date().toISOString()
      };
    }
  },

  evaluateGuardrails: async (prompt: string): Promise<GuardrailEvaluation> => {
    try {
      const res = await axios.post('http://localhost:8000/api/v1/ai/guardrails/evaluate', { prompt });
      return res.data;
    } catch {
      const isInj = /ignore|disregard|dan|unrestricted|system prompt/i.test(prompt);
      const isAdv = /invest|crypto|bitcoin|stock|buy/i.test(prompt);
      return {
        sanitized_prompt: prompt,
        is_prompt_injection: isInj,
        injection_reason: isInj ? 'Instruction Override Attempt' : undefined,
        is_financial_advice_request: isAdv,
        advice_reason: isAdv ? 'Cryptocurrency / Stock Speculation Advice' : undefined,
        pii_redacted: false,
        overall_status: isInj ? 'INJECTION_BLOCKED' : (isAdv ? 'ADVICE_REFUSED' : 'PASSED')
      };
    }
  }
};
