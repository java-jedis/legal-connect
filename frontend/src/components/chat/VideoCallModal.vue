<template>
  <div class="video-call-overlay">
    <div class="video-call-container">
      <div class="video-call-header">
        <div class="meeting-info">
          <h3>{{ meetingTitle }}</h3>
          <p>{{ participantInfo }}</p>
        </div>
        <button @click="endCall" class="end-call-btn">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            width="20"
            height="20"
          >
            <line
              x1="18"
              y1="6"
              x2="6"
              y2="18"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <line
              x1="6"
              y1="6"
              x2="18"
              y2="18"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          End Call
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Connecting to meeting...</p>
      </div>

      <!-- Error State -->
      <div v-if="error" class="error-container">
        <div class="error-icon">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="2"
            />
            <line
              x1="15"
              y1="9"
              x2="9"
              y2="15"
              stroke="currentColor"
              stroke-width="2"
            />
            <line
              x1="9"
              y1="9"
              x2="15"
              y2="15"
              stroke="currentColor"
              stroke-width="2"
            />
          </svg>
        </div>
        <h4>Failed to join meeting</h4>
        <p>{{ error }}</p>
        <button @click="retryConnection" class="btn btn-primary">
          Try Again
        </button>
      </div>

      <!-- Jitsi Meet Container (always present but hidden when loading/error) -->
      <div
        id="jitsi-container"
        class="jitsi-container"
        :style="{ display: isLoading || error ? 'none' : 'flex' }"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from "vue";
import { useAuthStore } from "../../stores/auth";

