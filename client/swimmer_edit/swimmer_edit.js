import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, updateEntity, fetchEntity} from '../auth/http_requests.js';

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
        await fetchEntity(getBackendUrl() + '/swimmers/' + getParameterById('swimmer'));
    }
});
