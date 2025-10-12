import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedPost} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const createCoachForm = document.getElementById('createCoachForm');
        createCoachForm.addEventListener('submit', event => createCoach(event));
    }
});

function buildCoachRequest() {
    const nameValue = document.getElementById('name')?.value.trim() || '';
    const levelValue = document.getElementById('level')?.value.trim() || '';
    return {
        name: nameValue === '' ? 'testName' : nameValue,
        level: levelValue === '' ? '0' : levelValue
    };
}

/**
 * Action event handled for creating coach.
 *
 * @param {Event} event dom event
 */
async function createCoach(event) {
    event.preventDefault();

    try {
        const request = buildCoachRequest();
        const response = await authenticatedPost(getBackendUrl() + '/coaches', request);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage);
        } else {
            console.warn('Create coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error creating coach:', error);
    }
}
