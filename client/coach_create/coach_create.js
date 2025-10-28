import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, createEntity} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        const createCoachForm = document.getElementById('createCoachForm');
        createCoachForm.addEventListener('submit',
                event =>
                    createEntity(
                        event,
                        getBackendUrl() + '/coaches',
                        buildCoachRequest(),
                        '/coach_list/coach_list.html'
                    )
        );
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
