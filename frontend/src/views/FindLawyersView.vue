<template>
  <div class="find-lawyers-view">
    <div class="container">
      <div class="section-header">
        <h1 class="page-title">Find a Lawyer</h1>
      </div>

      <form @submit.prevent="searchLawyers" class="filters-form">
        <div class="filters-grid">
          <div class="form-group">
            <label for="minExperience">Minimum Experience</label>
            <input
              id="minExperience"
              type="number"
              v-model.number="filters.minExperience"
              min="0"
              max="50"
              class="form-input"
              placeholder="Min years"
            />
          </div>
          <div class="form-group">
            <label for="maxExperience">Maximum Experience</label>
            <input
              id="maxExperience"
              type="number"
              v-model.number="filters.maxExperience"
              min="0"
              max="50"
              class="form-input"
              placeholder="Max years"
            />
          </div>
          <div class="form-group">
            <label for="practicingCourt">Practicing Court</label>
            <select
              id="practicingCourt"
              v-model="filters.practicingCourt"
              class="form-select"
            >
              <option value="">Any Court</option>
              <option
                v-for="court in practicingCourts"
                :key="court.value"
                :value="court.value"
              >
                {{ court.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="division">Division</label>
            <select
              id="division"
              v-model="filters.division"
              class="form-select"
            >
              <option value="">Any Division</option>
              <option
                v-for="division in divisions"
                :key="division.value"
                :value="division.value"
              >
                {{ division.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="district">District</label>
            <select
              id="district"
              v-model="filters.district"
              class="form-select"
            >
              <option value="">Any District</option>
              <option
                v-for="district in districts"
                :key="district.value"
                :value="district.value"
              >
                {{ district.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="specialization">Specialization</label>
            <select
              id="specialization"
              v-model="filters.specialization"
              class="form-select"
            >
              <option value="">Any Specialization</option>
              <option
                v-for="spec in specializations"
                :key="spec.value"
                :value="spec.value"
              >
                {{ spec.label }}
              </option>
            </select>
          </div>
          <div class="form-group search-button-group">
            <label>&nbsp;</label>
            <button type="submit" class="search-button" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? "Searching..." : "Search Lawyers" }}
            </button>
          </div>
        </div>
      </form>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <div v-if="lawyers.length > 0" class="results-section">
        <div class="results-header">
          <h2>
            Found {{ lawyers.length }} lawyer{{
              lawyers.length !== 1 ? "s" : ""
            }}
          </h2>
        </div>
        <div class="lawyers-grid">
          <div
            v-for="lawyer in lawyers"
            :key="lawyer.lawyerId"
            class="lawyer-card"
          >
            <div class="lawyer-header">
              <div class="lawyer-avatar">{{ getInitial(lawyer) }}</div>
              <div class="lawyer-info">
                <h3 class="lawyer-name">
                  {{ lawyer.firstName }} {{ lawyer.lastName }}
                </h3>
                <p class="lawyer-firm">
                  {{ lawyer.firm || "Independent Practice" }}
                </p>
              </div>
              <button 
                @click="viewLawyerDetails(lawyer.email)"
                class="view-details-button"
                type="button"
              >
                View Details
              </button>
            </div>

            <div class="lawyer-details">
              <div class="detail-item">
                <span class="detail-label">Experience</span>
                <span class="detail-value"
                  >{{ lawyer.yearsOfExperience || 0 }} years</span
                >
              </div>
              <div class="detail-item">
                <span class="detail-label">Court</span>
                <span class="detail-value">
                  {{
                    practicingCourts.find(
                      (c) => c.value === lawyer.practicingCourt
                    )?.label || lawyer.practicingCourt
                  }}
                </span>
              </div>
              <div class="detail-item">
                <span class="detail-label">Location</span>
                <span class="detail-value">
                  {{
                    divisions.find((d) => d.value === lawyer.division)?.label ||
                    lawyer.division
                  }}
                </span>
              </div>
            </div>

            <div class="specializations">
              <h4 class="specializations-title">Specializations</h4>
              <div class="specializations-list">
                <template v-if="lawyer.specializationDisplayNames.length > 0">
                  <span
                    v-for="(spec, index) in lawyer.specializationDisplayNames"
                    :key="index"
                    class="specialization-tag"
                  >
                    {{ spec }}
                  </span>
                </template>
                <span v-else class="no-specializations">General Practice</span>
              </div>
            </div>


          </div>
        </div>
      </div>

      <div v-else-if="!loading && !error && searched" class="no-results">
        <h3>No lawyers found</h3>
        <p>Try adjusting your search criteria to find more results.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useLawyerSearchStore } from "../stores/lawyerSearch";

const route = useRoute();
const router = useRouter();
const lawyerSearchStore = useLawyerSearchStore();

// Use store state
const loading = computed(() => lawyerSearchStore.loading);
const error = computed(() => lawyerSearchStore.error);
const lawyers = computed(() => lawyerSearchStore.lawyers);
const searched = computed(() => lawyerSearchStore.searched);
const filters = computed(() => lawyerSearchStore.filters);

// Use store constants
const practicingCourts = lawyerSearchStore.practicingCourts;
const divisions = lawyerSearchStore.divisions;
const districts = lawyerSearchStore.districts;
const specializations = lawyerSearchStore.specializations;

// Watch for filter changes and update store
watch(filters, (newFilters) => {
  lawyerSearchStore.setFilters(newFilters);
}, { deep: true });

onMounted(() => {
  // Check if we have search results in the store
  if (lawyerSearchStore.hasResults) {
    // If we have results, we're coming back from a details page
    console.log('Restoring search results from store');
  } else if (Object.keys(route.query).length > 0) {
    // If we have query parameters, restore filters and search
    console.log('Restoring filters from query parameters');
    lawyerSearchStore.restoreFromQuery(route.query);
    lawyerSearchStore.searchLawyers();
  }
});

async function searchLawyers() {
  await lawyerSearchStore.searchLawyers();
}

function getInitial(lawyer) {
  if (lawyer.firstName) {
    return lawyer.firstName.charAt(0).toUpperCase();
  }
  if (lawyer.email) {
    return lawyer.email.charAt(0).toUpperCase();
  }
  return "U";
}

function viewLawyerDetails(email) {
  // Preserve current search state in the store before navigating
  console.log('Navigating to lawyer details, preserving search state');
  router.push({
    path: `/lawyers/${encodeURIComponent(email)}/details`,
    query: { from: 'search' }
  });
}
</script>

<style scoped>
.find-lawyers-view {
  min-height: 100vh;
  background: linear-gradient(
    135deg,
    var(--color-background-soft) 0%,
    var(--color-background-mute) 100%
  );
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 3rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0;
  letter-spacing: -0.025em;
}

.filters-form {
  background: var(--color-background);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  color: var(--color-heading);
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-input,
.form-select {
  padding: 0.75rem 1rem;
  border: 2px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 1rem;
  background: var(--color-background);
  color: var(--color-text);
  transition: all var(--transition-fast);
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(6, 147, 227, 0.1);
}

.search-button {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  border: none;
  padding: 1rem 2rem;
  border-radius: var(--border-radius);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-width: 160px;
}

.search-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.search-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.alert {
  padding: 1rem 1.5rem;
  border-radius: var(--border-radius);
  margin-bottom: 2rem;
  font-weight: 500;
}

.alert-error {
  background: var(--color-error);
  color: var(--color-background);
  border: 1px solid var(--color-error);
  opacity: 0.9;
}

.results-section {
  background: var(--color-background);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.results-header {
  margin-bottom: 2rem;
}

.results-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0;
}

.lawyers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
}

.lawyer-card {
  background: var(--color-background-soft);
  border: 2px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: 1.5rem;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.lawyer-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    var(--color-primary),
    var(--color-secondary)
  );
  transform: scaleX(0);
  transition: transform var(--transition-normal);
}

.lawyer-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary);
}

