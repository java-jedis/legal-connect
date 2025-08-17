import { defineStore } from "pinia";
import { ref } from "vue";
import { authAPI } from "../services/api";
import { useNotificationStore } from "./notification";
import { initializeApp } from '../services/init';

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
      const authData = response.data; // Access the data field from ApiResponse

      // Validate that we received valid auth data
      if (!authData || !authData.token || !authData.role) {
        throw new Error("Invalid authentication response from server");
      }

      // Store token - only if it's valid
      if (
        authData.token &&
        authData.token.trim() !== "" &&
        authData.token !== "null" &&
        authData.token !== "undefined"
      ) {
        token.value = authData.token;
        localStorage.setItem("auth_token", authData.token);
      } else {
        console.error("Invalid token received from server:", authData.token);
        throw new Error("Invalid authentication token received");
      }

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

      // Map role to userType for compatibility - handle both string and enum cases
      const roleString =
        typeof authData.role === "string"
          ? authData.role
          : authData.role?.name || "USER";
      userType.value = roleString.toLowerCase();

      // Update login status
      isLoggedIn.value = true;

      // Persist to localStorage
      localStorage.setItem("auth_isLoggedIn", "true");
      localStorage.setItem("auth_userType", userType.value);
      localStorage.setItem("auth_userInfo", JSON.stringify(userInfo.value));

      await initializeApp();

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
      const authData = response.data; // Access the data field from ApiResponse

      // Validate that we received valid auth data
      if (!authData || !authData.token || !authData.role) {
        throw new Error("Invalid authentication response from server");
      }

      // Store token - only if it's valid
      if (
        authData.token &&
        authData.token.trim() !== "" &&
        authData.token !== "null" &&
        authData.token !== "undefined"
      ) {
        token.value = authData.token;
        localStorage.setItem("auth_token", authData.token);
      } else {
        console.error("Invalid token received from server:", authData.token);
        throw new Error("Invalid authentication token received");
      }

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

      // Map role to userType for compatibility - handle both string and enum cases
      const roleString =
        typeof authData.role === "string"
          ? authData.role
          : authData.role?.name || "USER";
      userType.value = roleString.toLowerCase();

      // Update login status
      isLoggedIn.value = true;

      // Persist to localStorage
      localStorage.setItem("auth_isLoggedIn", "true");
      localStorage.setItem("auth_userType", userType.value);
      localStorage.setItem("auth_userInfo", JSON.stringify(userInfo.value));
      
      // Initialize notification system and connect WebSocket
      try {
        const notificationStore = useNotificationStore();
        await notificationStore.initialize();
      } catch (wsError) {
        console.warn("Failed to initialize notification system:", wsError);
        // Don't fail registration if WebSocket connection fails
      }

      // Initialize chat system
      try {
        const { useChatStore } = await import("./chat");
        const chatStore = useChatStore();
        await chatStore.initialize();
      } catch (chatError) {
        console.warn("Failed to initialize chat system:", chatError);
        // Don't fail registration if chat initialization fails
      }

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

  const logout = async () => {
    try {
      // Clean up notification store and disconnect WebSocket before logout
      try {
        const notificationStore = useNotificationStore();
        await notificationStore.cleanup();
      } catch (wsError) {
        console.warn("Error cleaning up notification system:", wsError);
        // Continue with logout even if WebSocket cleanup fails
      }

      // Clean up chat system and disconnect WebSocket before logout
      try {
        const { useChatStore } = await import("./chat");
        const chatStore = useChatStore();
        await chatStore.cleanup();
      } catch (chatError) {
        console.warn("Error cleaning up chat system:", chatError);
        // Continue with logout even if chat cleanup fails
      }
      
      await authAPI.logout();
    } catch {
      // Ignore errors, always clear local state
    }
    isLoggedIn.value = false;
    userType.value = null;
    userInfo.value = null;
    token.value = null;
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
        message:
          response.data?.message || response.message || "OTP sent successfully",
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
        message:
          response.data?.message ||
          response.message ||
          "Password reset successfully",
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

  const sendVerificationCode = async (email) => {
    try {
      const response = await authAPI.sendVerificationCode(email);

      // If we reach here, the request was successful
      return {
        success: true,
        message:
          response.data?.message ||
          response.message ||
          "Verification code sent successfully",
      };
    } catch (error) {
      console.error("Send verification code error:", error);

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
          "Failed to send verification code. Please try again.",
      };
    }
  };

  const verifyEmail = async (verificationData) => {
    try {
      const response = await authAPI.verifyEmail(verificationData);

      // If we reach here, the request was successful
      return {
        success: true,
        message:
          response.data?.message ||
          response.message ||
          "Email verified successfully",
      };
    } catch (error) {
      console.error("Email verification error:", error);

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
          "Failed to verify email. Please try again.",
      };
    }
  };

  const changePassword = async (data) => {
    try {
      const response = await authAPI.changePassword(data);
      // Success if response.status is 200 and response.data is true
      if (response.status === 200 && response.data === true) {
        return {
          success: true,
          message: response.message || "Password changed successfully.",
        };
      } else {
        return {
          success: false,
          message:
            response?.error?.message ||
            response.message ||
            "Failed to change password.",
        };
      }
    } catch (error) {
      if (error.response?.data?.error?.message) {
        return { success: false, message: error.response.data.error.message };
      }
      return {
        success: false,
        message: error.response?.data?.message || "Failed to change password.",
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
    sendVerificationCode,
    verifyEmail,
    changePassword,
  };
});
