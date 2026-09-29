// FraudWatch - Minimalist Banking Dashboard Controller
// Interacts with Spring Boot backend APIs directly (Zero fake data)

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();

    const refreshBtn = document.getElementById('refresh-dashboard-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadDashboardData);
    }
});

async function loadDashboardData() {
    const loadingState = document.getElementById('dashboard-loading');
    const contentState = document.getElementById('dashboard-content');
    const errorState = document.getElementById('dashboard-error');

    if (loadingState) loadingState.style.display = 'flex';
    if (contentState) contentState.style.display = 'none';
    if (errorState) errorState.style.display = 'none';

    try {
        const [stats, rulesStats, transactions, pendingReviews] = await Promise.all([
            getDashboard(),
            getDashboardRules(),
            getTransactions(),
            getPendingReviews()
        ]);

        // Section 2: Render 5 Simple Statistic Cards
        renderKpiCards(stats);

        // Section 3: Render Recent Transactions Table
        renderRecentTransactions(transactions);

        // Section 4: Render Fraud Rules Triggers
        renderFraudRules(rulesStats);

        // Section 5: Render Pending Reviews Table
        renderPendingReviews(pendingReviews);

        if (loadingState) loadingState.style.display = 'none';
        if (contentState) contentState.style.display = 'block';
    } catch (error) {
        console.error('Failed to load dashboard data:', error);
        if (loadingState) loadingState.style.display = 'none';
        if (errorState) {
            errorState.style.display = 'block';
            const errorMsg = document.getElementById('dashboard-error-msg');
            if (errorMsg) errorMsg.textContent = error.message;
        }
        if (typeof showToast === 'function') {
            showToast(error.message, 'error');
        }
    }
}

/**
 * Section 2: Five simple statistic cards
 */
function renderKpiCards(stats) {
    if (!stats) return;

    const totalEl = document.getElementById('stat-total');
    const completedEl = document.getElementById('stat-completed');
    const flaggedEl = document.getElementById('stat-flagged');
    const blockedEl = document.getElementById('stat-blocked');
    const pendingEl = document.getElementById('stat-pending');

    if (totalEl) totalEl.textContent = stats.totalTransactions ?? 0;
    if (completedEl) completedEl.textContent = stats.completedTransactions ?? 0;
    if (flaggedEl) flaggedEl.textContent = stats.flaggedTransactions ?? 0;
    if (blockedEl) blockedEl.textContent = stats.blockedTransactions ?? 0;
    if (pendingEl) pendingEl.textContent = stats.pendingReviews ?? 0;
}

/**
 * Section 3: Recent Transactions Table
 */
function renderRecentTransactions(transactions) {
    const tbody = document.getElementById('recent-tx-table-body');
    if (!tbody) return;

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="dash-empty">No transactions recorded in the ledger yet.</td></tr>';
        return;
    }

    // Sort descending by ID, display recent 6
    const recent = [...transactions].sort((a, b) => b.id - a.id).slice(0, 6);

    tbody.innerHTML = recent.map(tx => {
        let statusClass = 'completed';
        if (tx.status === 'FLAGGED') statusClass = 'flagged';
        else if (tx.status === 'BLOCKED') statusClass = 'blocked';

        return `
            <tr>
                <td class="font-mono">#${tx.id}</td>
                <td class="font-mono">${escapeHtml(tx.sender)}</td>
                <td class="font-mono">${escapeHtml(tx.receiver)}</td>
                <td class="font-mono" style="font-weight: 700;">${formatCurrency(tx.amount)}</td>
                <td style="color: var(--navy-muted); font-size: 0.82rem;">${formatDateTime(tx.timestamp)}</td>
                <td>
                    <span class="dash-status ${statusClass}">${escapeHtml(tx.status)}</span>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Section 4: Fraud Rules Statistics (High Amount, Sender Velocity, Receiver Velocity)
 */
function renderFraudRules(rulesStats) {
    const highAmountCountEl = document.getElementById('rule-high-amount-count');
    const senderVelocityCountEl = document.getElementById('rule-sender-velocity-count');
    const receiverVelocityCountEl = document.getElementById('rule-receiver-velocity-count');

    const highAmountCount = rulesStats && rulesStats['HIGH_AMOUNT'] !== undefined ? rulesStats['HIGH_AMOUNT'] : 0;
    const senderVelocityCount = rulesStats && rulesStats['SENDER_VELOCITY'] !== undefined ? rulesStats['SENDER_VELOCITY'] : 0;
    const receiverVelocityCount = rulesStats && rulesStats['RECEIVER_VELOCITY'] !== undefined ? rulesStats['RECEIVER_VELOCITY'] : 0;

    if (highAmountCountEl) highAmountCountEl.textContent = highAmountCount;
    if (senderVelocityCountEl) senderVelocityCountEl.textContent = senderVelocityCount;
    if (receiverVelocityCountEl) receiverVelocityCountEl.textContent = receiverVelocityCount;
}

/**
 * Section 5: Pending Reviews (Transactions waiting for manual review)
 */
function renderPendingReviews(reviews) {
    const tbody = document.getElementById('pending-reviews-table-body');
    if (!tbody) return;

    if (!reviews || reviews.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="dash-empty">No transactions currently pending review. Safe state.</td></tr>';
        return;
    }

    const items = reviews.slice(0, 5);

    tbody.innerHTML = items.map(rev => {
        const tx = rev.transaction || {};
        const rulesList = (rev.triggeredRules || []).map(r => escapeHtml(r.type || r.name)).join(', ') || 'Rule Matched';

        return `
            <tr>
                <td class="font-mono">#${tx.id || rev.id}</td>
                <td class="font-mono" style="font-size: 0.82rem;">${escapeHtml(tx.sender || '—')} &rarr; ${escapeHtml(tx.receiver || '—')}</td>
                <td class="font-mono" style="font-weight: 700;">${formatCurrency(tx.amount)}</td>
                <td style="font-size: 0.8rem; color: var(--navy-muted);">${rulesList}</td>
                <td>
                    <a href="reviews.html" class="btn-navy btn-sm" style="text-decoration: none;">Review</a>
                </td>
            </tr>
        `;
    }).join('');
}