const props = defineProps({
  meeting: {
    type: Object,
    required: true,
  },
  roomName: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["close"]);

const authStore = useAuthStore();
const isLoading = ref(true);
const error = ref("");
let jitsiApi = null;

// Helper methods (declared first to avoid hoisting issues)
const getClientName = (client) => {
  if (!client) return "Unknown Client";
  return (
    `${client.firstName || ""} ${client.lastName || ""}`.trim() ||
    client.email ||
    "Unknown Client"
  );
};

// Computed properties for display
const meetingTitle = ref(props.meeting.roomName || "Video Consultation");
const participantInfo = ref(
  `Meeting with ${getClientName(props.meeting.client)}`
);

const getUserDisplayName = () => {
  const userInfo = authStore.userInfo;
  if (userInfo?.firstName && userInfo?.lastName) {
    return `${userInfo.firstName} ${userInfo.lastName}`;
  }
  return userInfo?.firstName || userInfo?.email || "User";
};

const getUserEmail = () => {
  return authStore.userInfo?.email || "user@example.com";
};

const loadJitsiScript = () => {
  return new Promise((resolve, reject) => {
    // Check if script is already loaded
    if (window.JitsiMeetExternalAPI) {
      resolve();
      return;
    }

    const script = document.createElement("script");
    script.src = `https://8x8.vc/${import.meta.env.VITE_JITSI_APP_ID}/external_api.js`;
    script.async = true;

    script.onload = () => {
      // Wait a bit for the API to be fully available
      setTimeout(resolve, 100);
    };

    script.onerror = () => {
      reject(new Error("Failed to load Jitsi Meet API"));
    };

    document.head.appendChild(script);
  });
};

const initializeJitsiMeet = async () => {
  try {
    isLoading.value = true;
    error.value = "";

    // Wait for DOM to be ready
    await nextTick();

    // Load Jitsi script
    await loadJitsiScript();

    // Wait for the DOM element to be available
    const waitForElement = () => {
      return new Promise((resolve, reject) => {
        const maxAttempts = 50; // 5 seconds max
        let attempts = 0;

        const checkElement = () => {
          const element = document.querySelector("#jitsi-container");
          if (element) {
            resolve(element);
          } else if (attempts < maxAttempts) {
            attempts++;
            setTimeout(checkElement, 100);
          } else {
            reject(new Error("Jitsi container element not found"));
          }
        };

        checkElement();
      });
    };

    const containerElement = await waitForElement();

    // Initialize Jitsi Meet
    const domain = "8x8.vc";
    const options = {
      roomName: `${import.meta.env.VITE_JITSI_APP_ID}/${props.roomName}`,
      parentNode: containerElement,
      userInfo: {
        displayName: getUserDisplayName(),
        email: getUserEmail(),
      },
      configOverwrite: {
        startWithAudioMuted: false,
        startWithVideoMuted: false,
        enableWelcomePage: false,
        prejoinPageEnabled: false,
        disableModeratorIndicator: false,
        startScreenSharing: false,
        enableEmailInStats: false,
      },
      interfaceConfigOverwrite: {
        DISABLE_JOIN_LEAVE_NOTIFICATIONS: false,
        DISABLE_PRESENCE_STATUS: false,
        DISPLAY_WELCOME_PAGE_CONTENT: false,
        ENABLE_FEEDBACK_ANIMATION: false,
        FILMSTRIP_ENABLED: true,
        GENERATE_ROOMNAMES_ON_WELCOME_PAGE: false,
        HIDE_INVITE_MORE_HEADER: false,
        JITSI_WATERMARK_LINK: "https://jitsi.org",
        LANG_DETECTION: true,
        LIVE_STREAMING_ENABLED: false,
        MOBILE_APP_PROMO: false,
        RECENT_LIST_ENABLED: true,
        REMOTE_VIDEO_MENU_BUTTON_ENABLED: true,
        SETTINGS_SECTIONS: [
          "devices",
          "language",
          "moderator",
          "profile",
          "calendar",
        ],
        SHOW_BRAND_WATERMARK: false,
        SHOW_JITSI_WATERMARK: false,
        SHOW_POWERED_BY: false,
        SHOW_PROMOTIONAL_CLOSE_PAGE: false,
        SHOW_WATERMARK_FOR_GUESTS: false,
        TOOLBAR_BUTTONS: [
          "microphone",
          "camera",
          "closedcaptions",
          "desktop",
          "embedmeeting",
          "fullscreen",
          "fodeviceselection",
          "hangup",
          "profile",
          "chat",
          "recording",
          "livestreaming",
          "etherpad",
          "sharedvideo",
          "settings",
          "raisehand",
          "videoquality",
          "filmstrip",
          "invite",
          "feedback",
          "stats",
          "shortcuts",
          "tileview",
          "videobackgroundblur",
          "download",
          "help",
          "mute-everyone",
          "security",
        ],
        TOOLBAR_TIMEOUT: 4000,
        UNSUPPORTED_BROWSER: false,
        VIDEO_LAYOUT_FIT: "both",
      },
    };

    jitsiApi = new window.JitsiMeetExternalAPI(domain, options);

    // Add event listeners
    jitsiApi.addEventListeners({
      readyToClose: () => {
        endCall();
      },
      participantLeft: (participant) => {
        console.log("Participant left:", participant);
      },
      participantJoined: (participant) => {
        console.log("Participant joined:", participant);
      },
      videoConferenceJoined: (participant) => {
        console.log("Conference joined:", participant);
        isLoading.value = false;
      },
      videoConferenceLeft: () => {
        console.log("Conference left");
        endCall();
      },
    });
  } catch (err) {
    console.error("Error initializing Jitsi Meet:", err);
    error.value = err.message || "Failed to initialize video call";
    isLoading.value = false;
  }
};

const retryConnection = () => {
  initializeJitsiMeet();
};

const endCall = () => {
  if (jitsiApi) {
    jitsiApi.dispose();
    jitsiApi = null;
  }
  emit("close");
};

// Lifecycle
onMounted(() => {
  initializeJitsiMeet();
});

onUnmounted(() => {
  if (jitsiApi) {
    jitsiApi.dispose();
    jitsiApi = null;
  }
});
</script>

<style scoped>
.video-call-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.video-call-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #000;
}

.video-call-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  z-index: 10000;
}

.meeting-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.125rem;
  font-weight: 600;
}

.meeting-info p {
  margin: 0;
  font-size: 0.875rem;
  opacity: 0.8;
}

.end-call-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.end-call-btn:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

.jitsi-container {
  flex: 1;
  width: 100%;
  height: 100%;
}

.loading-container,
.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
  padding: 2rem;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top: 4px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-container .error-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 1rem;
  color: #dc2626;
}

.error-container h4 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.error-container p {
  margin: 0 0 1.5rem 0;
  opacity: 0.8;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.5rem;
  border: 1px solid transparent;
  border-radius: var(--border-radius);
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.btn-primary:hover {
  background-color: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
  transform: translateY(-1px);
}

/* Ensure Jitsi Meet iframe takes full space */
:deep(iframe) {
  width: 100% !important;
  height: 100% !important;
  border: none !important;
}

/* Hide Jitsi watermark and branding if needed */
:deep(.watermark) {
  display: none !important;
}

:deep(.poweredby) {
  display: none !important;
}
</style>
