from typing import List, Dict, Any, Optional, Tuple
from datetime import datetime
from collections import defaultdict
from app.schemas import TransactionItem, MoMVarianceItem


class TransactionAnalyticsEngine:
    """
    Deterministic Financial Analytics Engine.
    Executes verifiable mathematical calculations over customer transactions.
    """

    @staticmethod
    def get_monthly_totals(transactions: List[TransactionItem], year: Optional[int] = None, month: Optional[int] = None) -> Dict[str, Any]:
        """Calculate total spend, credits, count, and average for a specific month."""
        if not transactions:
            return {"total_spent": 0.0, "total_income": 0.0, "tx_count": 0, "avg_tx": 0.0, "transactions": []}

        # If year/month not specified, use the latest transaction's month
        if year is None or month is None:
            latest_tx = max(transactions, key=lambda t: t.transaction_time)
            year, month = latest_tx.transaction_time.year, latest_tx.transaction_time.month

        filtered = [t for t in transactions if t.transaction_time.year == year and t.transaction_time.month == month]
        
        total_spent = sum(t.amount for t in filtered if t.direction.upper() == "DEBIT")
        total_income = sum(t.amount for t in filtered if t.direction.upper() == "CREDIT")
        debit_count = sum(1 for t in filtered if t.direction.upper() == "DEBIT")
        avg_tx = (total_spent / debit_count) if debit_count > 0 else 0.0

        return {
            "year": year,
            "month": month,
            "month_name": datetime(year, month, 1).strftime("%B"),
            "total_spent": round(total_spent, 2),
            "total_income": round(total_income, 2),
            "tx_count": len(filtered),
            "debit_count": debit_count,
            "avg_tx": round(avg_tx, 2),
            "transactions": filtered
        }

    @staticmethod
    def get_category_breakdown(transactions: List[TransactionItem], year: Optional[int] = None, month: Optional[int] = None) -> Dict[str, Any]:
        """Compute category totals and percentage shares."""
        monthly_data = TransactionAnalyticsEngine.get_monthly_totals(transactions, year, month)
        filtered = monthly_data["transactions"]

        cat_totals: Dict[str, float] = defaultdict(float)
        cat_counts: Dict[str, int] = defaultdict(int)

        for t in filtered:
            if t.direction.upper() == "DEBIT":
                cat_totals[t.category] += t.amount
                cat_counts[t.category] += 1

        total_spent = monthly_data["total_spent"]
        breakdown = []
        for cat, amount in sorted(cat_totals.items(), key=lambda x: x[1], reverse=True):
            pct = (amount / total_spent * 100) if total_spent > 0 else 0.0
            breakdown.append({
                "category": cat,
                "amount": round(amount, 2),
                "percentage": round(pct, 1),
                "count": cat_counts[cat]
            })

        top_category = breakdown[0] if breakdown else None

        return {
            "month_name": monthly_data["month_name"],
            "year": monthly_data["year"],
            "total_spent": total_spent,
            "top_category": top_category,
            "breakdown": breakdown
        }

    @staticmethod
    def get_mom_comparison(transactions: List[TransactionItem]) -> Dict[str, Any]:
        """
        Compare current month vs previous month spending.
        Provides overall delta and category-by-category variance decomposition.
        """
        if not transactions:
            return {"delta_amount": 0.0, "percentage_change": 0.0, "variance_breakdown": [], "status": "NO_DATA"}

        # Find latest month and preceding month
        months_present = sorted(list(set((t.transaction_time.year, t.transaction_time.month) for t in transactions)))
        if len(months_present) < 2:
            latest = months_present[-1] if months_present else (2026, 8)
            curr = TransactionAnalyticsEngine.get_category_breakdown(transactions, latest[0], latest[1])
            return {
                "current_month": curr["month_name"],
                "previous_month": "N/A",
                "current_spent": curr["total_spent"],
                "previous_spent": 0.0,
                "delta_amount": curr["total_spent"],
                "percentage_change": 100.0,
                "variance_breakdown": []
            }

        prev_y, prev_m = months_present[-2]
        curr_y, curr_m = months_present[-1]

        prev_data = TransactionAnalyticsEngine.get_category_breakdown(transactions, prev_y, prev_m)
        curr_data = TransactionAnalyticsEngine.get_category_breakdown(transactions, curr_y, curr_m)

        prev_spent = prev_data["total_spent"]
        curr_spent = curr_data["total_spent"]
        delta_total = curr_spent - prev_spent
        pct_change = ((delta_total / prev_spent) * 100) if prev_spent > 0 else 0.0

        # Category variance map
        prev_cats = {item["category"]: item["amount"] for item in prev_data["breakdown"]}
        curr_cats = {item["category"]: item["amount"] for item in curr_data["breakdown"]}
        all_cats = set(prev_cats.keys()).union(set(curr_cats.keys()))

        variance_items: List[MoMVarianceItem] = []
        for cat in all_cats:
            p_amt = prev_cats.get(cat, 0.0)
            c_amt = curr_cats.get(cat, 0.0)
            d_amt = c_amt - p_amt
            pct = ((d_amt / p_amt) * 100) if p_amt > 0 else (100.0 if c_amt > 0 else 0.0)

            variance_items.append(
                MoMVarianceItem(
                    category=cat,
                    previous_amount=round(p_amt, 2),
                    current_amount=round(c_amt, 2),
                    delta_amount=round(d_amt, 2),
                    percentage_change=round(pct, 1)
                )
            )

        # Sort by largest positive increase first
        variance_items.sort(key=lambda x: x.delta_amount, reverse=True)

        return {
            "current_month": curr_data["month_name"],
            "previous_month": prev_data["month_name"],
            "current_spent": curr_spent,
            "previous_spent": prev_spent,
            "delta_amount": round(delta_total, 2),
            "percentage_change": round(pct_change, 1),
            "variance_breakdown": variance_items
        }

    @staticmethod
    def get_largest_transactions(transactions: List[TransactionItem], limit: int = 5) -> List[Dict[str, Any]]:
        """Retrieve top highest debit transactions."""
        debits = [t for t in transactions if t.direction.upper() == "DEBIT"]
        sorted_debits = sorted(debits, key=lambda x: x.amount, reverse=True)[:limit]

        return [
            {
                "id": t.id,
                "amount": t.amount,
                "currency": t.currency,
                "merchant": t.merchant_name,
                "category": t.category,
                "date": t.transaction_time.strftime("%d %b %Y"),
                "description": t.description
            }
            for t in sorted_debits
        ]

    @staticmethod
    def get_recurring_expenses(transactions: List[TransactionItem]) -> List[Dict[str, Any]]:
        """Identify recurring subscriptions and bills."""
        recurring_merchants = {}
        for t in transactions:
            if t.direction.upper() == "DEBIT" and (t.is_recurring or t.category in ["Utilities", "Rent", "Entertainment"]):
                # Group by merchant name
                m = t.merchant_name
                if m not in recurring_merchants:
                    recurring_merchants[m] = {
                        "merchant_name": m,
                        "category": t.category,
                        "amount": t.amount,
                        "currency": t.currency,
                        "occurrences": 1,
                        "latest_date": t.transaction_time.strftime("%d %b %Y")
                    }
                else:
                    recurring_merchants[m]["occurrences"] += 1
                    if t.transaction_time.strftime("%d %b %Y") > recurring_merchants[m]["latest_date"]:
                        recurring_merchants[m]["latest_date"] = t.transaction_time.strftime("%d %b %Y")

        return sorted(list(recurring_merchants.values()), key=lambda x: x["amount"], reverse=True)


analytics_engine = TransactionAnalyticsEngine()
