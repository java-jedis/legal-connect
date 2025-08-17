<template>
  <div class="feedback-management">
    <div class="placeholder-content">
      <div class="icon">📝</div>
      <h3>Feedback Management</h3>
      <p>This section will allow admins to view and manage user feedback and reviews.</p>
      <div class="features">
        <div class="feature">
          <span class="feature-icon">👥</span>
          <span>View all user feedback</span>
        </div>
        <div class="feature">
          <span class="feature-icon">⭐</span>
          <span>Manage ratings and reviews</span>
        </div>
        <div class="feature">
          <span class="feature-icon">📊</span>
          <span>Analytics and reports</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Future: Feedback management functionality will be implemented here
</script>

<style scoped>
.feedback-management {
  width: 100%;
}

.placeholder-content {
  text-align: center;
  padding: 3rem 2rem;
  color: #666;
}

.icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.placeholder-content h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.3rem;
}

.placeholder-content p {
  margin-bottom: 2rem;
  font-size: 1rem;
  line-height: 1.5;
}

.features {
  display: flex;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.feature {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 0.9rem;
}

.feature-icon {
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .features {
    flex-direction: column;
    align-items: center;
  }
  
  .feature {
    width: 100%;
    max-width: 250px;
  }
}
</style> 