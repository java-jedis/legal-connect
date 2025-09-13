<template>
  <div class="admin-dashboard">
    <section class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="welcome-section">
            <h1 class="welcome-title">Admin Dashboard</h1>
            <p class="welcome-subtitle">Manage platform operations and user verifications</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Profile Picture Upload Prompt -->
    <section class="profile-prompt-section section" v-if="showProfilePrompt">
      <div class="container">
        <ProfilePictureUploadPrompt />
      </div>
    </section>

    <section class="dashboard-content section">
      <div class="container">
        <div class="dashboard-grid">
          <div class="dashboard-card clickable-card" @click="navigateToLawyerManagement">
            <div class="card-header">
              <h3>Lawyer Management</h3>
              <button class="btn btn-outline btn-sm">View All</button>
            </div>
            <PendingLawyerVerification />
          </div>

          <div class="dashboard-card">
            <div class="card-header">
              <h3>Feedback Management</h3>
            </div>
            <FeedbackManagement />
          </div>

          <div class="dashboard-card">
            <div class="card-header">
              <h3>Complaints Management</h3>
            </div>
            <ComplaintsManagement />
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import ComplaintsManagement from '@/components/admin/ComplaintsManagement.vue';
import FeedbackManagement from '@/components/admin/FeedbackManagement.vue';
import PendingLawyerVerification from '@/components/admin/PendingLawyerVerification.vue';
import ProfilePictureUploadPrompt from '@/components/user/ProfilePictureUploadPrompt.vue';
import { useAuthStore } from '@/stores/auth';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()
const authStore = useAuthStore()
const showProfilePrompt = ref(false)

// Check if profile picture prompt should be shown
const checkProfilePrompt = () => {
  // Don't show if user already has a profile picture
  if (authStore.userInfo?.profilePicture?.fullPictureUrl) {
    showProfilePrompt.value = false
    return
  }
  
  // Show the prompt for users without profile pictures
  showProfilePrompt.value = true
}

const dismissProfilePrompt = () => {
  showProfilePrompt.value = false
}

// Check on component mount
onMounted(() => {
  checkProfilePrompt()
})

const navigateToLawyerManagement = () => {
  router.push('/admin/lawyers')
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: var(--color-background);
}

.dashboard-header {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  padding: 2rem 0;
}

.profile-prompt-section {
  background: var(--color-background-soft);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.welcome-subtitle {
  font-size: 1.125rem;
  opacity: 0.9;
}

.dashboard-content {
  background: var(--color-background);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.dashboard-card {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background-soft);
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-heading);
}

.clickable-card {
  cursor: pointer;
  transition: all var(--transition-normal);
}

.clickable-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary);
}
</style> 