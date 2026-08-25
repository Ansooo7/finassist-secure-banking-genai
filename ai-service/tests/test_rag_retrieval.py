from app.rag.retriever import retriever


def test_rag_retrieval_contactless_limit():
    query = "What is the daily contactless limit for card payments in the UK?"
    results = retriever.retrieve(query, top_k=2)
    assert len(results) >= 1
    top_doc = results[0]
    assert "Contactless" in top_doc.title
    assert "£100" in top_doc.content_snippet
    assert top_doc.similarity_score > 0.25


def test_rag_retrieval_fraud_reporting():
    query = "How do I report a suspicious fraudulent charge on my account?"
    results = retriever.retrieve(query, top_k=2)
    assert len(results) >= 1
    top_doc = results[0]
    assert "Fraud" in top_doc.title or "Dispute" in top_doc.title
    assert "0800" in top_doc.content_snippet or "Chargeback" in top_doc.content_snippet


def test_rag_retrieval_international_wire():
    query = "What are the fees and timeline for international SWIFT transfers?"
    results = retriever.retrieve(query, top_k=2)
    assert len(results) >= 1
    top_doc = results[0]
    assert "SWIFT" in top_doc.title or "International" in top_doc.title
    assert "£9.50" in top_doc.content_snippet
