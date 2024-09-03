"use strict";

// Existing functions in auth.mjs

export async function login(username, password) {
    try {
        const response = await fetch('/api/registration/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.status === 403) {
            console.log("403 error - lockout");
            alert('Je account is tijdelijk geblokkeerd wegens te veel mislukte inlogpogingen. Probeer het later opnieuw.');
            return false;
        }

        if (!response.ok) {
            console.log("Login error - incorrect credentials");
            alert('Login mislukt. Controleer je inloggegevens.');
            return false;
        }
        const token = response.headers.get('Authorization');
        if (!token) {
            throw new Error('Geen token ontvangen van de server.');
        }

        localStorage.setItem('authToken', token);
        return true;
    } catch (error) {
        console.log("Unexpected error caught", error);
        alert(error.message);  // Only show if needed
        return false;
    }

}

export function logout() {
    localStorage.removeItem('authToken');
}

export function getToken() {
    return localStorage.getItem('authToken');
}

// New functions to add

export async function checkIfLoggedIn() {
    const token = getToken();
    if (!token) {
        alert('Je bent niet ingelogd.');
        window.location.href = '/login.html';
        return null; // User is not logged in
    }

    return await getLoggedInUser(token);
}

export async function getLoggedInUser(token) {
    try {
        const response = await fetch('/api/users/profile', {
            method: 'GET',
            headers: { 'Authorization': token }
        });

        if (!response.ok) {
            throw new Error('Kon gebruikersinformatie niet ophalen.');
        }

        const user = await response.json();
        return user;
    } catch (error) {
        alert('Kon gebruikersinformatie niet ophalen.');
        console.error(error);
        return null;
    }
}
