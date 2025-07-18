# LangChain utilities for RAG pipeline and document processing
from langchain.chains import RetrievalQA
from langchain_openai import ChatOpenAI
from langchain.document_loaders import PyPDFLoader, Docx2txtLoader, TextLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.prompts import PromptTemplate
from typing import List, Dict, Any
import os

class LangChainManager:
    def __init__(self, openai_api_key: str = None):
        self.openai_api_key = openai_api_key or os.getenv("OPENAI_API_KEY")
        self.llm = ChatOpenAI(
            api_key=self.openai_api_key,
            model_name="gpt-3.5-turbo",
            temperature=0.7
        )
        
    def load_document(self, file_path: str) -> List[str]:
        """Load document based on file extension"""
        file_extension = os.path.splitext(file_path)[1].lower()
        
        if file_extension == '.pdf':
            loader = PyPDFLoader(file_path)
        elif file_extension in ['.docx', '.doc']:
            loader = Docx2txtLoader(file_path)
        elif file_extension == '.txt':
            loader = TextLoader(file_path)
        else:
            raise ValueError(f"Unsupported file type: {file_extension}")
        
        documents = loader.load()
        return [doc.page_content for doc in documents]
    
    def create_qa_chain(self, vectorstore, custom_prompt: str = None):
        """Create a QA chain with the vectorstore"""
        
        # Default prompt template for legal documents
        default_prompt = """
        You are a legal assistant AI. Use the following pieces of context to answer the question at the end.
        If you don't know the answer, just say that you don't know, don't try to make up an answer.
        Always cite the relevant sections or documents when providing answers.
        
        Context: {context}
        
        Question: {question}
        
        Answer:
        """
        
        prompt_template = custom_prompt or default_prompt
        
        PROMPT = PromptTemplate(
            template=prompt_template,
            input_variables=["context", "question"]
        )
        
        qa_chain = RetrievalQA.from_chain_type(
            llm=self.llm,
            chain_type="stuff",
            retriever=vectorstore.as_retriever(search_kwargs={"k": 5}),
            chain_type_kwargs={"prompt": PROMPT},
            return_source_documents=True
        )
        
        return qa_chain
    
    def ask_question(self, qa_chain, question: str) -> Dict[str, Any]:
        """Ask a question using the QA chain"""
        result = qa_chain({"query": question})
        
        return {
            "answer": result["result"],
            "source_documents": [
                {
                    "content": doc.page_content,
                    "metadata": doc.metadata
                }
                for doc in result["source_documents"]
            ]
        }
    
    def summarize_document(self, text: str) -> str:
        """Summarize a document using the LLM"""
        prompt = f"""
        Please provide a concise summary of the following legal document:
        
        {text}
        
        Summary:
        """
        
        response = self.llm.invoke(prompt)
        return response.content
    
    def extract_key_points(self, text: str) -> List[str]:
        """Extract key points from a legal document"""
        prompt = f"""
        Extract the key points from the following legal document. 
        Return them as a numbered list:
        
        {text}
        
        Key Points:
        """
        
        response = self.llm.invoke(prompt)
        return response.content.split('\n')
    
    def split_text(self, text: str, chunk_size: int = 1000, chunk_overlap: int = 200) -> List[str]:
        """Split text into chunks for processing"""
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap
        )
        
        return text_splitter.split_text(text)
