/**
 * FraudWatch - Rules Management
 * Strict Two-Color Minimalist Architecture: Navy (#0F172A) & White (#FFFFFF)
 */

let allRules = [];
let currentEditingRuleId = null;

document.addEventListener("DOMContentLoaded", () => {
    initRulesPage();
});

function initRulesPage() {
    loadRules();

    // Event listeners
    const createBtn = document.getElementById("createRuleBtn");
    if (createBtn) {
        createBtn.addEventListener("click", () => openRuleModal());
    }

    const ruleTypeSelect = document.getElementById("ruleType");
    if (ruleTypeSelect) {
        ruleTypeSelect.addEventListener("change", handleRuleTypeChange);
    }

    const ruleForm = document.getElementById("ruleForm");
    if (ruleForm) {
        ruleForm.addEventListener("submit", handleSaveRule);
    }

    const filterType = document.getElementById("filterRuleType");
    if (filterType) {
        filterType.addEventListener("change", renderRulesTable);
    }

    const filterStatus = document.getElementById("filterRuleStatus");
    if (filterStatus) {
        filterStatus.addEventListener("change", renderRulesTable);
    }

    const searchInput = document.getElementById("searchRuleInput");
    if (searchInput) {
        searchInput.addEventListener("input", debounce(renderRulesTable, 250));
    }

    const urlParams = new URLSearchParams(window.location.search);
    const directRuleId = urlParams.get("id");
    if (directRuleId) {
        checkDirectRule(directRuleId);
    }
}

async function checkDirectRule(id) {
    try {
        const res = await RuleAPI.getRuleById(id);
        if (res && res.message) {
            showToast(res.message, "warning");
            const banner = document.getElementById("ruleAlertBanner");
            if (banner) {
                banner.style.display = "block";
                banner.textContent = res.message;
            }
        } else if (res && res.id) {
            openRuleModal(res);
        }
    } catch (err) {
        showToast(err.message, "error");
    }
}

/**
 * Fetch all rules from backend
 */
async function loadRules() {
    const tableBody = document.getElementById("rulesTableBody");
    if (tableBody) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 24px;">Loading detection rules...</td></tr>`;
    }

    try {
        allRules = await RuleAPI.getAllRules();
        renderRulesTable();
        updateRulesSummaryCards();
    } catch (err) {
        console.error("Failed to load rules:", err);
        showToast("Error loading rules: " + err.message, "error");
        if (tableBody) {
            tableBody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 24px;">Failed to load rules from server.</td></tr>`;
        }
    }
}

/**
 * Render filtered rules into table
 */
function renderRulesTable() {
    const tableBody = document.getElementById("rulesTableBody");
    if (!tableBody) return;

    const filterType = document.getElementById("filterRuleType")?.value || "ALL";
    const filterStatus = document.getElementById("filterRuleStatus")?.value || "ALL";
    const query = document.getElementById("searchRuleInput")?.value?.trim().toLowerCase() || "";

    const filtered = allRules.filter(r => {
        if (filterType !== "ALL" && r.type !== filterType) return false;
        if (filterStatus === "ACTIVE" && !r.active) return false;
        if (filterStatus === "INACTIVE" && r.active) return false;
        if (query) {
            const nameMatch = r.name && r.name.toLowerCase().includes(query);
            const typeMatch = r.type && r.type.toLowerCase().includes(query);
            if (!nameMatch && !typeMatch) return false;
        }
        return true;
    });

    if (filtered.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 24px; color: var(--navy-60);">No rules match the selected criteria.</td></tr>`;
        return;
    }

    tableBody.innerHTML = filtered.map(rule => {
        let conditionHtml = "";
        if (rule.type === "HIGH_AMOUNT") {
            conditionHtml = `Threshold: <strong>${formatCurrency(rule.amountThreshold)}</strong>`;
        } else if (rule.type === "VELOCITY") {
            conditionHtml = `Max <strong>${rule.transactionCount}</strong> txns in <strong>${rule.timeWindowMinutes}</strong> mins`;
        } else {
            conditionHtml = `—`;
        }

        const statusBadge = rule.active 
            ? `<span class="status-badge">[ ACTIVE ]</span>`
            : `<span class="status-badge" style="border-style: dashed; color: var(--navy-60);">[ DISABLED ]</span>`;

        const actionButtons = `
            <div style="display: flex; gap: 6px; justify-content: flex-end;">
                <button class="btn btn-secondary btn-sm" onclick="editRule(${rule.id})">
                    Edit
                </button>
                ${rule.active ? `
                    <button class="btn btn-secondary btn-sm" onclick="toggleRuleActive(${rule.id}, false)">
                        Disable
                    </button>
                ` : `
                    <button class="btn btn-primary btn-sm" onclick="toggleRuleActive(${rule.id}, true)">
                        Enable
                    </button>
                `}
                <button class="btn btn-secondary btn-sm" onclick="confirmDeleteRule(${rule.id}, '${escapeHtml(rule.name)}')">
                    Delete
                </button>
            </div>
        `;

        return `
            <tr>
                <td class="font-mono" style="color: var(--navy-60);">#${rule.id}</td>
                <td><strong>${escapeHtml(rule.name)}</strong></td>
                <td>${getRuleTypeBadge(rule.type)}</td>
                <td style="font-size: 0.85rem;">${conditionHtml}</td>
                <td>${statusBadge}</td>
                <td style="color: var(--navy-60); font-size: 0.82rem;">${rule.createdAt ? formatDate(rule.createdAt) : "—"}</td>
                <td class="text-right">${actionButtons}</td>
            </tr>
        `;
    }).join("");
}