.lawyer-card:hover::before {
  transform: scaleX(1);
}

.lawyer-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.lawyer-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    var(--color-primary),
    var(--color-secondary)
  );
  color: var(--color-background);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  margin-right: 1rem;
  flex-shrink: 0;
}

.lawyer-info {
  flex: 1;
}

.lawyer-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0 0 0.25rem 0;
}

.lawyer-firm {
  color: var(--color-text-muted);
  margin: 0;
  font-size: 0.875rem;
}

.lawyer-details {
  margin-bottom: 1.5rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 500;
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.detail-value {
  font-weight: 600;
  color: var(--color-heading);
  font-size: 0.875rem;
}

.specializations {
  margin-top: 1rem;
}

.specializations-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.75rem 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.specializations-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.specialization-tag {
  background: linear-gradient(
    135deg,
    var(--color-primary),
    var(--color-secondary)
  );
  color: var(--color-background);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.025em;
}

.no-specializations {
  color: var(--color-text-muted);
  font-style: italic;
  font-size: 0.875rem;
}

.view-details-button {
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-secondary) 100%
  );
  color: var(--color-background);
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
  margin-left: 1rem;
}

.view-details-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.view-details-button:active {
  transform: translateY(0);
}

.no-results {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--color-background);
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.no-results h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
  margin: 0 0 0.5rem 0;
}

.no-results p {
  color: var(--color-text-muted);
  margin: 0;
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .filters-grid {
    grid-template-columns: 1fr;
  }

  .lawyers-grid {
    grid-template-columns: 1fr;
  }

  .filters-form {
    padding: 1.5rem;
  }

  .results-section {
    padding: 1.5rem;
  }
}
</style>
