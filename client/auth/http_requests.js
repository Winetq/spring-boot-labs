const oktaAuth = new OktaAuth({
    issuer: 'https://integrator-7447834.okta.com/oauth2/default',
    clientId: '0oaw3nzfn0RvOt7cc697',
    redirectUri: window.location.origin + '/index.html',
    scopes: ['openid', 'profile', 'email'],
    responseType: ['code'],
    pkce: true,
    tokenManager: { storage: 'localStorage' }
});

export function redirectToLogin() {
    window.location.href = '/index.html';
}

export async function requireAuth() {
    if (!(await oktaAuth.isAuthenticated())) {
        redirectToLogin();
        return false;
    }
    return true;
}

export async function makeAuthenticatedRequest(url, options = {}) {
    const accessToken = oktaAuth.getAccessToken();
    if (!accessToken) return null;

    const headers = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        ...options.headers
    };

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            console.error('Access Token expired or invalid:', accessToken);
            redirectToLogin();
            return null;
        }

        return response;
    } catch (error) {
        console.error('Request failed:', error);
        throw error;
    }
}

export async function authenticatedGet(url) {
    return makeAuthenticatedRequest(url, { method: 'GET' });
}

export async function authenticatedPost(url, data) {
    return makeAuthenticatedRequest(url, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

export async function authenticatedPut(url, data) {
    return makeAuthenticatedRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

export async function authenticatedDelete(url) {
    return makeAuthenticatedRequest(url, { method: 'DELETE' });
}
