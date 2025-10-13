import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedGet, updateEntity} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const updateSwimmerForm = document.getElementById('updateSwimmerForm');
        updateSwimmerForm.addEventListener('submit',
                event =>
                    updateEntity(
                        event,
                        getBackendUrl() + '/swimmers/' + getParameterById('swimmer') + '?specialization=' + document.getElementById('specialization').value,
                        '/coach_view/coach_view.html?coach=' + getParameterById('coach')
                    )
        );
        await fetchAndDisplaySwimmer();
    }
});

/**
 * Fetches currently chosen coach's swimmer and updates edit form.
 */
async function fetchAndDisplaySwimmer() {
    try { // a to moze xd uprosic do fetch coach XD
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
