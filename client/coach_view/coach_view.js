import {
    getParameterById,
    clearElementChildren,
    createLinkCell,
    createButtonCell,
    createTextCell,
    setTextNode
} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, authenticatedGet, authenticatedDelete} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        await prepareCreateSwimmerLink();
        await fetchCoach();
        await fetchSwimmers();
    }
});

async function prepareCreateSwimmerLink() {
    let link = document.getElementById('create_swimmer_link');
    link.appendChild(createLinkCell('create swimmer', '../swimmer_create/swimmer_create.html?coach=' + getParameterById('coach')));
}

/**
 * Fetches all swimmers and modifies the DOM tree in order to display them.
 */
async function fetchSwimmers() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/coaches/' + getParameterById('coach') + '/swimmers');
        if (response?.ok) {
            const swimmers = await response.json();
            displaySwimmers(swimmers);
        } else {
            console.warn('Fetch swimmers failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching swimmers:', error);
    }
}

/**
 * Updates the DOM tree in order to display swimmers.
 *
 * @param {{swimmers: {id: number, name: string, specialization: string}[]}} swimmers
 */
function displaySwimmers(swimmers) {
    let tableBody = document.getElementById('tableBody');
    clearElementChildren(tableBody);
    swimmers.forEach(swimmer => {
        tableBody.appendChild(createTableRow(swimmer));
    })
}

/**
 * Creates single table row for entity.
 *
 * @param {{id: number, name: string, specialization: string}} swimmer
 * @returns {HTMLTableRowElement}
 */
function createTableRow(swimmer) {
    let tr = document.createElement('tr');
    tr.appendChild(createTextCell(swimmer.name));
    tr.appendChild(createTextCell(swimmer.specialization));
    tr.appendChild(createLinkCell('edit', '../swimmer_edit/swimmer_edit.html?coach=' + getParameterById('coach') + '&swimmer=' + swimmer.id));
    tr.appendChild(createButtonCell('delete', () => deleteSwimmer(swimmer)));
    return tr;
}

/**
 * Deletes entity from backend and reloads table.
 *
 * @param {string} swimmer to be deleted
 */
async function deleteSwimmer(swimmer) {
    try {
        const response = await authenticatedDelete(getBackendUrl() + '/swimmers/' + swimmer.id);
        if (response?.ok) {
            await fetchSwimmers();
        } else {
            console.warn('Delete swimmer failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error deleting swimmer:', error);
    }
}

/**
 * Fetches single coach and modifies the DOM tree in order to display it.
 */
async function fetchCoach() {
    try {
        const response = await authenticatedGet(getBackendUrl() + '/coaches/' + getParameterById('coach'));
        if (response?.ok) {
            const coach = await response.json();
            displayCoach(coach);
        } else {
            console.warn('Fetch coach failed:', response?.status, response?.statusText);
        }
    } catch (error) {
        console.error('Error fetching coach:', error);
    }
}

/**
 * Updates the DOM tree in order to display coach.
 *
 * @param {string} coach
 */
function displayCoach(coach) {
    setTextNode('name', coach.name);
    setTextNode('level', coach.level);
}
