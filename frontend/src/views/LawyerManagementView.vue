<template>
  <div class="lawyer-management">
    <section class="dashboard-header">
      <div class="container">
        <div class="header-content">
          <div class="welcome-section">
            <h1 class="welcome-title">Lawyer Management</h1>
            <p class="welcome-subtitle">Manage lawyer verification and platform access</p>
          </div>
        </div>
      </div>
    </section>

    <section class="management-content section">
      <div class="container">
        <div class="management-card">
          <PendingLawyerVerification />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import PendingLawyerVerification from '../components/PendingLawyerVerification.vue';
</script>

<style scoped>
.lawyer-management {
  min-height: 100vh;
  background: var(--color-background);
}

.dashboard-header {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: var(--color-background);
  padding: 2rem 0;
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

.management-content {
  background: var(--color-background);
}

.management-card {
  background: var(--color-background);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  padding: 1.5rem;
}

@media (max-width: 768px) {
  .management-card {
    padding: 1rem;
    margin: 0 -1rem;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }
}
</style> 