import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { lawyerAPI } from "../services/api";

export const useLawyerSearchStore = defineStore("lawyerSearch", () => {
  // State
  const lawyers = ref([]);
  const loading = ref(false);
  const error = ref("");
  const searched = ref(false);
  const filters = ref({
    minExperience: null,
    maxExperience: null,
    practicingCourt: "",
    division: "",
    district: "",
    specialization: "",
  });

  // Computed
  const hasResults = computed(() => lawyers.value.length > 0);
  const hasFilters = computed(() => {
    return Object.values(filters.value).some(
      (value) => value !== null && value !== "" && value !== undefined
    );
  });

  // Actions
  const setFilters = (newFilters) => {
    filters.value = { ...newFilters };
  };

  const clearFilters = () => {
    filters.value = {
      minExperience: null,
      maxExperience: null,
      practicingCourt: "",
      division: "",
      district: "",
      specialization: "",
    };
  };

  const setLawyers = (newLawyers) => {
    lawyers.value = newLawyers;
  };

  const clearResults = () => {
    lawyers.value = [];
    searched.value = false;
    error.value = "";
  };

  const searchLawyers = async (searchFilters = null) => {
    loading.value = true;
    error.value = "";
    searched.value = true;

    // Use provided filters or current store filters
    const filtersToUse = searchFilters || filters.value;

    try {
      const payload = {
        minExperience: filtersToUse.minExperience,
        maxExperience: filtersToUse.maxExperience,
        practicingCourt: filtersToUse.practicingCourt,
        division: filtersToUse.division,
        district: filtersToUse.district,
        specialization: filtersToUse.specialization,
      };

      // Remove empty values
      Object.keys(payload).forEach((key) => {
        if (payload[key] === null || payload[key] === "") {
          delete payload[key];
        }
      });

      const response = await lawyerAPI.findLawyers(payload);

      if (response.data && Array.isArray(response.data)) {
        // Map the response to include display names
        const mappedLawyers = response.data.map((lawyer) => ({
          ...lawyer,
          practicingCourtDisplayName: getPracticingCourtDisplayName(
            lawyer.practicingCourt
          ),
          divisionDisplayName: getDivisionDisplayName(lawyer.division),
          districtDisplayName: getDistrictDisplayName(lawyer.district),
          specializationDisplayNames: getSpecializationDisplayNames(
            lawyer.specializations
          ),
        }));

        lawyers.value = mappedLawyers;
      } else {
        lawyers.value = [];
      }
    } catch (err) {
      console.error("Error fetching lawyers:", err);
      error.value =
        err.response?.data?.error?.message ||
        "Failed to fetch lawyers. Please try again.";
      lawyers.value = [];
    } finally {
      loading.value = false;
    }
  };

  const restoreFromQuery = (query) => {
    if (Object.keys(query).length > 0) {
      filters.value.minExperience = query.minExperience
        ? Number(query.minExperience)
        : null;
      filters.value.maxExperience = query.maxExperience
        ? Number(query.maxExperience)
        : null;
      filters.value.practicingCourt = query.practicingCourt || "";
      filters.value.division = query.division || "";
      filters.value.district = query.district || "";
      filters.value.specialization = query.specialization || "";
      return true;
    }
    return false;
  };

  const getQueryParams = () => {
    const params = {};
    if (filters.value.minExperience !== null)
      params.minExperience = filters.value.minExperience;
    if (filters.value.maxExperience !== null)
      params.maxExperience = filters.value.maxExperience;
    if (filters.value.practicingCourt)
      params.practicingCourt = filters.value.practicingCourt;
    if (filters.value.division) params.division = filters.value.division;
    if (filters.value.district) params.district = filters.value.district;
    if (filters.value.specialization)
      params.specialization = filters.value.specialization;
    return params;
  };

  // Helper functions for display names
  const practicingCourts = [
    { value: "SUPREME_COURT", label: "Supreme Court" },
    { value: "HIGH_COURT_DIVISION", label: "High Court Division" },
    { value: "APPELLATE_DIVISION", label: "Appellate Division" },
    { value: "DISTRICT_COURT", label: "District Court" },
    { value: "SESSIONS_COURT", label: "Sessions Court" },
    { value: "ADMINISTRATIVE_TRIBUNAL", label: "Administrative Tribunal" },
    { value: "LABOUR_COURT", label: "Labour Court" },
    { value: "FAMILY_COURT", label: "Family Court" },
    { value: "MAGISTRATE_COURT", label: "Magistrate Court" },
    { value: "SPECIAL_TRIBUNAL", label: "Special Tribunal" },
  ];

  const divisions = [
    { value: "DHAKA", label: "Dhaka" },
    { value: "CHATTOGRAM", label: "Chattogram" },
    { value: "RAJSHAHI", label: "Rajshahi" },
    { value: "KHULNA", label: "Khulna" },
    { value: "BARISHAL", label: "Barishal" },
    { value: "SYLHET", label: "Sylhet" },
    { value: "RANGPUR", label: "Rangpur" },
    { value: "MYMENSINGH", label: "Mymensingh" },
  ];

  const districts = [
    { value: "DHAKA", label: "Dhaka" },
    { value: "CHATTOGRAM", label: "Chattogram" },
    { value: "RAJSHAHI", label: "Rajshahi" },
    { value: "KHULNA", label: "Khulna" },
    { value: "BARISHAL", label: "Barishal" },
    { value: "SYLHET", label: "Sylhet" },
    { value: "RANGPUR", label: "Rangpur" },
    { value: "MYMENSINGH", label: "Mymensingh" },
    { value: "COMILLA", label: "Comilla" },
    { value: "GAZIPUR", label: "Gazipur" },
    { value: "NARAYANGANJ", label: "Narayanganj" },
    { value: "TANGAIL", label: "Tangail" },
    { value: "NARSINGDI", label: "Narsingdi" },
    { value: "MUNSHIGANJ", label: "Munshiganj" },
    { value: "MANIKGANJ", label: "Manikganj" },
    { value: "MADARIPUR", label: "Madaripur" },
    { value: "SHARIATPUR", label: "Shariatpur" },
    { value: "RAJBARI", label: "Rajbari" },
    { value: "GOPALGANJ", label: "Gopalganj" },
    { value: "FARIDPUR", label: "Faridpur" },
    { value: "KISHOREGANJ", label: "Kishoreganj" },
    { value: "NETROKONA", label: "Netrokona" },
    { value: "JAMALPUR", label: "Jamalpur" },
    { value: "SHERPUR", label: "Sherpur" },
    { value: "BOGRA", label: "Bogra" },
    { value: "JOYPURHAT", label: "Joypurhat" },
    { value: "NAOGAON", label: "Naogaon" },
    { value: "NATORE", label: "Natore" },
    { value: "CHAPAI_NAWABGANJ", label: "Chapai Nawabganj" },
    { value: "PABNA", label: "Pabna" },
    { value: "SIRAJGANJ", label: "Sirajganj" },
    { value: "DINAJPUR", label: "Dinajpur" },
    { value: "THAKURGAON", label: "Thakurgaon" },
    { value: "PANCHAGARH", label: "Panchagarh" },
    { value: "NILPHAMARI", label: "Nilphamari" },
    { value: "LALMONIRHAT", label: "Lalmonirhat" },
    { value: "KURIGRAM", label: "Kurigram" },
    { value: "GAIBANDHA", label: "Gaibandha" },
    { value: "COXS_BAZAR", label: "Cox's Bazar" },
    { value: "BANDARBAN", label: "Bandarban" },
    { value: "RANGAMATI", label: "Rangamati" },
    { value: "KHAGRACHHARI", label: "Khagrachhari" },
    { value: "FENI", label: "Feni" },
    { value: "LAKSHMIPUR", label: "Lakshmipur" },
    { value: "CHANDPUR", label: "Chandpur" },
    { value: "NOAKHALI", label: "Noakhali" },
    { value: "BRAHMANBARIA", label: "Brahmanbaria" },
    { value: "JESSORE", label: "Jessore" },
    { value: "SATKHIRA", label: "Satkhira" },
    { value: "MEHERPUR", label: "Meherpur" },
    { value: "NARAIL", label: "Narail" },
    { value: "CHUADANGA", label: "Chuadanga" },
    { value: "KUSHTIA", label: "Kushtia" },
    { value: "MAGURA", label: "Magura" },
    { value: "JHENAIDAH", label: "Jhenaidah" },
    { value: "BAGERHAT", label: "Bagerhat" },
    { value: "PIROJPUR", label: "Pirojpur" },
    { value: "JHALOKATHI", label: "Jhalokathi" },
    { value: "BHOLA", label: "Bhola" },
    { value: "PATUAKHALI", label: "Patuakhali" },
    { value: "BARGUNA", label: "Barguna" },
    { value: "SUNAMGANJ", label: "Sunamganj" },
    { value: "HABIGANJ", label: "Habiganj" },
    { value: "MOULVIBAZAR", label: "Moulvibazar" },
  ];

  const specializations = [
    { value: "CRIMINAL_LAW", label: "Criminal Law" },
    { value: "CIVIL_LAW", label: "Civil Law" },
    { value: "FAMILY_LAW", label: "Family Law" },
    { value: "LABOUR_LAW", label: "Labour Law" },
    { value: "CORPORATE_LAW", label: "Corporate Law" },
    { value: "CONSTITUTIONAL_LAW", label: "Constitutional Law" },
    { value: "TAX_LAW", label: "Tax Law" },
    { value: "ENVIRONMENTAL_LAW", label: "Environmental Law" },
    { value: "INTELLECTUAL_PROPERTY_LAW", label: "Intellectual Property Law" },
    { value: "BANKING_LAW", label: "Banking and Finance Law" },
    { value: "PROPERTY_LAW", label: "Property and Real Estate Law" },
    { value: "HUMAN_RIGHTS_LAW", label: "Human Rights Law" },
    { value: "INTERNATIONAL_LAW", label: "International Law" },
    { value: "CYBER_LAW", label: "Cyber and ICT Law" },
    { value: "CONTRACT_LAW", label: "Contract Law" },
    { value: "ADMINISTRATIVE_LAW", label: "Administrative Law" },
    { value: "IMMIGRATION_LAW", label: "Immigration Law" },
    { value: "CONSUMER_LAW", label: "Consumer Protection Law" },
    { value: "INSURANCE_LAW", label: "Insurance Law" },
    { value: "MARITIME_LAW", label: "Maritime Law" },
    { value: "EDUCATION_LAW", label: "Education Law" },
  ];

  const getPracticingCourtDisplayName = (value) => {
    return practicingCourts.find((c) => c.value === value)?.label || value;
  };

  const getDivisionDisplayName = (value) => {
    return divisions.find((d) => d.value === value)?.label || value;
  };

  const getDistrictDisplayName = (value) => {
    return districts.find((d) => d.value === value)?.label || value;
  };

  const getSpecializationDisplayNames = (specializations) => {
    if (!specializations || !Array.isArray(specializations)) return [];
    return specializations.map(
      (s) => specializations.find((ls) => ls.value === s)?.label || s
    );
  };

  return {
    // State
    lawyers,
    loading,
    error,
    searched,
    filters,

    // Computed
    hasResults,
    hasFilters,

    // Actions
    setFilters,
    clearFilters,
    setLawyers,
    clearResults,
    searchLawyers,
    restoreFromQuery,
    getQueryParams,

    // Helper functions
    getPracticingCourtDisplayName,
    getDivisionDisplayName,
    getDistrictDisplayName,
    getSpecializationDisplayNames,

    // Constants
    practicingCourts,
    divisions,
    districts,
    specializations,
  };
});
