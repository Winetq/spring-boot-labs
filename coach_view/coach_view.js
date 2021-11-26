import {
    getParameterById,
    clearElementChildren,
    createLinkCell,
    createButtonCell,
    createTextCell,
    createImageCell,
    setTextNode
} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    prepareCreateSwimmerLink();
    fetchAndDisplayCoach();
    fetchAndDisplaySwimmers();
});

function prepareCreateSwimmerLink() {
    let link = document.getElementById('create_swimmer_link');
    link.appendChild(createLinkCell('create', '../swimmer_create/swimmer_create.html?coach=' + getParameterById('coach')));
}

/**
 * Fetches all swimmers and modifies the DOM tree in order to display them.
 */
function fetchAndDisplaySwimmers() {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            displaySwimmers(JSON.parse(this.responseText))
        }
    };
    xhttp.open("GET", getBackendUrl() + '/coaches/' + getParameterById('coach') + '/swimmers', true);
    xhttp.send();
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
    tr.appendChild(createButtonCell('delete', () => deleteSwimmer(swimmer.id)));
    return tr;
}

/**
 * Deletes entity from backend and reloads table.
 *
 * @param {number} swimmer to be deleted
 */
function deleteSwimmer(id) {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 202) {
            fetchAndDisplaySwimmers();
        }
    };
    xhttp.open("DELETE", getBackendUrl() + '/swimmers/' + id, true);
    xhttp.send();
}

/**
 * Fetches single coach and modifies the DOM tree in order to display it.
 */
function fetchAndDisplayCoach() {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            displayCoach(JSON.parse(this.responseText))
        }
    };
    xhttp.open("GET", getBackendUrl() + '/coaches/' + getParameterById('coach'), true);
    xhttp.send();
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
