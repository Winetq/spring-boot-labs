import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    const infoForm = document.getElementById('infoForm');

    infoForm.addEventListener('submit', event => updateInfoAction(event));

    fetchAndDisplaySwimmer();
});

/**
 * Fetches currently choosen coach's swimmer and updates edit form.
 */
function fetchAndDisplaySwimmer() {
    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            let response = JSON.parse(this.responseText);
            for (const [key, value] of Object.entries(response)) {
                let input = document.getElementById(key);
                if (input) {
                    input.value = value;
                }
            }
        }
    };
    xhttp.open("GET", getBackendUrl() + '/swimmers/' + getParameterById('swimmer'), true);
    xhttp.send();
}

/**
 * Action event handled for updating swimmer info.
 *
 * @param {Event} event dom event
 */
function updateInfoAction(event) {
    event.preventDefault();

    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            fetchAndDisplaySwimmer();
        }
    };
    xhttp.open("PUT", getBackendUrl() + '/swimmers/' + getParameterById('swimmer') + '?specialization='
        + document.getElementById('specialization').value, true);
    xhttp.send();
    alert("Let's see the results!");
}
