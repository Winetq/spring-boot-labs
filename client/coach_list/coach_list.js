import {clearElementChildren, createLinkCell, createButtonCell, createTextCell} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedGet, authenticatedDelete} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        await fetchCoaches();
    }
});

/**
 * Fetches all coaches and modifies the DOM tree in order to display them.
 */
async function fetchCoaches() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/coaches');
        if (response?.ok) {
            const coaches = await response.json();
            displayCoaches(coaches);
        } else {
            console.warn('Fetch coaches failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching coaches:', error);
    }
}

/**
 * Updates the DOM tree in order to display coaches.
 *
 * @param {{coaches: string[]}} coaches
 */
function displayCoaches(coaches) {
    let tableBody = document.getElementById('tableBody');
    clearElementChildren(tableBody);
    coaches.forEach(coach => {
        tableBody.appendChild(createTableRow(coach));
    })
}

/**
 * Creates single table row for entity.
 *
 * @param {string} coach
 * @returns {HTMLTableRowElement}
 */
function createTableRow(coach) {
    let tr = document.createElement('tr');
    tr.appendChild(createTextCell(coach.name));
    tr.appendChild(createLinkCell('view', '../coach_view/coach_view.html?coach=' + coach.id));
    tr.appendChild(createLinkCell('edit', '../coach_edit/coach_edit.html?coach=' + coach.id));
    tr.appendChild(createButtonCell('delete', () => deleteCoach(coach)));
    return tr;
}

// TODO: allow to delete coach only if user has an appropriate role
/**
 * Deletes entity from backend and reloads table.
 *
 * @param {string} coach to be deleted
 */
async function deleteCoach(coach) {
    try {
        const response = await authenticatedDelete(getBackendUrl() + '/coaches/' + coach.id);
        if (response?.ok) {
            await fetchCoaches();
        } else {
            console.warn('Delete coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error deleting coach:', error);
    }
}
