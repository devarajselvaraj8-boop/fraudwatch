// FraudWatch - Flagged Transactions Page Logic
// Strict Two-Color Minimalist Architecture: Navy (#0F172A) & White (#FFFFFF)

let allFlagged = [];

document.addEventListener('DOMContentLoaded', () => {
    loadFlaggedTransactions();

    const searchInput = document.getElementById('flagged-search');
    if (searchInput) {
        searchInput.addEventListener('input', applyFlaggedFilters);
    }

    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            filterButtons.forEach(b => b.classList.remove('active'));
            e.currentTarget.classList.add('active');
            applyFlaggedFilters();
        });
    });

    const refreshBtn = document.getElementById('refresh-flagged-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadFlaggedTransactions);
    }
});

async function loadFlaggedTransactions() {
    const loadingEl = document.getElementById('flagged-loading');
    const tableEl = document.getElementById('flagged-table-container');
    const emptyEl = document.getElementById('flagged-empty');
    const errorEl = document.getElementById('flagged-error');

    if (loadingEl) loadingEl.style.display = 'flex';
    if (tableEl) tableEl.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';

    try {
        const data = await getAllReviews();
        allFlagged = Array.isArray(data) ? [...data].sort((a, b) => b.id - a.id) : [];

        if (loadingEl) loadingEl.style.display = 'none';

        if (allFlagged.length === 0) {
            if (emptyEl) emptyEl.style.display = 'block';
        } else {
            if (tableEl) tableEl.style.display = 'block';
            applyFlaggedFilters();
        }
    } catch (error) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            const msgEl = document.getElementById('flagged-error-msg');
            if (msgEl) msgEl.textContent = error.message;
        }
        showToast(error.message, 'error');
    }
}

function applyFlaggedFilters() {
    const searchVal = (document.getElementById('flagged-search')?.value || '').toLowerCase().trim();
    const activeBtn = document.querySelector('.filter-btn.active');
    const statusVal = activeBtn ? activeBtn.getAttribute('data-status') : 'ALL';

    const filtered = allFlagged.filter(item => {
        const tx = item.transaction || {};
        const matchesSearch =
            (tx.id && tx.id.toString().includes(searchVal)) ||
            (tx.sender && tx.sender.toLowerCase().includes(searchVal)) ||
            (tx.receiver && tx.receiver.toLowerCase().includes(searchVal)) ||
            item.id.toString().includes(searchVal);

        const matchesStatus = statusVal === 'ALL' || item.reviewStatus === statusVal;

        return matchesSearch && matchesStatus;
    });

    renderFlaggedTable(filtered);
}

function renderFlaggedTable(items) {
    const tbody = document.getElementById('flagged-table-body');
    const emptyEl = document.getElementById('flagged-empty');
    const tableEl = document.getElementById('flagged-table-container');

    if (!tbody) return;

    if (items.length === 0) {
        if (tableEl) tableEl.style.display = 'none';
        if (emptyEl) {
            emptyEl.style.display = 'block';
        }
        return;
    }

    if (tableEl) tableEl.style.display = 'block';
    if (emptyEl) emptyEl.style.display = 'none';

    tbody.innerHTML = items.map(item => {
        const tx = item.transaction || {};
        const ruleNames = (item.triggeredRules || []).map(r => `<span class="rule-chip font-mono">${escapeHtml(r.type || r.name)}</span>`).join(' ');

        return `
            <tr>
                <td class="font-mono">#${tx.id || item.id}</td>
                <td class="font-mono">${escapeHtml(tx.sender || '—')}</td>
                <td class="font-mono">${escapeHtml(tx.receiver || '—')}</td>
                <td class="font-mono" style="font-weight: 700;">${formatCurrency(tx.amount)}</td>
                <td style="color: var(--navy-60); font-size: 0.82rem;">${formatDateTime(item.flaggedAt)}</td>
                <td>${ruleNames}</td>
                <td>${getStatusBadgeHtml(item.reviewStatus)}</td>
                <td class="text-right">
                    ${item.reviewStatus === 'PENDING' ? `
                        <a href="reviews.html" class="btn btn-secondary btn-sm">Review</a>
                    ` : `
                        <span style="font-size: 0.78rem; color: var(--navy-60);">${escapeHtml(item.reviewer || 'Resolved')}</span>
                    `}
                </td>
            </tr>
        `;
    }).join('');
}
