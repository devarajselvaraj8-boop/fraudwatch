// FraudWatch - Manual Reviews Clearance Logic
// Strict Two-Color Minimalist Architecture: Navy (#0F172A) & White (#FFFFFF)

let pendingReviews = [];
let activeReviewId = null;
let activeActionType = null; // 'APPROVE' or 'BLOCK'

document.addEventListener('DOMContentLoaded', () => {
    loadPendingReviews();

    const refreshBtn = document.getElementById('refresh-reviews-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadPendingReviews);
    }

    const actionForm = document.getElementById('review-action-form');
    if (actionForm) {
        actionForm.addEventListener('submit', handleExecuteReviewAction);
    }
});

async function loadPendingReviews() {
    const loadingEl = document.getElementById('reviews-loading');
    const listEl = document.getElementById('reviews-list');
    const emptyEl = document.getElementById('reviews-empty');
    const errorEl = document.getElementById('reviews-error');

    if (loadingEl) loadingEl.style.display = 'flex';
    if (listEl) listEl.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';

    try {
        const data = await getPendingReviews();
        pendingReviews = Array.isArray(data) ? [...data].sort((a, b) => b.id - a.id) : [];

        if (loadingEl) loadingEl.style.display = 'none';

        if (pendingReviews.length === 0) {
            if (emptyEl) emptyEl.style.display = 'block';
        } else {
            if (listEl) listEl.style.display = 'grid';
            renderReviewsList(pendingReviews);
        }
    } catch (error) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            const msgEl = document.getElementById('reviews-error-msg');
            if (msgEl) msgEl.textContent = error.message;
        }
        showToast(error.message, 'error');
    }
}

