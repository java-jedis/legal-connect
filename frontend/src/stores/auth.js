import { defineStore } from "pinia";
import { ref } from "vue";
import { authAPI } from "../services/api";

export const useAuthStore = defineStore("auth", () => {
  // Initialize state from localStorage if available
  const isLoggedIn = ref(localStorage.getItem("auth_isLoggedIn") === "true");
  const userType = ref(localStorage.getItem("auth_userType") || null);
  const userInfo = ref(
    JSON.parse(localStorage.getItem("auth_userInfo") || "null")
  );
  const token = ref(localStorage.getItem("auth_token") || null);

  const login = async (credentials) => {
    try {
      const response = await authAPI.login(credentials);

      // If we reach here, the request was successful
      const authData = response;

      // Store token
      token.value = authData.token;
      localStorage.setItem("auth_token", authData.token);

      // Store user info
      userInfo.value = {
        id: authData.id,
        firstName: authData.firstName,
        lastName: authData.lastName,
        email: authData.email,
        role: authData.role,
        emailVerified: authData.emailVerified,
        createdAt: authData.createdAt,
        updatedAt: authData.updatedAt,
      };

      // Map role to userType for compatibility
      userType.value = authData.role.toLowerCase();

      // Update login status
      isLoggedIn.value = true;

      // Persist to localStorage
      localStorage.setItem("auth_isLoggedIn", "true");
      localStorage.setItem("auth_userType", userType.value);
      localStorage.setItem("auth_userInfo", JSON.stringify(userInfo.value));

      return { success: true, data: authData };
    } catch (error) {
      console.error("Login error:", error);

      // Handle the specific error response structure from backend
      if (error.response?.data?.error?.message) {
        return {
          success: false,
          message: error.response.data.error.message,
        };
      }

      return {
        success: false,
        message:
          error.response?.data?.message || "Login failed. Please try again.",
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await authAPI.register(userData);

      // If we reach here, the request was successful
      const authData = response;

      // Store token
      token.value = authData.token;
      localStorage.setItem("auth_token", authData.token);

      // Store user info
      userInfo.value = {
        id: authData.id,
        firstName: authData.firstName,
        lastName: authData.lastName,
        email: authData.email,
        role: authData.role,
        emailVerified: authData.emailVerified,
        createdAt: authData.createdAt,
        updatedAt: authData.updatedAt,
      };

      // Map role to userType for compatibility
      userType.value = authData.role.toLowerCase();

      // Update login status
      isLoggedIn.value = true;

      // Persist to localStorage
      localStorage.setItem("auth_isLoggedIn", "true");
      localStorage.setItem("auth_userType", userType.value);
      localStorage.setItem("auth_userInfo", JSON.stringify(userInfo.value));

      return { success: true, data: authData };
    } catch (error) {
      console.error("Registration error:", error);

      // Handle the specific error response structure from backend
      if (error.response?.data?.error?.message) {
        return {
          success: false,
          message: error.response.data.error.message,
        };
      }

      return {
        success: false,
        message:
          error.response?.data?.message ||
          "Registration failed. Please try again.",
      };
    }
  };

  const logout = () => {
    isLoggedIn.value = false;
    userType.value = null;
    userInfo.value = null;
    token.value = null;

    // Clear localStorage
    localStorage.removeItem("auth_isLoggedIn");
    localStorage.removeItem("auth_userType");
    localStorage.removeItem("auth_userInfo");
    localStorage.removeItem("auth_token");
  };

  const isUser = () => {
    return isLoggedIn.value && userType.value === "user";
  };

  const isLawyer = () => {
    return isLoggedIn.value && userType.value === "lawyer";
  };

  const isAdmin = () => {
    return isLoggedIn.value && userType.value === "admin";
  };

  const getToken = () => {
    return token.value;
  };

  const getUserInfo = () => {
    return userInfo.value;
  };

  const sendPasswordResetOTP = async (email) => {
    try {
      const response = await authAPI.sendPasswordResetOTP(email);

      // If we reach here, the request was successful
      return {
        success: true,
        message: response.message || "OTP sent successfully",
      };
    } catch (error) {
      console.error("Send OTP error:", error);

      // Handle the specific error response structure from backend
      if (error.response?.data?.error?.message) {
        return {
          success: false,
          message: error.response.data.error.message,
        };
      }

      return {
        success: false,
        message:
          error.response?.data?.message ||
          "Failed to send OTP. Please try again.",
      };
    }
  };

  const resetPassword = async (resetData) => {
    try {
      const response = await authAPI.resetPassword(resetData);

      // If we reach here, the request was successful
      return {
        success: true,
        message: response.message || "Password reset successfully",
      };
    } catch (error) {
      console.error("Reset password error:", error);

      // Handle the specific error response structure from backend
      if (error.response?.data?.error?.message) {
        return {
          success: false,
          message: error.response.data.error.message,
        };
      }

      return {
        success: false,
        message:
          error.response?.data?.message ||
          "Failed to reset password. Please try again.",
      };
    }
  };

  return {
    isLoggedIn,
    userType,
    userInfo,
    token,
    login,
    register,
    logout,
    isUser,
    isLawyer,
    isAdmin,
    getToken,
    getUserInfo,
    sendPasswordResetOTP,
    resetPassword,
  };
});
