import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

const oktaAuth = new OktaAuth({
    issuer: 'https://integrator-7447834.okta.com/oauth2/default',
    clientId: '0oaw3nzfn0RvOt7cc697',
    redirectUri: window.location.origin + '/index.html',
    scopes: ['openid', 'profile', 'email'],
    responseType: ['code'],
    pkce: true,
    tokenManager: { storage: 'localStorage' }
});


function redirectToLogin() {
    window.location.href = '/index.html';
}

export async function requireAuth() {
    if (!(await oktaAuth.isAuthenticated())) {
        redirectToLogin();
        return false;
    }
    return true;
}


async function makeAuthenticatedRequest(url, options = {}) {
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


/**
 * Fetches single coach and modifies the DOM tree in order to display it.
 */
export async function fetchCoach() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/coaches/' + getParameterById('coach'));
        if (response?.ok) {
            const coach = await response.json();
            displayCoach(coach);
        } else {
            console.warn('Fetch coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching coach:', error);
    }
}

/**
 * Updates the DOM tree in order to display coach.
 *
 * @param {string} coach
 */
function displayCoach(coach) {
    for (const [key, value] of Object.entries(coach)) {
        let input = document.getElementById(key);
        if (input) {
            input.value = value;
        }
    }
}


/**
 * Action event handled for creating entity.
 *
 * @param {Event} event dom event
 * @param {string} url link url
 * @param {object} request body of the request
 * @param {string} redirectUrl link to redirect when entity is created
 */
export async function createEntity(event, url, request, redirectUrl) {
    event.preventDefault();

    try {
        const response = await authenticatedPost(url, request);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage);
            window.location.href = redirectUrl;
        } else {
            console.warn('Create entity failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error creating entity:', error);
    }
}


/**
 * Action event handled for updating entity info.
 *
 * @param {Event} event dom event
 * @param {string} url link url
 * @param {string} redirectUrl link to redirect when entity is created
 */
export async function updateEntity(event, url, redirectUrl) {
    event.preventDefault();

    try {
        const response = await authenticatedPut(url);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage);
            window.location.href = redirectUrl;
        } else {
            console.warn('Update entity failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error updating entity:', error);
    }
}
