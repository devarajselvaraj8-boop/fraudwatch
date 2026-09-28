// FraudWatch - Global Application Shell & Navigation
// Minimalist Navy & White system

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initMobileSidebar();
});

function initNavigation() {
    const currentPath = window.location.pathname.toLowerCase();
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        const href = (link.getAttribute('href') || '').toLowerCase();
        if (currentPath.endsWith(href) || (currentPath.endsWith('/') && href.includes('dashboard.html'))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

function initMobileSidebar() {
    const toggleBtn = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            if (overlay) overlay.classList.toggle('open');
        });
    }

    if (overlay && sidebar) {
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('open');
        });
    }
}
