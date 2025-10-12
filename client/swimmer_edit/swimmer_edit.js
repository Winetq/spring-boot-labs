import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedGet, authenticatedPut} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const updateSwimmerForm = document.getElementById('updateSwimmerForm');
        updateSwimmerForm.addEventListener('submit', event => updateSwimmer(event));
        await fetchAndDisplaySwimmer();
    }
});

/**
 * Fetches currently chosen coach's swimmer and updates edit form.
 */
async function fetchAndDisplaySwimmer() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/swimmers/' + getParameterById('swimmer'));
        if (response?.ok) {
            const swimmer = await response.json();
            for (const [key, value] of Object.entries(swimmer)) {
                let input = document.getElementById(key);
                if (input) {
                    input.value = value;
                }
            }
        } else {
            console.warn('Fetch and display swimmer failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching and displaying swimmer:', error);
    }
}

/**
 * Action event handled for updating swimmer info.
 *
 * @param {Event} event dom event
 */
async function updateSwimmer(event) {
    event.preventDefault();

    try {
        const response = await authenticatedPut(getBackendUrl() + '/swimmers/' + getParameterById('swimmer') +
            '?specialization=' + document.getElementById('specialization').value);
        if (response?.ok) {
            const successfulMessage = await response.text();
            alert(successfulMessage)
        } else {
            console.warn('Update swimmer failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error updating swimmer:', error);
    }
}
