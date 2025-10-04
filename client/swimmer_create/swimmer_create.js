import {getParameterById} from '../js/dom_utils.js';
import {getBackendUrl} from '../js/configuration.js';

window.addEventListener('load', () => {
    const infoForm = document.getElementById('infoForm');

    infoForm.addEventListener('submit', event => createSwimmerAction(event));
});

/**
 * Action event handled for creating swimmer.
 *
 * @param {Event} event dom event
 */
function createSwimmerAction(event) {
    event.preventDefault();
    
    if (document.getElementById('name').value !== "" && document.getElementById('specialization').value !==  "") {
        const xhttp = new XMLHttpRequest();
        xhttp.open("POST", getBackendUrl() + '/swimmers/with_coach', false); // a synchronous request
        let coach_id = getParameterById('coach');
        const request = {
            'coach_id': coach_id,
            'name': document.getElementById('name').value,
            'specialization': document.getElementById('specialization').value
        };
        xhttp.setRequestHeader('Content-Type', 'application/json');
        xhttp.send(JSON.stringify(request)); // JSON.parse()
        alert("Let's see the results!");
    }
}
