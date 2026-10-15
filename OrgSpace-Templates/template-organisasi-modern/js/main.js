/**
 * Template Website Organisasi Modern - OrgSpace
 * Logika interaktivitas client-side ringan (Vanilla JavaScript).
 */

document.addEventListener("DOMContentLoaded", () => {
  // 1. Mobile Menu Navigation Toggle
  const toggleBtn = document.querySelector(".mobile-toggle");
  const navMenu = document.querySelector(".nav-menu");

  if (toggleBtn && navMenu) {
    toggleBtn.addEventListener("click", () => {
      navMenu.classList.toggle("is-active");
      const isExpanded = navMenu.classList.contains("is-active");
      toggleBtn.setAttribute("aria-expanded", String(isExpanded));
    });

    // Tutup menu saat salah satu link diklik
    navMenu.querySelectorAll(".nav-link").forEach((link) => {
      link.addEventListener("click", () => {
        navMenu.classList.remove("is-active");
      });
    });
  }

  // 2. Smooth Scroll untuk Anchor Links
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      const targetId = this.getAttribute("href");
      if (targetId && targetId !== "#") {
        const targetElement = document.querySelector(targetId);
        if (targetElement) {
          e.preventDefault();
          targetElement.scrollIntoView({
            behavior: "smooth",
            block: "start",
          });
        }
      }
    });
  });
});
