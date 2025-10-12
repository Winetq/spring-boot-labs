import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, fetchCoach, authenticatedPut} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const updateCoachForm = document.getElementById('updateCoachForm');
        updateCoachForm.addEventListener('submit', event => updateCoach(event));
        await fetchCoach();
    }
});

/**
 * Action event handled for updating coach info.
 *
 * @param {Event} event dom event
 */
async function updateCoach(event) {
    event.preventDefault();

    try {
        let url = getBackendUrl() + '/coaches/' + getParameterById('coach') + '?level=' + document.getElementById('level').value;
        const response = await authenticatedPut(url);
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
