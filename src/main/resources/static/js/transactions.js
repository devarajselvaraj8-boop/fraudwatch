// FraudWatch - Transactions Page Logic

let allTransactions = [];

document.addEventListener('DOMContentLoaded', () => {
    loadTransactions();

    // Search and filter listeners
    const searchInput = document.getElementById('tx-search');
    if (searchInput) {
        searchInput.addEventListener('input', applyFilters);
    }

    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            filterButtons.forEach(b => b.classList.remove('active'));
            e.currentTarget.classList.add('active');
            applyFilters();
        });
    });

    // Form submit listener
    const createForm = document.getElementById('create-tx-form');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateTransaction);
    }

    const editForm = document.getElementById('edit-tx-form');
    if (editForm) {
        editForm.addEventListener('submit', handleUpdateTransaction);
    }

    const refreshBtn = document.getElementById('refresh-tx-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadTransactions);
    }
});

async function loadTransactions() {
    const loadingEl = document.getElementById('tx-loading');
    const tableEl = document.getElementById('tx-table-container');
    const emptyEl = document.getElementById('tx-empty');
    const errorEl = document.getElementById('tx-error');

    if (loadingEl) loadingEl.style.display = 'flex';
    if (tableEl) tableEl.style.display = 'none';
    if (emptyEl) emptyEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';

    try {
        const data = await getTransactions();
        allTransactions = Array.isArray(data) ? [...data].sort((a, b) => b.id - a.id) : [];

        if (loadingEl) loadingEl.style.display = 'none';

        if (allTransactions.length === 0) {
            if (emptyEl) emptyEl.style.display = 'block';
        } else {
            if (tableEl) tableEl.style.display = 'block';
            applyFilters();
        }
    } catch (error) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            const msgEl = document.getElementById('tx-error-msg');
            if (msgEl) msgEl.textContent = error.message;
        }
        showToast(error.message, 'error');
    }
}

function applyFilters() {
    const searchVal = (document.getElementById('tx-search')?.value || '').toLowerCase().trim();
    const activeBtn = document.querySelector('.filter-btn.active');
    const statusVal = activeBtn ? activeBtn.getAttribute('data-status') : 'ALL';

    const filtered = allTransactions.filter(tx => {
        const matchesSearch =
            tx.id.toString().includes(searchVal) ||
            tx.sender.toLowerCase().includes(searchVal) ||
            tx.receiver.toLowerCase().includes(searchVal);

        const matchesStatus = statusVal === 'ALL' || tx.status === statusVal;

        return matchesSearch && matchesStatus;
    });

    renderTransactionsTable(filtered);
}

