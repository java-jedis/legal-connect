/**
 * Utility functions for handling API errors
 */

export const handleApiError = (error) => {
  console.error("API Error:", error);

  // Handle different types of errors
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;

    switch (status) {
      case 400:
        return data?.message || "Invalid request. Please check your input.";
      case 401:
        return "Authentication failed. Please login again.";
      case 403:
        return "Access denied. You do not have permission to perform this action.";
      case 404:
        return "Resource not found.";
      case 409:
        return data?.message || "Resource already exists.";
      case 422:
        return data?.message || "Validation failed. Please check your input.";
      case 500:
        return "Server error. Please try again later.";
      default:
        return data?.message || "An unexpected error occurred.";
    }
  } else if (error.request) {
    // Network error
    return "Network error. Please check your connection and try again.";
  } else {
    // Other error
    return error.message || "An unexpected error occurred.";
  }
};

export const showErrorAlert = (error) => {
  const message = handleApiError(error);
  alert(message);
};

export const showSuccessAlert = (message) => {
  alert(message);
};
