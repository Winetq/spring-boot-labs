import {clearElementChildren, createLinkCell, createButtonCell, createTextCell} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    fetchAndDisplayCoaches();
});

/**
 * Fetches all coaches and modifies the DOM tree in order to display them.
 */
function fetchAndDisplayCoaches() {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            displayCoaches(JSON.parse(this.responseText));
        }
    };
    xhttp.open("GET", getBackendUrl() + '/coaches', true);
    xhttp.send();
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

/**
 * Deletes entity from backend and reloads table.
 *
 * @param {string} coach to be deleted
 */
function deleteCoach(coach) {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 202) {
            fetchAndDisplayCoaches();
        }
    };
    xhttp.open("DELETE", getBackendUrl() + '/coaches/' + coach.id, true);
    xhttp.send();
}