/**
 * Update top KPI summary cards for rules
 */
function updateRulesSummaryCards() {
    const totalEl = document.getElementById("totalRulesCount");
    const activeEl = document.getElementById("activeRulesCount");
    const highAmountEl = document.getElementById("highAmountRulesCount");
    const velocityEl = document.getElementById("velocityRulesCount");

    if (totalEl) totalEl.textContent = allRules.length;
    if (activeEl) activeEl.textContent = allRules.filter(r => r.active).length;
    if (highAmountEl) highAmountEl.textContent = allRules.filter(r => r.type === "HIGH_AMOUNT" && r.active).length;
    if (velocityEl) velocityEl.textContent = allRules.filter(r => r.type === "VELOCITY" && r.active).length;
}

/**
 * Handle type switch in rule modal (HIGH_AMOUNT vs VELOCITY)
 */
function handleRuleTypeChange() {
    const type = document.getElementById("ruleType")?.value;
    const highAmountGroup = document.getElementById("highAmountGroup");
    const velocityGroup = document.getElementById("velocityGroup");
    const thresholdInput = document.getElementById("ruleAmountThreshold");
    const countInput = document.getElementById("ruleTransactionCount");
    const windowInput = document.getElementById("ruleTimeWindow");

    if (type === "HIGH_AMOUNT") {
        if (highAmountGroup) highAmountGroup.style.display = "block";
        if (velocityGroup) velocityGroup.style.display = "none";
        if (thresholdInput) thresholdInput.required = true;
        if (countInput) countInput.required = false;
        if (windowInput) windowInput.required = false;
    } else if (type === "VELOCITY") {
        if (highAmountGroup) highAmountGroup.style.display = "none";
        if (velocityGroup) velocityGroup.style.display = "block";
        if (thresholdInput) thresholdInput.required = false;
        if (countInput) countInput.required = true;
        if (windowInput) windowInput.required = true;
    }
}

/**
 * Open create or edit rule modal
 */
function openRuleModal(rule = null) {
    currentEditingRuleId = rule ? rule.id : null;
    const modalTitle = document.getElementById("ruleModalTitle");
    const submitBtn = document.getElementById("ruleSubmitBtn");
    const form = document.getElementById("ruleForm");

    if (form) form.reset();

    if (rule) {
        if (modalTitle) modalTitle.textContent = "Edit Detection Rule";
        if (submitBtn) submitBtn.textContent = "Update Rule";

        document.getElementById("ruleName").value = rule.name || "";
        document.getElementById("ruleType").value = rule.type;
        document.getElementById("ruleActive").checked = !!rule.active;

        if (rule.type === "HIGH_AMOUNT") {
            document.getElementById("ruleAmountThreshold").value = rule.amountThreshold || "";
        } else if (rule.type === "VELOCITY") {
            document.getElementById("ruleTransactionCount").value = rule.transactionCount || "";
            document.getElementById("ruleTimeWindow").value = rule.timeWindowMinutes || "";
        }
    } else {
        if (modalTitle) modalTitle.textContent = "Create Detection Rule";
        if (submitBtn) submitBtn.textContent = "Create Rule";
        document.getElementById("ruleType").value = "HIGH_AMOUNT";
        document.getElementById("ruleActive").checked = true;
    }

    handleRuleTypeChange();
    openModal("ruleModal");
}

