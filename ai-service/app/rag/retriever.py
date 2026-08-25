import re
import logging
from typing import List, Tuple, Dict, Any
from app.rag.knowledge_base import BANKING_KNOWLEDGE_BASE
from app.core.embeddings import embedding_engine
from app.schemas import FAQDocumentSource
from app.config import settings

logger = logging.getLogger("finassist.rag")


class KnowledgeRetriever:
    """
    Hybrid Semantic & Keyword RAG Retriever.
    Indexes banking knowledge base FAQs and retrieves top-K relevant documents.
    """

    def __init__(self):
        self.documents = BANKING_KNOWLEDGE_BASE
        self.doc_embeddings: List[Tuple[Dict[str, Any], Any]] = []
        self._build_index()

    def _build_index(self):
        """Pre-compute embeddings for all knowledge base documents."""
        logger.info(f"Indexing {len(self.documents)} banking knowledge base documents...")
        for doc in self.documents:
            # Combine title, keywords, and content for rich semantic indexing
            search_text = f"{doc['title']} {' '.join(doc['keywords'])} {doc['content']}"
            embedding = embedding_engine.embed_text(search_text)
            self.doc_embeddings.append((doc, embedding))
        logger.info("[RAG Index] Knowledge base vector index built successfully.")

    def retrieve(self, query: str, top_k: int = 3) -> List[FAQDocumentSource]:
        """
        Retrieve top-K most relevant FAQ documents for a given query.
        Combines cosine similarity with keyword boosting.
        """
        if not query or not query.strip():
            return []

        STOP_WORDS = {"what", "is", "the", "for", "in", "and", "a", "an", "to", "how", "do", "i", "my", "of", "uk", "&"}
        query_words = set(w for w in re.sub(r"[^\w\s]", " ", query.lower()).split() if w not in STOP_WORDS)
        query_vec = embedding_engine.embed_text(query)

        scored_docs = []
        for doc, doc_vec in self.doc_embeddings:
            # 1. Cosine similarity
            cosine_sim = embedding_engine.cosine_similarity(query_vec, doc_vec)

            # 2. Keyword boost on domain keywords
            keyword_score = 0.0
            for kw in doc["keywords"]:
                if kw.lower() in query.lower():
                    keyword_score += 0.35

            title_words = set(w for w in re.sub(r"[^\w\s]", " ", doc["title"].lower()).split() if w not in STOP_WORDS)
            title_overlap = len(query_words.intersection(title_words))
            keyword_score += (title_overlap * 0.2)
            keyword_score = min(0.8, keyword_score)

            # Combined hybrid score (0.0 - 1.0)
            hybrid_score = min(1.0, (cosine_sim * 0.3) + (keyword_score * 0.7))

            if hybrid_score >= settings.similarity_threshold:
                scored_docs.append((doc, hybrid_score))

        # Sort by score descending
        scored_docs.sort(key=lambda x: x[1], reverse=True)
        top_results = scored_docs[:top_k]

        results = []
        for doc, score in top_results:
            results.append(
                FAQDocumentSource(
                    doc_id=doc["doc_id"],
                    category=doc["category"],
                    title=doc["title"],
                    content_snippet=doc["content"],
                    similarity_score=round(float(score), 4)
                )
            )

        return results


retriever = KnowledgeRetriever()