function renderTransactionsTable(transactions) {
    const tbody = document.getElementById('tx-table-body');
    const emptyEl = document.getElementById('tx-empty');
    const tableEl = document.getElementById('tx-table-container');

    if (!tbody) return;

    if (transactions.length === 0) {
        if (tableEl) tableEl.style.display = 'none';
        if (emptyEl) {
            emptyEl.style.display = 'block';
            emptyEl.querySelector('p').textContent = 'No transactions matched the search or filter criteria.';
        }
        return;
    }

    if (tableEl) tableEl.style.display = 'block';
    if (emptyEl) emptyEl.style.display = 'none';

    tbody.innerHTML = transactions.map(tx => `
        <tr>
            <td class="font-mono text-muted">#${tx.id}</td>
            <td class="font-mono font-bold">${escapeHtml(tx.sender)}</td>
            <td class="font-mono font-bold">${escapeHtml(tx.receiver)}</td>
            <td class="font-mono font-bold">${formatCurrency(tx.amount)}</td>
            <td class="text-muted text-sm">${formatDateTime(tx.timestamp)}</td>
            <td>${getStatusBadgeHtml(tx.status)}</td>
            <td class="text-right">
                <div style="display: flex; gap: 6px; justify-content: flex-end;">
                    <button class="btn btn-sm btn-secondary" onclick="viewTransactionDetails(${tx.id})">
                        View
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="openEditTxModal(${tx.id})">
                        Edit
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="confirmDeleteTransaction(${tx.id})">
                        Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function handleCreateTransaction(e) {
    e.preventDefault();

    const sender = document.getElementById('tx-sender').value.trim();
    const receiver = document.getElementById('tx-receiver').value.trim();
    const amountVal = parseFloat(document.getElementById('tx-amount').value);
    const submitBtn = document.getElementById('submit-tx-btn');

    if (!sender) {
        showToast('Sender account is required.', 'error');
        return;
    }
    if (!receiver) {
        showToast('Receiver account is required.', 'error');
        return;
    }
    if (isNaN(amountVal) || amountVal <= 0) {
        showToast('Amount must be a positive number.', 'error');
        return;
    }

    try {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Processing...';
        }

        const payload = {
            sender,
            receiver,
            amount: amountVal
        };

        const result = await createTransaction(payload);

        closeModal('create-tx-modal');
        document.getElementById('create-tx-form').reset();

        // Message according to returned status as specified in Phase 12
        if (result.status === 'COMPLETED') {
            showToast('Transaction completed successfully.', 'success');
        } else if (result.status === 'FLAGGED') {
            showToast('Transaction flagged for manual review.', 'warning');
        } else if (result.status === 'BLOCKED') {
            showToast('Transaction blocked.', 'error');
        }

        await loadTransactions();
        if (typeof updatePendingBadge === 'function') {
            updatePendingBadge();
        }
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Transaction';
        }
    }
}

async function viewTransactionDetails(id) {
    const tx = allTransactions.find(t => t.id === id);
    if (!tx) return;

    document.getElementById('detail-tx-id').textContent = '#' + tx.id;
    document.getElementById('detail-tx-sender').textContent = tx.sender;
    document.getElementById('detail-tx-receiver').textContent = tx.receiver;
    document.getElementById('detail-tx-amount').textContent = formatCurrency(tx.amount);
    document.getElementById('detail-tx-time').textContent = formatDateTime(tx.timestamp);
    document.getElementById('detail-tx-status').innerHTML = getStatusBadgeHtml(tx.status);

    const flaggedSection = document.getElementById('detail-flagged-section');
    if (tx.status === 'FLAGGED' || tx.status === 'BLOCKED') {
        if (flaggedSection) {
            flaggedSection.style.display = 'block';
            flaggedSection.innerHTML = `
                <div class="alert-box alert-warning">
                    <strong>⚠️ FLAGGED &bull; Requires Manual Review</strong>
                    <p class="text-sm mt-1">This transaction matched active fraud surveillance rules and requires clearance by a compliance officer.</p>
                </div>
            `;

            // Query review details
            try {
                const reviews = await getAllReviews();
                const review = reviews.find(r => r.transaction && r.transaction.id === tx.id);
                if (review) {
                    flaggedSection.innerHTML += `
                        <div class="detail-audit-box mt-3">
                            <div class="detail-row">
                                <span class="detail-label">Review Status:</span>
                                <span>${escapeHtml(review.reviewStatus)}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Flagged At:</span>
                                <span>${formatDateTime(review.flaggedAt)}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Triggered Rules:</span>
                                <div class="rule-chips">
                                    ${review.triggeredRules.map(r => `<span class="rule-chip">${escapeHtml(r.name)} [${escapeHtml(r.type)}]</span>`).join('')}
                                </div>
                            </div>
                            ${review.reviewer ? `
                            <div class="detail-row">
                                <span class="detail-label">Reviewed By:</span>
                                <span>${escapeHtml(review.reviewer)} (${escapeHtml(review.comment || 'No comment')})</span>
                            </div>` : ''}
                        </div>
                    `;
                }
            } catch {
                // Silently ignore
            }
        }
    } else {
        if (flaggedSection) flaggedSection.style.display = 'none';
    }

    openModal('detail-tx-modal');
}

async function openEditTxModal(id) {
    try {
        const tx = await TransactionAPI.getTransactionById(id);
        if (tx && tx.message) {
            showToast(tx.message, 'warning');
            await loadTransactions();
            return;
        }
        if (!tx) {
            showToast('Transaction not found', 'error');
            return;
        }

        document.getElementById('edit-tx-id').value = tx.id;
        document.getElementById('edit-tx-id-badge').textContent = '#' + tx.id;
        document.getElementById('edit-tx-sender').value = tx.sender;
        document.getElementById('edit-tx-receiver').value = tx.receiver;
        document.getElementById('edit-tx-amount').value = tx.amount;

        openModal('edit-tx-modal');
    } catch (err) {
        showToast('Error loading transaction: ' + err.message, 'error');
    }
}

async function handleUpdateTransaction(e) {
    e.preventDefault();

    const id = document.getElementById('edit-tx-id').value;
    const sender = document.getElementById('edit-tx-sender').value.trim();
    const receiver = document.getElementById('edit-tx-receiver').value.trim();
    const amountVal = parseFloat(document.getElementById('edit-tx-amount').value);
    const saveBtn = document.getElementById('save-tx-btn');

    if (!sender) {
        showToast('Sender account is required.', 'error');
        return;
    }
    if (!receiver) {
        showToast('Receiver account is required.', 'error');
        return;
    }
    if (isNaN(amountVal) || amountVal <= 0) {
        showToast('Amount must be a positive number.', 'error');
        return;
    }

    try {
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.textContent = 'Saving...';
        }

        const payload = {
            sender,
            receiver,
            amount: amountVal
        };

        const result = await TransactionAPI.updateTransaction(id, payload);

        closeModal('edit-tx-modal');
        showToast(`Transaction #${id} updated successfully. Status: ${result.status}`, 'success');

        await loadTransactions();
        if (typeof updatePendingBadge === 'function') {
            updatePendingBadge();
        }
    } catch (error) {
        showToast('Update failed: ' + error.message, 'error');
    } finally {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.textContent = 'Update Transaction';
        }
    }
}

async function confirmDeleteTransaction(id) {
    if (!confirm(`Are you sure you want to delete transaction ${id}?`)) {
        return;
    }

    try {
        const res = await TransactionAPI.deleteTransaction(id);
        const msg = (res && res.message) ? res.message : `Transaction with id ${id} was deleted successfully`;
        showToast(msg, 'success');
        await loadTransactions();
        if (typeof updatePendingBadge === 'function') {
            updatePendingBadge();
        }
    } catch (err) {
        showToast('Delete failed: ' + err.message, 'error');
    }
}