async function editRule(id) {
    try {
        const rule = await RuleAPI.getRuleById(id);
        if (rule && rule.message) {
            // Rule was soft-deleted
            showToast(rule.message, "warning");
            const banner = document.getElementById("ruleAlertBanner");
            if (banner) {
                banner.style.display = "block";
                banner.textContent = rule.message;
            }
            await loadRules();
            return;
        }
        if (!rule) {
            showToast("Rule not found", "error");
            return;
        }
        openRuleModal(rule);
    } catch (err) {
        console.error("Edit rule error:", err);
        showToast("Error retrieving rule: " + err.message, "error");
    }
}

async function handleSaveRule(e) {
    e.preventDefault();
    const submitBtn = document.getElementById("ruleSubmitBtn");
    const originalText = submitBtn ? submitBtn.textContent : "Save";

    const name = document.getElementById("ruleName")?.value.trim();
    const type = document.getElementById("ruleType")?.value;
    const active = document.getElementById("ruleActive")?.checked ?? true;

    if (!name) {
        showToast("Please provide a rule name", "warning");
        return;
    }

    const payload = {
        name,
        type,
        active
    };

    if (type === "HIGH_AMOUNT") {
        const threshold = parseFloat(document.getElementById("ruleAmountThreshold")?.value);
        if (isNaN(threshold) || threshold <= 0) {
            showToast("Please enter a valid positive amount threshold", "warning");
            return;
        }
        payload.amountThreshold = threshold;
    } else if (type === "VELOCITY") {
        const count = parseInt(document.getElementById("ruleTransactionCount")?.value, 10);
        const windowMins = parseInt(document.getElementById("ruleTimeWindow")?.value, 10);

        if (isNaN(count) || count <= 0) {
            showToast("Please enter a valid transaction count threshold", "warning");
            return;
        }
        if (isNaN(windowMins) || windowMins <= 0) {
            showToast("Please enter a valid time window in minutes", "warning");
            return;
        }
        payload.transactionCount = count;
        payload.timeWindowMinutes = windowMins;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = "Saving...";
    }

    try {
        if (currentEditingRuleId) {
            await RuleAPI.updateRule(currentEditingRuleId, payload);
            showToast(`Rule #${currentEditingRuleId} updated successfully`, "success");
        } else {
            const created = await RuleAPI.createRule(payload);
            showToast(`Rule #${created.id} created successfully`, "success");
        }
        closeModal("ruleModal");
        await loadRules();
    } catch (err) {
        console.error("Save rule error:", err);
        showToast("Failed to save rule: " + err.message, "error");
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    }
}

async function toggleRuleActive(id, makeActive) {
    try {
        if (!makeActive) {
            await RuleAPI.disableRule(id);
            showToast(`Rule #${id} disabled`, "info");
        } else {
            await RuleAPI.enableRule(id);
            showToast(`Rule #${id} enabled`, "success");
        }
        await loadRules();
    } catch (err) {
        console.error("Failed to toggle rule status:", err);
        showToast("Failed to update rule: " + err.message, "error");
    }
}

async function confirmDeleteRule(id, name) {
    if (!confirm(`Are you sure you want to delete rule #${id} ("${name}")?\n\nThis will soft-delete the rule. Historical fraud detection records referencing this rule will be preserved.`)) {
        return;
    }
    try {
        const res = await RuleAPI.deleteRule(id);
        const msg = (res && res.message) ? res.message : `Rule with id ${id} was deleted successfully`;
        showToast(msg, "success");
        await loadRules();
    } catch (err) {
        console.error("Failed to delete rule:", err);
        showToast("Failed to delete rule: " + err.message, "error");
    }
}

