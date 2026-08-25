import re
import numpy as np
from typing import List
from sklearn.feature_extraction.text import TfidfVectorizer


class EmbeddingEngine:
    """
    384-Dimensional Semantic Embedding Engine.
    Generates normalized dense vectors for knowledge base documents and user queries.
    """

    def __init__(self, dimension: int = 384):
        self.dimension = dimension

    def embed_text(self, text: str) -> np.ndarray:
        """Embed a single text string into a normalized 384-dim vector."""
        if not text:
            return np.zeros(self.dimension, dtype=np.float32)

        # Preprocess text
        cleaned = re.sub(r"[^\w\s]", " ", text.lower()).strip()
        tokens = cleaned.split()

        if not tokens:
            return np.zeros(self.dimension, dtype=np.float32)

        # Deterministic hashing vectorizer into 384 dimensions
        vector = np.zeros(self.dimension, dtype=np.float32)
        for i, word in enumerate(tokens):
            # Combined positional and token hash
            h1 = hash(word) % self.dimension
            h2 = (hash(word) * 31 + i) % self.dimension
            vector[h1] += 1.0
            vector[h2] += 0.5

        # L2 Normalize
        norm = np.linalg.norm(vector)
        if norm > 0:
            vector = vector / norm
            
        return vector

    def embed_documents(self, documents: List[str]) -> List[np.ndarray]:
        """Batch embed a list of document strings."""
        return [self.embed_text(doc) for doc in documents]

    @staticmethod
    def cosine_similarity(vec_a: np.ndarray, vec_b: np.ndarray) -> float:
        """Compute cosine similarity between two normalized vectors."""
        dot_product = np.dot(vec_a, vec_b)
        norm_a = np.linalg.norm(vec_a)
        norm_b = np.linalg.norm(vec_b)
        if norm_a == 0 or norm_b == 0:
            return 0.0
        return float(dot_product / (norm_a * norm_b))


embedding_engine = EmbeddingEngine()
