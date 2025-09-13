import os
import google.generativeai as genai
from dotenv import load_dotenv

def test_gemini_api():
    """
    Tests the Gemini API with the configured model and API key.
    """
    try:
        # Load environment variables from .env file
        load_dotenv()

        # Get API key and model from environment variables
        api_key = os.getenv("GOOGLE_API_KEY")
        model_name = os.getenv("GEMINI_MODEL")

        if not api_key:
            print("Error: GOOGLE_API_KEY not found in .env file.")
            return

        if not model_name:
            print("Error: GEMINI_MODEL not found in .env file.")
            return

        # Configure the generative AI library
        genai.configure(api_key=api_key)

        # Create the generative model instance
        model = genai.GenerativeModel(model_name)

        # Define a sample prompt
        prompt = "Hello, Gemini! Please tell me a fun fact about programming."

        print(f"Testing Gemini model: {model_name}")
        print(f"Prompt: {prompt}")

        # Generate content
        response = model.generate_content(prompt)

        # Print the response
        print("\nResponse from Gemini:")
        print(response.text)

    except Exception as e:
        print(f"\nAn error occurred: {e}")

if __name__ == "__main__":
    test_gemini_api()
