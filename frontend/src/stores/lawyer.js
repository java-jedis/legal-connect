import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { lawyerAPI } from "../services/api";

export const useLawyerStore = defineStore("lawyer", () => {
  const lawyerInfo = ref(null);
  const isLoading = ref(false);
  const error = ref(null);

  // Computed properties
  const hasProfile = computed(() => {
    if (!lawyerInfo.value) return false;

    // Check if all fields are null/empty (indicating no profile)
    const data = lawyerInfo.value;
    const hasNoProfile =
      !data.firm &&
      !data.yearsOfExperience &&
      !data.barCertificateNumber &&
      !data.practicingCourt &&
      !data.division &&
      !data.district &&
      !data.bio &&
      !data.verificationStatus &&
      (!data.specializations || data.specializations.length === 0);

    return !hasNoProfile;
  });

  const isVerified = computed(() => {
    return (
      hasProfile.value && lawyerInfo.value?.verificationStatus === "APPROVED"
    );
  });

  const isPending = computed(() => {
    return (
      hasProfile.value && lawyerInfo.value?.verificationStatus === "PENDING"
    );
  });

  const isRejected = computed(() => {
    return (
      hasProfile.value && lawyerInfo.value?.verificationStatus === "REJECTED"
    );
  });

  const canAccessDashboard = computed(() => {
    return hasProfile.value && isVerified.value;
  });

  // Actions
  const fetchLawyerInfo = async () => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await lawyerAPI.getLawyerInfo();

      // Check if the response indicates no profile exists
      if (
        response.message &&
        response.message.includes("Lawyer profile has not been created")
      ) {
        lawyerInfo.value = null;
        return null;
      }

      // Check if all fields are null/empty (indicating no profile)
      const data = response.data;
      const hasNoProfile =
        !data.firm &&
        !data.yearsOfExperience &&
        !data.barCertificateNumber &&
        !data.practicingCourt &&
        !data.division &&
        !data.district &&
        !data.bio &&
        !data.verificationStatus &&
        (!data.specializations || data.specializations.length === 0);

      if (hasNoProfile) {
        lawyerInfo.value = null;
        return null;
      }

      lawyerInfo.value = response.data;
      return response.data;
    } catch (err) {
      console.error("Error fetching lawyer info:", err);

      // If the error indicates no profile exists, set lawyerInfo to null
      if (
        err.response?.status === 404 ||
        err.response?.data?.message?.includes(
          "Lawyer profile has not been created"
        )
      ) {
        lawyerInfo.value = null;
        return null;
      }

      error.value =
        err.response?.data?.message || "Failed to fetch lawyer information";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const createLawyerProfile = async (profileData) => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await lawyerAPI.createLawyerProfile(profileData);
      lawyerInfo.value = response.data;
      return response.data;
    } catch (err) {
      console.error("Error creating lawyer profile:", err);
      error.value =
        err.response?.data?.message || "Failed to create lawyer profile";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const updateLawyerProfile = async (profileData) => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await lawyerAPI.updateLawyerProfile(profileData);
      lawyerInfo.value = response.data;
      return response.data;
    } catch (err) {
      console.error("Error updating lawyer profile:", err);
      error.value =
        err.response?.data?.message || "Failed to update lawyer profile";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const uploadCredentials = async (file) => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await lawyerAPI.uploadCredentials(file);
      return response.data;
    } catch (err) {
      console.error("Error uploading credentials:", err);
      error.value =
        err.response?.data?.message || "Failed to upload credentials";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const viewCredentials = async () => {
    isLoading.value = true;
    error.value = null;

    try {
      const response = await lawyerAPI.viewCredentials();
      return response;
    } catch (err) {
      console.error("Error viewing credentials:", err);
      error.value = err.response?.data?.message || "Failed to view credentials";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const clearError = () => {
    error.value = null;
  };

  const reset = () => {
    lawyerInfo.value = null;
    isLoading.value = false;
    error.value = null;
  };

  return {
    // State
    lawyerInfo,
    isLoading,
    error,

    // Computed
    hasProfile,
    isVerified,
    isPending,
    isRejected,
    canAccessDashboard,

    // Actions
    fetchLawyerInfo,
    createLawyerProfile,
    updateLawyerProfile,
    uploadCredentials,
    viewCredentials,
    clearError,
    reset,
  };
});
