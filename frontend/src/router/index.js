import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: HomeView,
    },
    {
      path: "/about",
      name: "about",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/AboutView.vue"),
    },
    {
      path: "/services",
      name: "services",
      component: () => import("../views/ServicesView.vue"),
    },
    {
      path: "/how-it-works",
      name: "how-it-works",
      component: () => import("../views/HowItWorksView.vue"),
    },
    {
      path: "/contact",
      name: "contact",
      component: () => import("../views/ContactView.vue"),
    },
    {
      path: "/register",
      name: "register",
      component: () => import("../views/RegisterView.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },
    {
      path: "/forgot-password",
      name: "forgot-password",
      component: () => import("../views/ForgotPasswordView.vue"),
    },

    {
      path: "/email-verification",
      name: "email-verification",
      component: () => import("../views/EmailVerificationView.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/dashboard/user",
      name: "user-dashboard",
      component: () => import("../views/UserDashboard.vue"),
      meta: { requiresAuth: true, requiresEmailVerification: true },
    },
    {
      path: "/dashboard/lawyer",
      name: "lawyer-dashboard",
      component: () => import("../views/LawyerDashboard.vue"),
      meta: { requiresAuth: true, requiresEmailVerification: true },
    },
    {
      path: "/dashboard/admin",
      name: "admin-dashboard",
      component: () => import("../views/AdminDashboard.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/lawyer/profile/create",
      name: "lawyer-profile-creation",
      component: () => import("../views/LawyerProfileCreationView.vue"),
      meta: { requiresAuth: true, requiresEmailVerification: true },
    },
    {
      path: "/profile",
      name: "view-profile",
      component: () => import("../views/ProfileView.vue"),
      meta: { requiresAuth: true, requiresEmailVerification: true },
    },
  ],
});

// Navigation guards
router.beforeEach(async (to, from, next) => {
  // Get auth store
  const authStore = JSON.parse(
    localStorage.getItem("auth_isLoggedIn") || "false"
  );
  const userInfo = JSON.parse(localStorage.getItem("auth_userInfo") || "null");

  // Check if route requires authentication
  if (to.meta.requiresAuth && !authStore) {
    next("/login");
    return;
  }

  // Check if route requires email verification
  if (
    to.meta.requiresEmailVerification &&
    userInfo &&
    !userInfo.emailVerified
  ) {
    next("/email-verification");
    return;
  }

  // If user is already verified and trying to access email verification page, redirect to dashboard
  if (to.name === "email-verification" && userInfo && userInfo.emailVerified) {
    if (userInfo.role === "LAWYER") {
      next("/dashboard/lawyer");
    } else {
      next("/dashboard/user");
    }
    return;
  }

  // For lawyer dashboard, let the component handle verification status
  // The LawyerDashboard component will show appropriate content based on verification status
  if (
    to.name === "lawyer-dashboard" &&
    userInfo &&
    userInfo.role === "LAWYER"
  ) {
    // Allow access to lawyer dashboard - the component will handle the verification logic
    next();
    return;
  }

  next();
});

export default router;
