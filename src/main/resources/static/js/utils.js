// FraudWatch - Utility Functions & UI Helpers
// Strict Two-Color Minimalist Architecture: Navy (#0F172A) & White (#FFFFFF)

function formatCurrency(amount) {
    if (amount === null || amount === undefined || isNaN(amount)) return '₹0.00';
    return '₹' + Number(amount).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatDateTime(isoString) {
    if (!isoString) return '—';
    try {
        const d = new Date(isoString);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    } catch {
        return isoString;
    }
}

const formatDate = formatDateTime;

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * Status Badges: Navy & White ONLY (No green, red, yellow, etc.)
 */
function getStatusBadgeHtml(status) {
    const s = String(status || '').toUpperCase();
    let modifier = '';
    if (s === 'COMPLETED') modifier = 'completed';
    else if (s === 'FLAGGED') modifier = 'flagged';
    else if (s === 'BLOCKED') modifier = 'blocked';
    else if (s === 'PENDING') modifier = 'pending';
    else if (s === 'APPROVED') modifier = 'approved';

    return `<span class="status-badge ${modifier}">[ ${escapeHtml(s)} ]</span>`;
}

function getRuleTypeBadge(type) {
    return `<span class="rule-tag font-mono">[ ${escapeHtml(type)} ]</span>`;
}

/**
 * Toast Notification System: Navy & White ONLY
 */
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast';

    let label = 'NOTE';
    if (type === 'success') label = 'SUCCESS';
    else if (type === 'error') label = 'ALERT';
    else if (type === 'warning') label = 'WARNING';

    toast.innerHTML = `
        <span class="toast-icon font-mono">[${label}]</span>
        <span class="toast-message">${escapeHtml(message)}</span>
        <button class="toast-close" onclick="this.parentElement.remove()" aria-label="Close">&times;</button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.2s ease';
        setTimeout(() => toast.remove(), 250);
    }, 4500);
}

// Modal Handlers
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

// Close modal when clicking backdrop
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('active');
        document.body.style.overflow = '';
    }
});

function debounce(func, delay = 250) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => func(...args), delay);
    };
}

function renderLoading(msg = 'Loading...') {
    return `<div class="empty-state" style="border:none; padding: 24px;"><span class="spinner"></span> <span style="margin-left: 8px;">${escapeHtml(msg)}</span></div>`;
}
