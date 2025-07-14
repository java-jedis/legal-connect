import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { caseAPI } from "../services/api";

export const useCaseStore = defineStore("case", () => {
  // State
  const cases = ref([]);
  const currentCase = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const totalCases = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);
  const statusFilter = ref(null);
  const sortDirection = ref("DESC");

  // Computed
  const hasMorePages = computed(() => {
    return cases.value.length < totalCases.value;
  });

  const inProgressCases = computed(() => {
    return cases.value.filter((caseItem) => caseItem.status === "IN_PROGRESS");
  });

  const resolvedCases = computed(() => {
    return cases.value.filter((caseItem) => caseItem.status === "RESOLVED");
  });

  // Actions
  const clearError = () => {
    error.value = null;
  };

  const setLoading = (value) => {
    loading.value = value;
  };

  const getAllUserCases = async (
    page = 0,
    size = 10,
    status = null,
    sort = "DESC",
    reset = false
  ) => {
    setLoading(true);
    clearError();

    try {
      const response = await caseAPI.getAllUserCases(page, size, status, sort);

      // Extract data from ApiResponse format: response.data.cases
      const caseList = response.data?.cases || [];

      if (reset || page === 0) {
        cases.value = caseList;
      } else {
        // Append for pagination
        cases.value = [...cases.value, ...caseList];
      }

      totalCases.value = caseList.length; // Backend doesn't return total count, so we use current length
      currentPage.value = page;
      pageSize.value = size;
      statusFilter.value = status;
      sortDirection.value = sort;
    } catch (err) {
      console.error("❌ Error fetching cases:", err);
      console.error("❌ Error response:", err.response);
      console.error("❌ Error response data:", err.response?.data);

      error.value =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Failed to fetch cases";
      console.error("Error fetching cases:", err);
    } finally {
      setLoading(false);
    }
  };

  const getCaseById = async (caseId) => {
    setLoading(true);
    clearError();

    try {
      const response = await caseAPI.getCaseById(caseId);

      // Extract data from ApiResponse format: response.data
      const caseData = response.data;
      if (caseData) {
        currentCase.value = caseData;
        return caseData;
      }
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Failed to fetch case details";
      console.error("Error fetching case:", err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const createCase = async (caseData) => {
    setLoading(true);
    clearError();

    try {
      const response = await caseAPI.createCase(caseData);

      // Extract data from ApiResponse format: response.data
      const newCaseData = response.data;
      if (newCaseData) {
        // Add new case to the beginning of the list
        cases.value.unshift(newCaseData);
        totalCases.value += 1;
        return newCaseData;
      }
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Failed to create case";
      console.error("Error creating case:", err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateCase = async (caseId, updateData) => {
    setLoading(true);
    clearError();

    try {
      const response = await caseAPI.updateCase(caseId, updateData);

      // Extract data from ApiResponse format: response.data
      const updatedCaseData = response.data;
      if (updatedCaseData) {
        // Update case in the list
        const index = cases.value.findIndex((c) => c.caseId === caseId);
        if (index !== -1) {
          cases.value[index] = updatedCaseData;
        }

        // Update current case if it's the one being updated
        if (currentCase.value?.caseId === caseId) {
          currentCase.value = updatedCaseData;
        }

        return updatedCaseData;
      }
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Failed to update case";
      console.error("Error updating case:", err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateCaseStatus = async (caseId, statusData) => {
    setLoading(true);
    clearError();

    try {
      const response = await caseAPI.updateCaseStatus(caseId, statusData);

      // Extract data from ApiResponse format: response.data
      const updatedCaseData = response.data;
      if (updatedCaseData) {
        // Update case in the list
        const index = cases.value.findIndex((c) => c.caseId === caseId);
        if (index !== -1) {
          cases.value[index] = updatedCaseData;
        }

        // Update current case if it's the one being updated
        if (currentCase.value?.caseId === caseId) {
          currentCase.value = updatedCaseData;
        }

        return updatedCaseData;
      }
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        "Failed to update case status";
      console.error("Error updating case status:", err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const loadMoreCases = async () => {
    if (!hasMorePages.value || loading.value) return;

    await getAllUserCases(
      currentPage.value + 1,
      pageSize.value,
      statusFilter.value,
      sortDirection.value,
      false
    );
  };

  const filterCasesByStatus = async (status) => {
    await getAllUserCases(0, pageSize.value, status, sortDirection.value, true);
  };

  const refreshCases = async () => {
    await getAllUserCases(
      0,
      pageSize.value,
      statusFilter.value,
      sortDirection.value,
      true
    );
  };

  const clearCurrentCase = () => {
    currentCase.value = null;
  };

  const clearCases = () => {
    cases.value = [];
    totalCases.value = 0;
    currentPage.value = 0;
    statusFilter.value = null;
  };

  return {
    // State
    cases,
    currentCase,
    loading,
    error,
    totalCases,
    currentPage,
    pageSize,
    statusFilter,
    sortDirection,

    // Computed
    hasMorePages,
    inProgressCases,
    resolvedCases,

    // Actions
    clearError,
    getAllUserCases,
    getCaseById,
    createCase,
    updateCase,
    updateCaseStatus,
    loadMoreCases,
    filterCasesByStatus,
    refreshCases,
    clearCurrentCase,
    clearCases,
  };
});
