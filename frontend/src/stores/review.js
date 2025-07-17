import { defineStore } from "pinia";
import { ref } from "vue";
import api from "../services/api";

export const useReviewStore = defineStore("review", () => {
  // State: reviews mapped by caseId
  const reviewsByCaseId = ref({});
  const loading = ref({});
  const error = ref({});

  // Fetch review for a case
  async function fetchReview(caseId) {
    loading.value[caseId] = true;
    error.value[caseId] = null;
    try {
      const res = await api.get(`/lawyer-directory/reviews/${caseId}`);
      if (res.data && res.data.data) {
        reviewsByCaseId.value[caseId] = res.data.data;
      } else {
        reviewsByCaseId.value[caseId] = null;
      }
    } catch (err) {
      // If 404, treat as no review
      if (err.response && err.response.status === 404) {
        reviewsByCaseId.value[caseId] = null;
      } else {
        error.value[caseId] =
          err.response?.data?.error?.message || "Failed to fetch review";
      }
    } finally {
      loading.value[caseId] = false;
    }
  }

  // Add review
  async function addReview(caseId, reviewData) {
    loading.value[caseId] = true;
    error.value[caseId] = null;
    try {
      const res = await api.post("/lawyer-directory/reviews", {
        ...reviewData,
        caseId,
      });
      reviewsByCaseId.value[caseId] = res.data.data;
      return res.data.data;
    } catch (err) {
      error.value[caseId] =
        err.response?.data?.error?.message || "Failed to add review";
      throw err;
    } finally {
      loading.value[caseId] = false;
    }
  }

  // Update review
  async function updateReview(caseId, reviewId, reviewData) {
    loading.value[caseId] = true;
    error.value[caseId] = null;
    try {
      const res = await api.put(
        `/lawyer-directory/reviews/${reviewId}`,
        reviewData
      );
      reviewsByCaseId.value[caseId] = res.data.data;
      return res.data.data;
    } catch (err) {
      error.value[caseId] =
        err.response?.data?.error?.message || "Failed to update review";
      throw err;
    } finally {
      loading.value[caseId] = false;
    }
  }

  // Delete review
  async function deleteReview(caseId, reviewId) {
    loading.value[caseId] = true;
    error.value[caseId] = null;
    try {
      await api.delete(`/lawyer-directory/reviews/${reviewId}`);
      reviewsByCaseId.value[caseId] = null;
    } catch (err) {
      error.value[caseId] =
        err.response?.data?.error?.message || "Failed to delete review";
      throw err;
    } finally {
      loading.value[caseId] = false;
    }
  }

  return {
    reviewsByCaseId,
    loading,
    error,
    fetchReview,
    addReview,
    updateReview,
    deleteReview,
  };
});
