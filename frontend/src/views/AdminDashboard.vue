<template>
  <div class="admin-dashboard">
    <h1>Admin Dashboard</h1>
    <section class="pending-lawyers">
      <h2>Lawyer Management</h2>
      <PendingLawyerVerification />
    </section>
    <section class="feedback-section">
      <h2>Feedback Management</h2>
      <FeedbackManagement />
    </section>
    <section class="complaints-section">
      <h2>Complaints Management</h2>
      <ComplaintsManagement />
    </section>
  </div>
</template>

<script setup>
import ComplaintsManagement from '../components/ComplaintsManagement.vue';
import FeedbackManagement from '../components/FeedbackManagement.vue';
import PendingLawyerVerification from '../components/PendingLawyerVerification.vue';
</script>

<style scoped>
.admin-dashboard {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
}
section {
  margin-bottom: 2.5rem;
  background: #fafbfc;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
h1 {
  margin-bottom: 2rem;
  font-size: 2.2rem;
  font-weight: 700;
}
h2 {
  margin-bottom: 1rem;
  font-size: 1.3rem;
  font-weight: 600;
}
.placeholder {
  color: #888;
  font-style: italic;
  padding: 1rem 0;
}
</style> 