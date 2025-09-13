import { useAuthStore } from "../stores/auth";
import { useChatStore } from "../stores/chat";
import { useNotificationStore } from "../stores/notification";

export async function initializeApp() {
  const authStore = useAuthStore();

  if (authStore.isLoggedIn) {
    const notificationStore = useNotificationStore();
    const chatStore = useChatStore();

    try {
      // Fetch user info if user doesn't have profile picture data
      if (!authStore.userInfo?.profilePicture) {
        await authStore.fetchUserInfo();
      }

      await Promise.all([
        notificationStore.initialize(),
        chatStore.initialize(),
      ]);
      console.log("Notification and chat systems initialized successfully.");
    } catch (error) {
      console.error("Failed to initialize app services:", error);
    }
  }
}
