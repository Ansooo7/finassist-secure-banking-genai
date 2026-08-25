from datetime import datetime
from app.schemas import TransactionItem
from app.analytics.transaction_engine import analytics_engine


def make_test_transactions():
    return [
        # July 2026 Transactions
        TransactionItem(
            id="tx-01", amount=120.00, direction="DEBIT", category="Dining",
            merchant_name="Nando's", transaction_time=datetime(2026, 7, 5, 12, 30)
        ),
        TransactionItem(
            id="tx-02", amount=85.50, direction="DEBIT", category="Groceries",
            merchant_name="Tesco", transaction_time=datetime(2026, 7, 10, 15, 0)
        ),
        # August 2026 Transactions
        TransactionItem(
            id="tx-03", amount=350.00, direction="DEBIT", category="Dining",
            merchant_name="Hawksmoor", transaction_time=datetime(2026, 8, 12, 19, 30)
        ),
        TransactionItem(
            id="tx-04", amount=95.00, direction="DEBIT", category="Groceries",
            merchant_name="Waitrose", transaction_time=datetime(2026, 8, 14, 11, 0)
        ),
        TransactionItem(
            id="tx-05", amount=15.99, direction="DEBIT", category="Entertainment",
            merchant_name="Netflix", is_recurring=True, transaction_time=datetime(2026, 8, 1, 9, 0)
        ),
        TransactionItem(
            id="tx-06", amount=2800.00, direction="CREDIT", category="Income",
            merchant_name="Employer Payroll", transaction_time=datetime(2026, 8, 25, 8, 0)
        )
    ]


def test_monthly_totals_calculation():
    txs = make_test_transactions()
    july_data = analytics_engine.get_monthly_totals(txs, 2026, 7)
    assert july_data["total_spent"] == 205.50
    assert july_data["debit_count"] == 2

    aug_data = analytics_engine.get_monthly_totals(txs, 2026, 8)
    assert aug_data["total_spent"] == 460.99
    assert aug_data["total_income"] == 2800.00


def test_category_breakdown():
    txs = make_test_transactions()
    cats = analytics_engine.get_category_breakdown(txs, 2026, 8)
    assert cats["total_spent"] == 460.99
    assert cats["top_category"]["category"] == "Dining"
    assert cats["top_category"]["amount"] == 350.00


def test_mom_variance_comparison():
    txs = make_test_transactions()
    mom = analytics_engine.get_mom_comparison(txs)
    assert mom["current_spent"] == 460.99
    assert mom["previous_spent"] == 205.50
    assert mom["delta_amount"] == 255.49
    assert mom["percentage_change"] > 100.0

    # Dining increased from 120 to 350 -> delta +230
    dining_item = next(item for item in mom["variance_breakdown"] if item.category == "Dining")
    assert dining_item.delta_amount == 230.00


def test_recurring_subscription_detection():
    txs = make_test_transactions()
    recurring = analytics_engine.get_recurring_expenses(txs)
    assert len(recurring) >= 1
    assert any(r["merchant_name"] == "Netflix" for r in recurring)


def test_largest_transactions():
    txs = make_test_transactions()
    largest = analytics_engine.get_largest_transactions(txs, limit=2)
    assert len(largest) == 2
    assert largest[0]["amount"] == 350.00
    assert largest[0]["merchant"] == "Hawksmoor"
