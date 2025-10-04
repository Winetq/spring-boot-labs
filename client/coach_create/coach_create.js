import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    const infoForm = document.getElementById('infoForm');

    infoForm.addEventListener('submit', event => createCoachAction(event));
});

/**
 * Action event handled for creating coach.
 *
 * @param {Event} event dom event
 */
function createCoachAction(event) {
    event.preventDefault();

    const xhttp = new XMLHttpRequest();
    xhttp.open("POST", getBackendUrl() + '/coaches', false); // a synchronous request
    
    if (document.getElementById('name').value == "" || document.getElementById('level').value ==  "") {
        const request = {
            'name': 'name',
            'level': '0'
        };
        xhttp.setRequestHeader('Content-Type', 'application/json');
        xhttp.send(JSON.stringify(request)); // JSON.parse()
    } else {
        const request = {
            'name': document.getElementById('name').value,
            'level': document.getElementById('level').value
        };
        xhttp.setRequestHeader('Content-Type', 'application/json');
        xhttp.send(JSON.stringify(request)); // JSON.parse()
    }
    
    alert("Let's see the results!");
}
