import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, fetchCoach, updateEntity} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const updateCoachForm = document.getElementById('updateCoachForm');
        updateCoachForm.addEventListener('submit',
                event =>
                    updateEntity(
                        event,
                        getBackendUrl() + '/coaches/' + getParameterById('coach') + '?level=' + document.getElementById('level').value,
                        '/coach_view/coach_view.html?coach=' + getParameterById('coach')
                    )
        );
        await fetchCoach();
    }
});
