const oktaAuth = new OktaAuth({
    issuer: 'https://integrator-7447834.okta.com/oauth2/default',
    clientId: '0oaw3nzfn0RvOt7cc697',
    redirectUri: window.location.origin + '/index.html',
    scopes: ['openid', 'profile', 'email'],
    responseType: ['code'],
    pkce: true,
    tokenManager: { storage: 'localStorage' }
});

async function handleAuthCallback() {
    if (window.location.search.includes('code=')) {
        try {
            await oktaAuth.handleLoginRedirect(); // exchange code for tokens
            window.history.replaceState({}, document.title, '/index.html');
            await showUserInfo();
        } catch (e) {
            console.error('Callback error:', e);
        }
    }
}

function redirectToLogin() {
    oktaAuth.signInWithRedirect();
}

async function showUserInfo() {
    const accessToken = oktaAuth.getAccessToken();
    const idToken = oktaAuth.getIdToken();

    if (!accessToken || !idToken) return;

    const userinfo = await oktaAuth.getUser();
    const section = document.querySelector('.main-section');
    section.innerHTML = `
        <h2>Welcome ${userinfo.name} (${userinfo.email})</h2>
        <button id="logoutBtn" class="ui-control ui-button">Logout</button>
        <pre style="word-break: break-all; white-space: pre-wrap;">Access Token: \n${accessToken}</pre>
        <pre style="word-break: break-all; white-space: pre-wrap;">ID Token: \n${idToken}</pre>
    `;
    document.getElementById('logoutBtn').onclick = logout;

    showAuthenticatedNavigation();
}

function showAuthenticatedNavigation() {
    const coachesNav = document.getElementById('coaches-nav');
    if (coachesNav) {
        coachesNav.style.display = 'inline-block';
    }
}

function hideAuthenticatedNavigation() {
    const coachesNav = document.getElementById('coaches-nav');
    if (coachesNav) {
        coachesNav.style.display = 'none';
    }
}

async function logout() {
    try {
        oktaAuth.tokenManager.clear(); // clear tokens
        hideAuthenticatedNavigation();
        await oktaAuth.signOutOfOkta();
    } catch (error) {
        console.error('Logout error:', error);
        // Even if signOut fails, clear tokens, hide and reload
        oktaAuth.tokenManager.clear();
        hideAuthenticatedNavigation();
        window.location.reload();
    }
}

function submitSigninForm(event) {
    event.preventDefault();
    redirectToLogin();
}

async function init() {
    await handleAuthCallback();

    if (await oktaAuth.isAuthenticated()) {
        console.log('User is authenticated');
        await showUserInfo();
    } else {
        console.log('User is not authenticated');
        hideAuthenticatedNavigation();
        const signinForm = document.getElementById('signinForm');
        signinForm.addEventListener('submit', submitSigninForm);
    }
}

window.addEventListener('load', init);
