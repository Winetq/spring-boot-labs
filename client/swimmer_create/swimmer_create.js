import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, createEntity} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const createSwimmerForm = document.getElementById('createSwimmerForm');
        createSwimmerForm.addEventListener('submit',
                event =>
                    createEntity(
                        event,
                        getBackendUrl() + '/swimmers/with_coach',
                        buildSwimmerRequest(),
                        '/coach_view/coach_view.html?coach=' + getParameterById('coach')
                    )
        );
    }
});

function buildSwimmerRequest() {
    const nameValue = document.getElementById('name')?.value.trim() || '';
    const specializationValue = document.getElementById('specialization')?.value.trim() || '';
    return {
        coachId: getParameterById('coach'),
        name: nameValue === '' ? 'testName' : nameValue,
        specialization: specializationValue === '' ? 'FREESTYLE' : specializationValue
    };
}
