import { defineStore } from "pinia";
import { ref } from "vue";

export const useAdminStore = defineStore("admin", () => {
  // Example admin state
  const selectedTab = ref("pending-lawyers");
  const lastFetchTime = ref(null);

  // Example actions
  function setTab(tab) {
    selectedTab.value = tab;
  }
  function updateFetchTime(time) {
    lastFetchTime.value = time;
  }

  return {
    selectedTab,
    lastFetchTime,
    setTab,
    updateFetchTime,
  };
});
