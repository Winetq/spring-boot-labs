import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    const infoForm = document.getElementById('infoForm');

    infoForm.addEventListener('submit', event => updateInfoAction(event));

    fetchAndDisplayCoach();
});

/**
 * Fetches currently choosen coach and updates edit form.
 */
function fetchAndDisplayCoach() {
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
    xhttp.open("GET", getBackendUrl() + '/coaches/' + getParameterById('coach'), true);
    xhttp.send();
}

/**
 * Action event handled for updating coach info.
 *
 * @param {Event} event dom event
 */
function updateInfoAction(event) {
    event.preventDefault();

    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            fetchAndDisplayCoach();
        }
    };
    xhttp.open("PUT", getBackendUrl() + '/coaches/' + getParameterById('coach') + '?level='
        + document.getElementById('level').value, true);
    xhttp.send();
    alert("Let's see the results!");
}
