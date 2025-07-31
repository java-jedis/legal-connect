import { useAuthStore } from '../stores/auth';
import { useNotificationStore } from '../stores/notification';
import { useChatStore } from '../stores/chat';

export async function initializeApp() {
  const authStore = useAuthStore();

  if (authStore.isLoggedIn) {
    const notificationStore = useNotificationStore();
    const chatStore = useChatStore();

    try {
      await Promise.all([
        notificationStore.initialize(),
        chatStore.initialize(),
      ]);
      console.log('Notification and chat systems initialized successfully.');
    } catch (error) {
      console.error('Failed to initialize app services:', error);
    }
  }
}
