// final script.js — robust dropdown + hamburger + auth UI
// Expects /api/auth/me (GET) and /api/auth/logout (POST) to be implemented on the backend.

document.addEventListener("DOMContentLoaded", function () {

  /* Local storage helpers */
  function getUserLocal() {
    try { return JSON.parse(localStorage.getItem("kindoraUser")); }
    catch { return null; }
  }
  function setUserLocal(u) {
    try { localStorage.setItem("kindoraUser", JSON.stringify(u)); } catch {}
  }
  function clearUserLocal() {
    try { localStorage.removeItem("kindoraUser"); } catch {}
  }

  /* fetch helper with credentials */
  async function fetchWithCred(url, opts = {}) {
    opts = opts || {};
    opts.credentials = opts.credentials || 'include';
    return fetch(url, opts);
  }

  /* backend helpers */
  async function serverGetMe() {
    try {
      const r = await fetchWithCred('/api/auth/me', { method: 'GET' });
      if (!r.ok) return null;
      const j = await r.json().catch(()=>null);
      return j;
    } catch (e) {
      return null;
    }
  }

  async function serverLogout() {
    try {
      await fetchWithCred('/api/auth/logout', { method: 'POST' });
    } catch (e) { /* ignore network errors */ }
    clearUserLocal();
    window.location.href = 'authentication.html';
  }

  /* Protect navigation — show/hide login/profile; intercept protected actions when not logged in */
  async function protectNavigation() {
    const me = await serverGetMe();

    const loginBtn = document.querySelector(".btn.login");
    const profileContainer = document.getElementById("profileContainer");
    const profileBtn = document.getElementById("profileBtn");

    if (!me) {
      clearUserLocal();
      if (profileContainer) profileContainer.style.display = "none";
      if (loginBtn) { loginBtn.style.display = ""; loginBtn.classList.remove('hidden'); }

      // intercept protected elements
      document.querySelectorAll(".btn-donate, .dash-btn, .nav-link, .btn-primary").forEach(el => {
        if (el.dataset._protectAttached) return;
        el.dataset._protectAttached = "1";
        el.addEventListener("click", function (ev) {
          ev.preventDefault();
          window.location.href = 'authentication.html';
        });
      });
      return;
    }

    // logged in
    setUserLocal(me);
    if (loginBtn) { loginBtn.style.display = 'none'; loginBtn.classList.add('hidden'); }
    if (profileContainer) profileContainer.style.display = 'inline-block';

    // set initial
    const nameCandidate = me.fullName || me.name || me.email || '';
    if (profileBtn) {
      const initial = nameCandidate ? nameCandidate.trim().charAt(0).toUpperCase() : 'U';
      const pin = document.getElementById('profileInitial');
      if (pin) pin.textContent = initial;
      else profileBtn.textContent = initial;
    }

    // reset protected intercepts by cloning nodes (safe reset)
    document.querySelectorAll(".btn-donate, .dash-btn, .nav-link, .btn-primary").forEach(el => {
      const clone = el.cloneNode(true);
      el.parentNode.replaceChild(clone, el);
    });
  }

  /* Profile dropdown + modal */
  const profileBtn = document.getElementById("profileBtn");
  const profileDropdown = document.getElementById("profileDropdown");
  const viewProfile = document.getElementById("viewProfile");
  const logoutBtn = document.getElementById("logoutBtn");

  // defensive cleanup: remove any inline display property that might block CSS toggles
  if (profileDropdown && profileDropdown.style && profileDropdown.style.display) {
    profileDropdown.style.removeProperty('display');
  }

  if (profileBtn) {
    profileBtn.addEventListener("click", function (ev) {
      ev.stopPropagation();
      if (!profileDropdown) return;
      const isOpen = profileDropdown.classList.contains('open');
      profileDropdown.classList.toggle('open', !isOpen);
      profileBtn.setAttribute('aria-expanded', String(!isOpen));
      profileDropdown.setAttribute('aria-hidden', String(isOpen)); // when now open -> aria-hidden=false
      // ensure no stuck inline style
      profileDropdown.style.removeProperty('display');
    });
  }

  // global click to close
  document.addEventListener("click", function (e) {
    if (!profileDropdown || !profileBtn) return;
    if (!profileDropdown.contains(e.target) && !profileBtn.contains(e.target)) {
      profileDropdown.classList.remove('open');
      profileBtn.setAttribute('aria-expanded', 'false');
      profileDropdown.setAttribute('aria-hidden', 'true');
    }
  });

  // view profile
  if (viewProfile) {
    viewProfile.addEventListener('click', (ev) => {
      ev.preventDefault();
      if (profileDropdown) {
        profileDropdown.classList.remove('open');
        profileDropdown.setAttribute('aria-hidden', 'true');
      }
      const user = getUserLocal();
      if (!user) {
        (async () => {
          const fresh = await serverGetMe();
          if (!fresh) return alert('Profile not available. Please login again.');
          setUserLocal(fresh);
          openProfileModal(fresh);
        })();
        return;
      }
      openProfileModal(user);
    });
  }

  function openProfileModal(user) {
    const fullName = user.fullName || user.name || '';
    const email = user.email || user.username || '';
    const phone = user.phoneNumber || user.phone || user.mobile || '';
    const role = user.role || user.userRole || '';

    const backdrop = document.createElement('div');
    backdrop.className = 'profile-modal__backdrop';
    backdrop.innerHTML = `
      <div class="profile-modal" role="dialog" aria-modal="true" aria-labelledby="profileTitle">
        <h2 id="profileTitle">My Profile</h2>
        <p><strong>Name:</strong> ${escapeHtml(fullName) || '-'}</p>
        <p><strong>Email:</strong> ${escapeHtml(email) || '-'}</p>
        <p><strong>Phone:</strong> ${escapeHtml(phone) || '-'}</p>
        <p><strong>Role:</strong> ${escapeHtml(role) || '-'}</p>
        <div style="text-align:right;margin-top:12px">
          <button id="closeProfileModal" class="btn-ghost">Close</button>
        </div>
      </div>
    `;
    backdrop.addEventListener('click', function (ev) {
      if (ev.target === backdrop) backdrop.remove();
    });

    document.body.appendChild(backdrop);
    const closeBtn = document.getElementById('closeProfileModal');
    if (closeBtn) closeBtn.addEventListener('click', () => backdrop.remove());
  }

  function escapeHtml(s) {
    if (!s) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', (ev) => {
      ev.preventDefault();
      if (profileDropdown) {
        profileDropdown.classList.remove('open');
        profileDropdown.setAttribute('aria-hidden', 'true');
      }
      serverLogout();
    });
  }

  /* Hamburger / mobile menu logic */
  const hamburger = document.getElementById("hamburger");
  const mobileMenu = document.getElementById("mobile-menu");
  const mobileClose = document.getElementById("mobile-close");
  const overlay = document.getElementById("overlay");

  function openMenu() {
    if (!mobileMenu || !overlay) return;
    mobileMenu.classList.add('open');
    mobileMenu.setAttribute('aria-hidden', 'false');
    overlay.classList.add('show');
    overlay.setAttribute('aria-hidden', 'false');
    hamburger.setAttribute('aria-expanded', 'true');
    document.body.style.overflow = 'hidden';
  }
  function closeMenu() {
    if (!mobileMenu || !overlay) return;
    mobileMenu.classList.remove('open');
    mobileMenu.setAttribute('aria-hidden', 'true');
    overlay.classList.remove('show');
    overlay.setAttribute('aria-hidden', 'true');
    hamburger.setAttribute('aria-expanded', 'false');
    document.body.style.overflow = '';
  }

  if (hamburger) {
    hamburger.addEventListener('click', function (e) {
      e.stopPropagation();
      const opened = mobileMenu && mobileMenu.classList.contains('open');
      if (opened) closeMenu();
      else openMenu();
    });
  }
  if (mobileClose) mobileClose.addEventListener('click', closeMenu);
  if (overlay) overlay.addEventListener('click', closeMenu);

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      closeMenu();
      if (profileDropdown) {
        profileDropdown.classList.remove('open');
        profileDropdown.setAttribute('aria-hidden', 'true');
      }
    }
  });

  /* Simple hero slider (non-blocking) */
  const slides = Array.from(document.querySelectorAll(".hero-slider .slide"));
  const dotsContainer = document.getElementById("slider-dots");
  let current = 0;
  const INTERVAL = 4000;
  let timer = null;
  if (slides.length && dotsContainer) {
    slides.forEach((s, i) => {
      const dot = document.createElement("button");
      dot.dataset.index = i;
      if (i === 0) dot.classList.add("active");
      dotsContainer.appendChild(dot);
    });
    const dots = dotsContainer.querySelectorAll("button");
    function showSlide(index) {
      index = (index + slides.length) % slides.length;
      slides.forEach((s, i) => s.classList.toggle("active", i === index));
      dots.forEach((d, i) => d.classList.toggle("active", i === index));
      current = index;
    }
    function next() { showSlide(current + 1); }
    dotsContainer.addEventListener("click", (e) => {
      const idx = parseInt(e.target.dataset.index);
      if (!isNaN(idx)) showSlide(idx);
    });
    timer = setInterval(next, INTERVAL);
  }

  /* Init: check auth + UI */
  (async function initAuthAndUI() {
    try {
      clearUserLocal();
      const me = await serverGetMe();
      if (me) setUserLocal(me);
    } catch (e) { clearUserLocal(); }
    await protectNavigation();
    const y = new Date().getFullYear();
    const yearEl = document.getElementById('year');
    if (yearEl) yearEl.textContent = y;
  })();

});
