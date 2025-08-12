import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { blogAPI } from "../services/api";
import { useAuthStore } from "./auth";

export const useBlogStore = defineStore("blog", () => {
  // State
  const blogs = ref([]);
  const pagination = ref({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 0,
    sortDirection: "DESC",
  });
  const isLoading = ref(false);
  const error = ref(null);

  const currentBlog = ref(null);
  const subscribers = ref([]);
  const subscriberPage = ref({ page: 0, size: 10, total: 0, totalPages: 0 });

  // Law Insights state
  const subscribedBlogs = ref([]);
  const subscribedBlogsPage = ref({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 0,
    sortDirection: "DESC",
  });
  const searchResults = ref([]);
  const searchPage = ref({ page: 0, size: 10, total: 0, totalPages: 0 });

  // Getters
  const hasBlogs = computed(() => (blogs.value?.length || 0) > 0);
  const hasSubscribedBlogs = computed(
    () => (subscribedBlogs.value?.length || 0) > 0
  );

  // Actions
  const loadMyBlogs = async (page = 0, size = 10, sortDirection = "DESC") => {
    isLoading.value = true;
    error.value = null;
    try {
      const auth = useAuthStore();
      const authorId = auth.getUserInfo()?.id;
      if (!authorId) throw new Error("Not authenticated");

      const response = await blogAPI.getAuthorBlogs(
        authorId,
        page,
        size,
        sortDirection
      );
      blogs.value = response.data?.blogs || [];
      const meta = response.metadata || {};
      pagination.value = {
        page: meta.pageNumber ?? page,
        size: meta.pageSize ?? size,
        total: meta.totalCount ?? blogs.value.length,
        totalPages: meta.totalPages ?? 1,
        sortDirection: meta.sortDirection ?? sortDirection,
      };
      return blogs.value;
    } catch (err) {
      console.error("Error loading blogs:", err);
      error.value =
        err.response?.data?.message || err.message || "Failed to load blogs";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const createBlog = async ({ title, content, status }) => {
    isLoading.value = true;
    error.value = null;
    try {
      const payload = { title, content, status };
      const response = await blogAPI.createBlog(payload);
      const created = response.data;
      // Optimistic add to top
      blogs.value = [created, ...blogs.value];
      return created;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to create blog";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const updateBlog = async (blogId, { title, content, status }) => {
    isLoading.value = true;
    error.value = null;
    try {
      const payload = { title, content, status };
      const response = await blogAPI.updateBlog(blogId, payload);
      const updated = response.data;
      blogs.value = blogs.value.map((b) => (b.blogId === blogId ? updated : b));
      if (currentBlog.value?.blogId === blogId) currentBlog.value = updated;
      return updated;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to update blog";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const deleteBlog = async (blogId) => {
    isLoading.value = true;
    error.value = null;
    try {
      await blogAPI.deleteBlog(blogId);
      blogs.value = blogs.value.filter((b) => b.blogId !== blogId);
      if (currentBlog.value?.blogId === blogId) currentBlog.value = null;
      return true;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to delete blog";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const changeStatus = async (blogId, status) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await blogAPI.changeBlogStatus(blogId, status);
      const updated = response.data;
      blogs.value = blogs.value.map((b) => (b.blogId === blogId ? updated : b));
      if (currentBlog.value?.blogId === blogId) currentBlog.value = updated;
      return updated;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to update status";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const loadSubscribers = async (page = 0, size = 10) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await blogAPI.getSubscribers(page, size);
      subscribers.value = response.data?.subscribers || [];
      const meta = response.metadata || {};
      subscriberPage.value = {
        page: meta.pageNumber ?? page,
        size: meta.pageSize ?? size,
        total: meta.totalCount ?? subscribers.value.length,
        totalPages: meta.totalPages ?? 1,
      };
      return subscribers.value;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to load subscribers";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const getBlogById = async (blogId) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await blogAPI.getBlog(blogId);
      currentBlog.value = response.data;
      return currentBlog.value;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to load blog";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const loadSubscribedBlogs = async (
    page = 0,
    size = 10,
    sortDirection = "DESC"
  ) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await blogAPI.getSubscribedBlogs(
        page,
        size,
        sortDirection
      );
      subscribedBlogs.value = response.data?.blogs || [];
      const meta = response.metadata || {};
      subscribedBlogsPage.value = {
        page: meta.pageNumber ?? page,
        size: meta.pageSize ?? size,
        total: meta.totalCount ?? subscribedBlogs.value.length,
        totalPages: meta.totalPages ?? 1,
        sortDirection: meta.sortDirection ?? sortDirection,
      };
      return subscribedBlogs.value;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to load subscribed blogs";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const subscribeToAuthor = async (authorId) => {
    try {
      const res = await blogAPI.subscribe(authorId);
      return res;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to subscribe";
      throw err;
    }
  };

  const unsubscribeFromAuthor = async (authorId) => {
    try {
      const res = await blogAPI.unsubscribe(authorId);
      return res;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to unsubscribe";
      throw err;
    }
  };

  const searchPublishedBlogs = async (q, page = 0, size = 10) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await blogAPI.searchPublished(q, page, size);
      searchResults.value = response.data?.results || [];
      const meta = response.metadata || {};
      searchPage.value = {
        page: meta.pageNumber ?? page,
        size: meta.pageSize ?? size,
        total: meta.totalCount ?? searchResults.value.length,
        totalPages: meta.totalPages ?? 1,
      };
      return searchResults.value;
    } catch (err) {
      error.value =
        err.response?.data?.error?.message ||
        err.message ||
        "Failed to search blogs";
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const reset = () => {
    blogs.value = [];
    subscribers.value = [];
    subscribedBlogs.value = [];
    searchResults.value = [];
    currentBlog.value = null;
    error.value = null;
  };

  return {
    // state
    blogs,
    pagination,
    isLoading,
    error,
    currentBlog,
    subscribers,
    subscriberPage,
    subscribedBlogs,
    subscribedBlogsPage,
    searchResults,
    searchPage,

    // getters
    hasBlogs,
    hasSubscribedBlogs,

    // actions
    loadMyBlogs,
    createBlog,
    updateBlog,
    deleteBlog,
    changeStatus,
    loadSubscribers,
    getBlogById,
    loadSubscribedBlogs,
    subscribeToAuthor,
    unsubscribeFromAuthor,
    searchPublishedBlogs,
    reset,
  };
});
