import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedPost} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const createSwimmerForm = document.getElementById('createSwimmerForm');
        createSwimmerForm.addEventListener('submit', event => createSwimmer(event));
    }
});

function buildSwimmerRequest() {
    return {
        coach_id: getParameterById('coach'),
        name: document.getElementById('name').value,
        specialization: document.getElementById('specialization').value
    };
}

/**
 * Action event handled for creating swimmer.
 *
 * @param {Event} event dom event
 */
async function createSwimmer(event) {
    event.preventDefault();

    try {
        const request = buildSwimmerRequest();
        const response = await authenticatedPost(getBackendUrl() + '/swimmers/with_coach', request);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage);
        } else {
            console.warn('Create swimmer failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error creating swimmer:', error);
    }
}
