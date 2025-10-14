import {
    getParameterById,
    clearElementChildren,
    createLinkCell,
    createButtonCell,
    createTextCell
} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';
import {requireAuth, fetchEntity, fetchEntities, deleteEntity} from '../auth/http_requests.js';

window.addEventListener('load', async () => {
    if (await requireAuth()) {
        await prepareCreateSwimmerLink();
        await fetchEntity(getBackendUrl() + '/coaches/' + getParameterById('coach'));
        await fetchEntities(getBackendUrl() + '/coaches/' + getParameterById('coach') + '/swimmers', displaySwimmers);
    }
});

async function prepareCreateSwimmerLink() {
    let link = document.getElementById('create_swimmer_link');
    link.appendChild(createLinkCell('create swimmer', '../swimmer_create/swimmer_create.html?coach=' + getParameterById('coach')));
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
    tr.appendChild(createButtonCell('delete', () =>
            deleteEntity(
                getBackendUrl() + '/swimmers/' + swimmer.id,
                getBackendUrl() + '/coaches/' + getParameterById('coach') + '/swimmers',
                displaySwimmers
            )
        )
    );
    return tr;
}
