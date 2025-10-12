import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedGet, authenticatedPut} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const updateCoachForm = document.getElementById('updateCoachForm');
        updateCoachForm.addEventListener('submit', event => updateCoach(event));
        await fetchAndDisplayCoach();
    }
});

/**
 * Fetches currently chosen coach and updates edit form.
 */
async function fetchAndDisplayCoach() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/coaches/' + getParameterById('coach'));
        if (response?.ok) {
            const coach = await response.json();
            for (const [key, value] of Object.entries(coach)) {
                let input = document.getElementById(key);
                if (input) {
                    input.value = value;
                }
            }
        } else {
            console.warn('Fetch and display coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching and displaying coach:', error);
    }
}

/**
 * Action event handled for updating coach info.
 *
 * @param {Event} event dom event
 */
async function updateCoach(event) {
    event.preventDefault();

    try {
        const response = await authenticatedPut(getBackendUrl() + '/coaches/' + getParameterById('coach') +
            '?level=' + document.getElementById('level').value);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage)
        } else {
            console.warn('Update coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error updating coach:', error);
    }
}
