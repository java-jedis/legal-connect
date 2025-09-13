#How it works: User Query → RAG Search → Legal Documents → llm.py → Gemini API → Legal Response

"""
LLM service using Google's Gemini for response generation
"""

import os
import google.generativeai as genai
from typing import List, Dict, Any, Optional
import logging
from tenacity import retry, stop_after_attempt, wait_exponential

logger = logging.getLogger(__name__)

class GeminiService:
    """Service for generating responses using Google's Gemini LLM"""
    
    def __init__(self):
        self.api_key = os.getenv("GOOGLE_API_KEY")
        self.model_name = os.getenv("GEMINI_MODEL", "gemini-2.5-pro")
        
        if not self.api_key:
            raise ValueError("GOOGLE_API_KEY environment variable is required")
        genai.configure(api_key=self.api_key)
        self.model = genai.GenerativeModel(self.model_name)
        
        # System prompt for legal assistant
        self.system_prompt = """You are a knowledgeable legal assistant specializing in Bangladesh law. 
You help users understand legal documents, laws, and regulations in Bangladesh. 

Your responsibilities:
1. Provide accurate legal information based on the provided context
2. Explain complex legal concepts in simple language
3. Reference specific legal documents and sections when applicable  
4. Always cite genuine source information (document names, acts, laws) not generic "Source 1, 2, 3"
5. Be helpful but remind users to consult qualified lawyers for legal advice

Guidelines:
- Always base your answers on the provided context
- When referencing information, cite the actual document name and page (e.g., "Divorce Act, 1869, Page 26")
- If information is not in the context, clearly state this
- Use both Bengali and English terms when appropriate
- Provide specific legal citations and references when possible
- Be respectful and professional
- Never provide definitive legal advice, only information
IMPORTANT RESTRICTIONS:
- When citing sources, use the actual document names and details provided in the context, NOT generic "Source 1", "Source 2" labels.
- Only respond to legal questions related to Bangladesh law
- If asked non-legal questions, politely redirect to legal matters
- Do not provide general knowledge, entertainment, or off-topic responses
- Always stay within your role as a legal assistant """
        
        logger.info(f"Initialized GeminiService with model: {self.model_name}")
    
    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=4, max=10)
    )
    async def generate_response(
        self,
        query: str,
        context_documents: List[Dict[str, Any]],
        chat_history: List[Dict[str, str]] = None
    ) -> Dict[str, Any]:
        """
        Generate response using RAG approach
        
        Args:
            query: User's question
            context_documents: List of relevant documents from vector search
            chat_history: Previous conversation history
        
        Returns:
            Generated response with metadata
        """
        try:
            # Prepare context from retrieved documents
            context = self._prepare_context(context_documents)
            
            # Prepare chat history
            history_text = self._prepare_history(chat_history) if chat_history else ""
            
            # Create prompt
            prompt = self._create_prompt(query, context, history_text)
            
            # Generate response
            response = self.model.generate_content(
                prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.7,
                    top_p=0.8,
                    top_k=40,
                    max_output_tokens=2048,
                )
            )
            
            # Extract response text
            response_text = response.text if response.text else "I apologize, but I couldn't generate a response. Please try again."
            
            # Prepare metadata
            metadata = {
                "sources": self._extract_sources(context_documents),
                "context_used": len(context_documents),
                "model": self.model_name,
                "confidence": self._estimate_confidence(context_documents, query)
            }
            
            logger.info(f"Generated response for query: {query[:100]}...")
            
            return {
                "response": response_text,
                "metadata": metadata
            }
            
        except Exception as e:
            logger.error(f"Error generating response: {e}")
            return {
                "response": "I apologize, but I encountered an error while processing your request. Please try again.",
                "metadata": {"error": str(e)}
            }
    
    def _prepare_context(self, documents: List[Dict[str, Any]]) -> str:
        """Prepare context from retrieved documents with genuine source information"""
        if not documents:
            return "No relevant documents found."
        
        context_parts = []
        for i, doc in enumerate(documents, 1):
            # Create genuine source information instead of generic "Source X"
            document_name = doc.get('document_name', 'Unknown Document')
            filename = doc.get('filename', '')
            page_number = doc.get('page_number')
            
            # Format genuine source info
            if document_name and document_name != 'Unknown Document':
                source_info = f"[From: {document_name}"
                if page_number:
                    source_info += f", Page {page_number}"
                if filename and filename != document_name:
                    source_info += f" ({filename})"
                source_info += "]"
            else:
                source_info = f"[Document: {filename or 'Unknown'}"
                if page_number:
                    source_info += f", Page {page_number}"
                source_info += "]"
            
            text = doc.get('text', '').strip()
            if text:
                context_parts.append(f"{source_info}\n{text}\n")
        
        return "\n".join(context_parts)
    
    def _prepare_history(self, history: List[Dict[str, str]]) -> str:
        """Prepare chat history for context"""
        if not history:
            return ""
        
        history_parts = []
        for msg in history[-5:]:  # Last 5 messages for context
            role = msg.get('role', '').title()
            content = msg.get('content', '').strip()
            if content:
                history_parts.append(f"{role}: {content}")
        
        return "\nPrevious conversation:\n" + "\n".join(history_parts) + "\n"
    
    def _create_prompt(self, query: str, context: str, history: str) -> str:
        """Create the complete prompt for the model"""
        prompt = f"""{self.system_prompt}

{history}

Context Information:
{context}

User Question: {query}

Please provide a helpful and accurate response based on the context information provided above. 

IMPORTANT INSTRUCTIONS:
- If this question refers to something discussed earlier in the conversation, use that context along with the provided documents
- If the question uses words like "this", "that", "it", "eta", "details", "explain more" - refer to the previous conversation for context
- Always cite actual document names and page numbers, not generic "Source 1, 2, 3"
- If the context doesn't contain enough information to answer the question, please state that clearly
- Use both Bengali and English terms when appropriate for Bangladesh legal topics

Please provide a helpful and accurate response based on the context information provided."""
        
        return prompt
    
    def _extract_sources(self, documents: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Extract source information from documents"""
        sources = []
        for doc in documents:
            source = {
                "document_name": doc.get('document_name', 'Unknown'),
                "filename": doc.get('filename', ''),
                "page_number": doc.get('page_number'),
                "similarity_score": doc.get('score', 0.0)
            }
            sources.append(source)
        
        return sources
    
    def _estimate_confidence(self, documents: List[Dict[str, Any]], query: str) -> float:
        """Estimate confidence based on document relevance and query match"""
        if not documents:
            return 0.0
        
        # Average similarity score
        avg_score = sum(doc.get('score', 0.0) for doc in documents) / len(documents)
        
        # Boost confidence if we have multiple relevant documents
        confidence = avg_score
        if len(documents) >= 3:
            confidence *= 1.1
        
        # Cap at 1.0
        return min(confidence, 1.0)
    
    async def generate_summary(self, documents: List[Dict[str, Any]]) -> str:
        """Generate a summary of multiple documents"""
        try:
            context = self._prepare_context(documents)
            
            prompt = f"""Please provide a concise summary of the following legal documents:

{context}

Summary:"""
            
            response = self.model.generate_content(
                prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.5,
                    max_output_tokens=1024,
                )
            )
            
            return response.text if response.text else "Unable to generate summary."
            
        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            return "Error generating summary."
    
    async def extract_key_points(self, text: str) -> List[str]:
        """Extract key points from a legal document"""
        try:
            prompt = f"""Extract the key points from this legal document text:

{text}

Please provide a bullet-point list of the most important aspects, sections, or provisions."""
            
            response = self.model.generate_content(
                prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.3,
                    max_output_tokens=1024,
                )
            )
            
            if response.text:
                # Parse bullet points
                points = [line.strip('- •').strip() for line in response.text.split('\n') if line.strip().startswith(('- ', '• ', '*'))]
                return points
            
            return []
            
        except Exception as e:
            logger.error(f"Error extracting key points: {e}")
            return []


