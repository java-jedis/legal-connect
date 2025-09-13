#!/usr/bin/env python3
"""
Environment Configuration Validator for Legal Connect RAG System
"""

import os
import re
import json
import socket
import psycopg2
import requests
from urllib.parse import urlparse
from typing import Dict, List, Tuple, Any
from pathlib import Path

class EnvironmentValidator:
    def __init__(self):
        self.errors = []
        self.warnings = []
        self.success = []
        
    def log_error(self, message: str):
        self.errors.append(f"❌ ERROR: {message}")
        
    def log_warning(self, message: str):
        self.warnings.append(f"⚠️  WARNING: {message}")
        
    def log_success(self, message: str):
        self.success.append(f"✅ SUCCESS: {message}")

    def validate_database_connection(self) -> bool:
        """Validate PostgreSQL database connection"""
        try:
            database_url = os.getenv("DATABASE_URL")
            if not database_url:
                self.log_error("DATABASE_URL is not set")
                return False
                
            # Parse database URL
            parsed = urlparse(database_url)
            if parsed.scheme != "postgresql":
                self.log_error(f"Invalid database scheme: {parsed.scheme}, expected 'postgresql'")
                return False
                
            # Test connection
            conn = psycopg2.connect(database_url)
            conn.close()
            self.log_success("PostgreSQL database connection successful")
            return True
            
        except psycopg2.OperationalError as e:
            self.log_error(f"PostgreSQL connection failed: {e}")
            return False
        except Exception as e:
            self.log_error(f"Database validation error: {e}")
            return False

    def validate_qdrant_connection(self) -> bool:
        """Validate Qdrant vector database connection"""
        try:
            # Check for URL-based configuration first
            qdrant_url = os.getenv("QDRANT_URL")
            if qdrant_url:
                api_key = os.getenv("QDRANT_API_KEY")
                headers = {}
                if api_key:
                    headers["api-key"] = api_key
                
                # Extract base URL for health check
                if qdrant_url.endswith('/'):
                    base_url = qdrant_url.rstrip('/')
                else:
                    base_url = qdrant_url
                
                url = f"{base_url}/collections"
                response = requests.get(url, headers=headers, timeout=10)
                
                if response.status_code == 200:
                    self.log_success("Qdrant vector database connection successful (URL-based)")
                    return True
                else:
                    self.log_error(f"Qdrant connection failed with status: {response.status_code}")
                    return False
            else:
                # Fallback to host:port configuration
                host = os.getenv("QDRANT_HOST", "localhost")
                port = int(os.getenv("QDRANT_PORT", "6333"))
                api_key = os.getenv("QDRANT_API_KEY")
                
                # Test connection
                url = f"http://{host}:{port}/collections"
                headers = {}
                if api_key:
                    headers["api-key"] = api_key
                    
                response = requests.get(url, headers=headers, timeout=5)
                
                if response.status_code == 200:
                    self.log_success("Qdrant vector database connection successful (host:port)")
                    return True
                else:
                    self.log_error(f"Qdrant connection failed with status: {response.status_code}")
                    return False
                
        except requests.exceptions.ConnectionError:
            self.log_error("Qdrant connection failed - service not available")
            return False
        except Exception as e:
            self.log_error(f"Qdrant validation error: {e}")
            return False

    def validate_google_ai_api(self) -> bool:
        """Validate Google AI API key"""
        try:
            import google.generativeai as genai
            
            api_key = os.getenv("GOOGLE_API_KEY")
            if not api_key:
                self.log_error("GOOGLE_API_KEY is not set")
                return False
                
            if len(api_key) < 30:
                self.log_error("GOOGLE_API_KEY appears to be invalid (too short)")
                return False
                
            # Test API key
            genai.configure(api_key=api_key)
            
            # Test embedding model
            embedding_model = os.getenv("EMBEDDING_MODEL", "models/embedding-001")
            try:
                result = genai.embed_content(
                    model=embedding_model,
                    content="test",
                    task_type="retrieval_document"
                )
                self.log_success("Google AI API key and embedding model working")
                
                # Test generation model
                gemini_model = os.getenv("GEMINI_MODEL", "gemini-2.5-pro")
                model = genai.GenerativeModel(gemini_model)
                response = model.generate_content("Hello")
                self.log_success("Google AI generation model working")
                return True
                
            except Exception as e:
                self.log_error(f"Google AI model test failed: {e}")
                return False
                
        except ImportError:
            self.log_error("google-generativeai package not installed")
            return False
        except Exception as e:
            self.log_error(f"Google AI validation error: {e}")
            return False

    def validate_redis_connection(self) -> bool:
        """Validate Redis connection"""
        try:
            import redis
            
            redis_url = os.getenv("REDIS_URL", "redis://localhost:6379/0")
            r = redis.from_url(redis_url)
            r.ping()
            self.log_success("Redis connection successful")
            return True
            
        except ImportError:
            self.log_warning("Redis package not installed (optional for caching)")
            return True
        except Exception as e:
            self.log_warning(f"Redis connection failed (optional): {e}")
            return True

    def validate_file_paths(self) -> bool:
        """Validate file paths and permissions"""
        try:
            # Check bdcode_json directory
            json_path = Path("bdcode_json")
            if not json_path.exists():
                self.log_error("bdcode_json directory not found")
                return False
                
            if not json_path.is_dir():
                self.log_error("bdcode_json is not a directory")
                return False
                
            # Count JSON files
            json_files = list(json_path.rglob("*.json"))
            if len(json_files) == 0:
                self.log_warning("No JSON files found in bdcode_json directory")
            else:
                self.log_success(f"Found {len(json_files)} JSON files in bdcode_json")
                
            # Test file readability
            readable_files = 0
            for json_file in json_files[:5]:  # Test first 5 files
                try:
                    with open(json_file, 'r', encoding='utf-8') as f:
                        json.load(f)
                    readable_files += 1
                except Exception as e:
                    self.log_warning(f"Cannot read {json_file}: {e}")
                    
            if readable_files > 0:
                self.log_success(f"Successfully validated {readable_files} JSON files")
                
            return True
            
        except Exception as e:
            self.log_error(f"File path validation error: {e}")
            return False

    def validate_environment_variables(self) -> bool:
        """Validate all required environment variables"""
        required_vars = [
            ("DATABASE_URL", "PostgreSQL database connection"),
            ("GOOGLE_API_KEY", "Google AI API key"),
            ("QDRANT_COLLECTION_NAME", "Qdrant collection name"),
        ]
        
        # Check if using URL or host:port configuration
        qdrant_url = os.getenv("QDRANT_URL")
        if qdrant_url:
            required_vars.append(("QDRANT_URL", "Qdrant database URL"))
        else:
            required_vars.extend([
                ("QDRANT_HOST", "Qdrant database host"),
                ("QDRANT_PORT", "Qdrant database port"),
            ])
        
        optional_vars = [
            ("QDRANT_API_KEY", "Qdrant API key (optional)"),
            ("REDIS_URL", "Redis connection (optional)"),
            ("JWT_SECRET_KEY", "JWT secret key"),
        ]
        
        all_valid = True
        
        # Check required variables
        for var_name, description in required_vars:
            value = os.getenv(var_name)
            if not value:
                self.log_error(f"{var_name} is required but not set ({description})")
                all_valid = False
            else:
                self.log_success(f"{var_name} is set")
                
        # Check optional variables
        for var_name, description in optional_vars:
            value = os.getenv(var_name)
            if not value:
                self.log_warning(f"{var_name} is not set ({description})")
            else:
                self.log_success(f"{var_name} is set")
                
        return all_valid

    def validate_configuration_values(self) -> bool:
        """Validate configuration values make sense"""
        try:
            # Check numeric values
            chunk_size = int(os.getenv("CHUNK_SIZE", "1000"))
            chunk_overlap = int(os.getenv("CHUNK_OVERLAP", "200"))
            top_k = int(os.getenv("TOP_K_RESULTS", "5"))
            similarity_threshold = float(os.getenv("SIMILARITY_THRESHOLD", "0.7"))
            max_context = int(os.getenv("MAX_CONTEXT_LENGTH", "4000"))
            
            if chunk_overlap >= chunk_size:
                self.log_error("CHUNK_OVERLAP should be less than CHUNK_SIZE")
                return False
                
            if not 0.0 <= similarity_threshold <= 1.0:
                self.log_error("SIMILARITY_THRESHOLD should be between 0.0 and 1.0")
                return False
                
            if top_k <= 0 or top_k > 100:
                self.log_warning("TOP_K_RESULTS should be between 1 and 100")
                
            if max_context < chunk_size:
                self.log_warning("MAX_CONTEXT_LENGTH should be larger than CHUNK_SIZE")
                
            # Check CORS origins
            cors_origins = os.getenv("CORS_ORIGINS", '["http://localhost:3000"]')
            try:
                origins = json.loads(cors_origins.replace("'", '"'))
                if not isinstance(origins, list):
                    self.log_error("CORS_ORIGINS should be a JSON array")
                    return False
                self.log_success(f"CORS origins configured for {len(origins)} domains")
            except json.JSONDecodeError:
                self.log_error("CORS_ORIGINS is not valid JSON")
                return False
                
            self.log_success("Configuration values are valid")
            return True
            
        except ValueError as e:
            self.log_error(f"Invalid numeric configuration value: {e}")
            return False
        except Exception as e:
            self.log_error(f"Configuration validation error: {e}")
            return False

    def run_full_validation(self) -> Dict[str, Any]:
        """Run complete environment validation"""
        print("🔍 Starting Legal Connect RAG Environment Validation...\n")
        
        # Load .env file
        env_file = Path(".env")
        if env_file.exists():
            with open(env_file) as f:
                for line in f:
                    if '=' in line and not line.strip().startswith('#'):
                        key, value = line.strip().split('=', 1)
                        os.environ[key] = value
            self.log_success(".env file loaded successfully")
        else:
            self.log_error(".env file not found")
            
        # Run all validations
        validations = [
            ("Environment Variables", self.validate_environment_variables),
            ("Configuration Values", self.validate_configuration_values),
            ("File Paths", self.validate_file_paths),
            ("PostgreSQL Database", self.validate_database_connection),
            ("Qdrant Vector DB", self.validate_qdrant_connection),
            ("Google AI API", self.validate_google_ai_api),
            ("Redis Cache", self.validate_redis_connection),
        ]
        
        results = {}
        for name, validator in validations:
            print(f"\n📋 Validating {name}...")
            try:
                results[name] = validator()
            except Exception as e:
                self.log_error(f"{name} validation failed with exception: {e}")
                results[name] = False
                
        # Print summary
        print("\n" + "="*60)
        print("🎯 VALIDATION SUMMARY")
        print("="*60)
        
        if self.success:
            print("\n✅ SUCCESSFUL VALIDATIONS:")
            for msg in self.success:
                print(f"  {msg}")
                
        if self.warnings:
            print("\n⚠️  WARNINGS:")
            for msg in self.warnings:
                print(f"  {msg}")
                
        if self.errors:
            print("\n❌ ERRORS:")
            for msg in self.errors:
                print(f"  {msg}")
        
        # Overall status
        total_critical = len([r for name, r in results.items() 
                            if not r and name not in ["Redis Cache"]])
        
        print(f"\n📊 OVERALL STATUS:")
        if total_critical == 0:
            print("🎉 All critical validations passed! Your environment is ready.")
        else:
            print(f"🚨 {total_critical} critical validation(s) failed. Please fix errors above.")
            
        return {
            "success": total_critical == 0,
            "results": results,
            "errors": len(self.errors),
            "warnings": len(self.warnings),
            "successes": len(self.success)
        }

if __name__ == "__main__":
    validator = EnvironmentValidator()
    results = validator.run_full_validation()
    
    # Exit with error code if validation failed
    if not results["success"]:
        exit(1)
