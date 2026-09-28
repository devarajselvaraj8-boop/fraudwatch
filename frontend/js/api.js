// FraudWatch - Centralized API Service (Vanilla Fetch API)

async function request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...(options.headers || {})
        }
    };

    try {
        const response = await fetch(url, config);

        // Handle 204 No Content
        if (response.status === 204) {
            return null;
        }

        let data = null;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            let errorMessage = `HTTP Error ${response.status}`;
            if (data && typeof data === 'object' && data.message) {
                errorMessage = data.message;
            } else if (response.status === 400) {
                errorMessage = 'Invalid request parameters.';
            } else if (response.status === 404) {
                errorMessage = 'Requested resource not found.';
            } else if (response.status === 409) {
                errorMessage = 'Conflict: Item has already been processed.';
            } else if (response.status === 500) {
                errorMessage = 'Internal server error while processing request.';
            }
            throw new Error(errorMessage);
        }

        return data;
    } catch (error) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Unable to connect to FraudWatch backend. Please ensure the backend is running.');
        }
        throw error;
    }
}

// Transactions API
async function getTransactions() {
    return request('/api/transactions');
}

async function getTransaction(id) {
    return request(`/api/transactions/${id}`);
}

async function createTransaction(data) {
    return request('/api/transactions', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function updateTransaction(id, data) {
    return request(`/api/transactions/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

async function deleteTransaction(id) {
    return request(`/api/transactions/${id}`, {
        method: 'DELETE'
    });
}

// Rules API
async function getRules() {
    return request('/api/rules');
}

async function getRule(id) {
    return request(`/api/rules/${id}`);
}

async function createRule(data) {
    return request('/api/rules', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function updateRule(id, data) {
    return request(`/api/rules/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

async function deleteRule(id) {
    return request(`/api/rules/${id}`, {
        method: 'DELETE'
    });
}

async function enableRule(id) {
    return request(`/api/rules/${id}/enable`, {
        method: 'PUT'
    });
}

async function disableRule(id) {
    return request(`/api/rules/${id}/disable`, {
        method: 'PUT'
    });
}

// Reviews API
async function getPendingReviews() {
    return request('/api/reviews/pending');
}

async function getAllReviews(status = null) {
    const url = status && status !== 'ALL' ? `/api/reviews?status=${status}` : '/api/reviews';
    return request(url);
}

async function getReview(id) {
    return request(`/api/reviews/${id}`);
}

async function approveReview(id, data) {
    return request(`/api/reviews/${id}/approve`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function blockReview(id, data) {
    return request(`/api/reviews/${id}/block`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

// Dashboard API
async function getDashboard() {
    return request('/api/dashboard');
}

async function getDashboardRules() {
    return request('/api/dashboard/rules');
}

// Global API Namespaces
const TransactionAPI = {
    getAllTransactions: getTransactions,
    getTransactionById: getTransaction,
    createTransaction: createTransaction,
    updateTransaction: updateTransaction,
    deleteTransaction: deleteTransaction
};

const RuleAPI = {
    getAllRules: getRules,
    getRuleById: getRule,
    createRule: createRule,
    updateRule: updateRule,
    deleteRule: deleteRule,
    enableRule: enableRule,
    disableRule: disableRule
};

const ReviewAPI = {
    getPendingReviews: getPendingReviews,
    getAllReviews: getAllReviews,
    getReviewById: getReview,
    approveReview: approveReview,
    blockReview: blockReview
};

const DashboardAPI = {
    getDashboard: getDashboard,
    getDashboardRules: getDashboardRules
};