function renderReviewsList(reviews) {
    const listEl = document.getElementById('reviews-list');
    if (!listEl) return;

    listEl.innerHTML = reviews.map(rev => {
        const tx = rev.transaction || {};
        const ruleBadges = (rev.triggeredRules || []).map(r => `
            <span class="rule-chip font-mono">${escapeHtml(r.name || r.type)} [${escapeHtml(r.type || '')}]</span>
        `).join('');

        return `
            <div class="review-card">
                <div class="review-header">
                    <div>
                        <span class="font-mono" style="font-weight: 700;">Review #${rev.id}</span>
                        <span class="font-mono text-muted" style="margin-left: 8px;">Tx #${tx.id}</span>
                    </div>
                    <div>
                        ${getStatusBadgeHtml(rev.reviewStatus)}
                    </div>
                </div>

                <div class="review-parties">
                    <span>${escapeHtml(tx.sender || '—')}</span>
                    <span style="color: var(--navy-40);">&rarr;</span>
                    <span>${escapeHtml(tx.receiver || '—')}</span>
                </div>

                <div class="review-amount font-mono">
                    ${formatCurrency(tx.amount)}
                </div>

                <div class="text-sm" style="color: var(--navy-60);">
                    Flagged: ${formatDateTime(rev.flaggedAt)}
                </div>

                <div class="review-rules-box">
                    <div style="font-size: 0.72rem; text-transform: uppercase; font-weight: 700; color: var(--navy-60); margin-bottom: 6px;">Triggered Rules:</div>
                    <div>${ruleBadges || '<span class="text-muted">No rules listed</span>'}</div>
                </div>

                <div class="review-actions">
                    <button class="btn btn-secondary btn-sm" onclick="viewReviewDetails(${rev.id})">
                        View Details
                    </button>
                    <button class="btn btn-primary btn-sm" onclick="openReviewActionModal(${rev.id}, 'APPROVE')">
                        Approve
                    </button>
                    <button class="btn btn-secondary btn-sm" onclick="openReviewActionModal(${rev.id}, 'BLOCK')">
                        Block
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function openReviewActionModal(id, actionType) {
    const rev = pendingReviews.find(r => r.id === id);
    if (!rev) return;

    activeReviewId = id;
    activeActionType = actionType;

    const modalTitle = document.getElementById('action-modal-title');
    const modalSubtitle = document.getElementById('action-modal-subtitle');
    const confirmBtn = document.getElementById('action-modal-confirm-btn');
    const commentInput = document.getElementById('action-comment');
    const reviewerInput = document.getElementById('action-reviewer');

    if (reviewerInput && !reviewerInput.value) {
        reviewerInput.value = 'Compliance Officer';
    }

    if (actionType === 'APPROVE') {
        modalTitle.textContent = 'Confirm Approval';
        modalSubtitle.textContent = `Transaction #${rev.transaction.id} (${formatCurrency(rev.transaction.amount)}) will be approved and marked COMPLETED.`;
        confirmBtn.textContent = 'Approve Transaction';
        if (commentInput) commentInput.value = 'Verified legitimate customer activity after manual review';
    } else {
        modalTitle.textContent = 'Confirm Block';
        modalSubtitle.textContent = `Transaction #${rev.transaction.id} (${formatCurrency(rev.transaction.amount)}) will be permanently blocked.`;
        confirmBtn.textContent = 'Block Transaction';
        if (commentInput) commentInput.value = 'Confirmed high-risk suspicious anomaly';
    }

    confirmBtn.className = 'btn btn-primary';

    openModal('review-action-modal');
}

async function handleExecuteReviewAction(e) {
    e.preventDefault();

    if (!activeReviewId || !activeActionType) return;

    const reviewer = document.getElementById('action-reviewer').value.trim();
    const comment = document.getElementById('action-comment').value.trim();
    const confirmBtn = document.getElementById('action-modal-confirm-btn');

    if (!reviewer) {
        showToast('Reviewer identity is required.', 'error');
        return;
    }

    const payload = { reviewer, comment };

    try {
        if (confirmBtn) {
            confirmBtn.disabled = true;
            confirmBtn.textContent = 'Processing...';
        }

        if (activeActionType === 'APPROVE') {
            await approveReview(activeReviewId, payload);
            showToast('Review approved. Transaction marked COMPLETED.', 'success');
        } else {
            await blockReview(activeReviewId, payload);
            showToast('Transaction blocked permanently.', 'success');
        }

        closeModal('review-action-modal');
        activeReviewId = null;
        activeActionType = null;

        await loadPendingReviews();
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (confirmBtn) {
            confirmBtn.disabled = false;
        }
    }
}

function viewReviewDetails(id) {
    const rev = pendingReviews.find(r => r.id === id);
    if (!rev) return;

    document.getElementById('rd-review-id').textContent = '#' + rev.id;
    document.getElementById('rd-tx-id').textContent = '#' + rev.transaction.id;
    document.getElementById('rd-sender').textContent = rev.transaction.sender;
    document.getElementById('rd-receiver').textContent = rev.transaction.receiver;
    document.getElementById('rd-amount').textContent = formatCurrency(rev.transaction.amount);
    document.getElementById('rd-flagged-time').textContent = formatDateTime(rev.flaggedAt);
    document.getElementById('rd-status').innerHTML = getStatusBadgeHtml(rev.reviewStatus);

    const rulesContainer = document.getElementById('rd-rules-list');
    if (rulesContainer) {
        rulesContainer.innerHTML = rev.triggeredRules.map(r => `
            <div class="dash-rule-card" style="margin-bottom: 8px;">
                <div>
                    <div class="font-mono" style="font-weight: 700;">${escapeHtml(r.name)} [${escapeHtml(r.type)}]</div>
                    <div class="text-sm" style="color: var(--navy-60); margin-top: 2px;">
                        ${r.type === 'HIGH_AMOUNT' ? `Threshold: ${formatCurrency(r.amountThreshold)}` : ''}
                        ${r.type === 'SENDER_VELOCITY' ? `Sender Limit: ${r.transactionCount} transactions within ${r.timeWindowMinutes} minutes` : ''}
                        ${r.type === 'RECEIVER_VELOCITY' ? `Receiver Limit: ${r.transactionCount} transactions within ${r.timeWindowMinutes} minutes` : ''}
                        ${r.type === 'VELOCITY' ? `Limit: ${r.transactionCount} transactions within ${r.timeWindowMinutes} minutes` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    }

    openModal('review-detail-modal');
}
