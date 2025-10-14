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

async function authenticatedGet(url) {
    return makeAuthenticatedRequest(url, { method: 'GET' });
}

async function authenticatedPost(url, data) {
    return makeAuthenticatedRequest(url, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function authenticatedPut(url, data) {
    return makeAuthenticatedRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

async function authenticatedDelete(url) {
    return makeAuthenticatedRequest(url, { method: 'DELETE' });
}


/**
 * Fetches single entity and modifies the DOM tree in order to display it.
 *
 * @param {string} url link url
 */
export async function fetchEntity(url) {
    try {
        const response = await authenticatedGet(url);
        if (response?.ok) {
            const entity = await response.json();
            displayEntity(entity);
        } else {
            console.warn('Fetch entity failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching entity:', error);
    }
}

/**
 * Updates the DOM tree in order to display entity.
 *
 * @param {string} entity
 */
function displayEntity(entity) {
    for (const [key, value] of Object.entries(entity)) {
        let input = document.getElementById(key);
        if (input) {
            input.value = value;
        }
    }
}


/**
 * Fetches all entities and modifies the DOM tree in order to display them.
 *
 * @param {string} url link url
 * @param {function} displayEntities function that updates the DOM tree in order to display entities
 */
export async function fetchEntities(url, displayEntities) {
    try {
        const response = await authenticatedGet(url);
        if (response?.ok) {
            const entities = await response.json();
            displayEntities(entities);
        } else {
            console.warn('Fetch entities failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching entities:', error);
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


// TODO: allow to delete entity only if user has an appropriate role
/**
 * Deletes entity from backend and reloads table.
 *
 * @param {string} urlToDelete link url to delete
 * @param {string} urlToFetch link url to fetch
 * @param {function} displayEntities function that updates the DOM tree in order to display entities
 */
export async function deleteEntity(urlToDelete, urlToFetch, displayEntities) {
    try {
        const response = await authenticatedDelete(urlToDelete);
        if (response?.ok) {
            await fetchEntities(urlToFetch, displayEntities);
        } else {
            console.warn('Delete entity failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error deleting entity:', error);
    }
}
