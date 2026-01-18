// Detectăm dacă suntem în development sau production
const isDev = window.location.port === '5173' || window.location.port === '3000';

// În development, folosim URL-uri absolute (localhost)
// În production (Docker), folosim URL-uri relative (nginx proxy)
const GATEWAY_API = isDev ? 'http://localhost:8000/api' : '/api';
const BILETE_API = isDev ? 'http://localhost:8080/api' : '/api';
const CLIENTI_API = isDev ? 'http://localhost:8081/api' : '/api';

// Helper pentru request-uri cu autentificare
async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
    const token = localStorage.getItem('token');

    const headers: HeadersInit = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
    }

    return fetch(url, { ...options, headers });
}

// API pentru autentificare (folosește Gateway)
export const authApi = {
    async login(email: string, password: string) {
        const response = await fetch(`${GATEWAY_API}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });
        return response.json();
    },

    async logout() {
        const response = await fetchWithAuth(`${GATEWAY_API}/auth/logout`, {
            method: 'POST',
        });
        return response.json();
    },
};

// API pentru evenimente
export const eventsApi = {
    async getAll(location?: string, name?: string) {
        const params = new URLSearchParams();
        if (location) params.append('location', location);
        if (name) params.append('name', name);
        const query = params.toString() ? `?${params}` : '';

        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events${query}`);
        return response.json();
    },

    async getById(id: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events/${id}`);
        return response.json();
    },

    async create(event: { idOwner: number; nume: string; locatie: string; descriere: string; numarLocuri: number }) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events`, {
            method: 'POST',
            body: JSON.stringify(event),
        });
        return response.json();
    },

    async update(id: number, event: { idOwner: number; nume: string; locatie: string; descriere: string; numarLocuri: number }) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events/${id}`, {
            method: 'PUT',
            body: JSON.stringify(event),
        });
        return response.json();
    },

    async delete(id: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events/${id}`, {
            method: 'DELETE',
        });
        return response.ok;
    },

    async getTickets(eventId: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events/${eventId}/tickets`);
        return response.json();
    },
};

// API pentru pachete
export const packagesApi = {
    async getAll(page?: number, itemsPerPage?: number) {
        const params = new URLSearchParams();
        if (page !== undefined) params.append('page', page.toString());
        if (itemsPerPage !== undefined) params.append('items_per_page', itemsPerPage.toString());
        const query = params.toString() ? `?${params}` : '';

        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets${query}`);
        return response.json();
    },

    async getById(id: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets/${id}`);
        return response.json();
    },

    async create(pkg: { idOwner: number; nume: string; locatie: string; descriere: string; numarLocuri: number }) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets`, {
            method: 'POST',
            body: JSON.stringify(pkg),
        });
        return response.json();
    },

    async delete(id: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets/${id}`, {
            method: 'DELETE',
        });
        return response.ok;
    },

    async getEvents(packetId: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets/${packetId}/events`);
        return response.json();
    },

    async getTickets(packetId: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets/${packetId}/tickets`);
        return response.json();
    },
};

// API pentru bilete
export const ticketsApi = {
    async getAll() {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/tickets`);
        return response.json();
    },

    async getByCod(cod: string) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/tickets/${cod}`);
        return response.json();
    },

    async createForEvent(eventId: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/events/${eventId}/tickets`, {
            method: 'POST',
            body: JSON.stringify({}),
        });
        return response.json();
    },

    async createForPacket(packetId: number) {
        const response = await fetchWithAuth(`${BILETE_API}/event-manager/event-packets/${packetId}/tickets`, {
            method: 'POST',
            body: JSON.stringify({}),
        });
        return response.json();
    },
};

// API pentru clienți
export const clientsApi = {
    async getAll(page = 0, size = 10) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients?page=${page}&size=${size}`);
        return response.json();
    },

    async getById(id: string) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients/${id}`);
        return response.json();
    },

    async getByEmail(email: string) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients?email=${encodeURIComponent(email)}`);
        return response.json();
    },

    async create(client: { email: string; prenume: string; nume: string; dateSuntPublice: boolean }) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients`, {
            method: 'POST',
            body: JSON.stringify(client),
        });
        return response.json();
    },

    async update(id: string, client: { email: string; prenume: string; nume: string; dateSuntPublice: boolean }) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients/${id}`, {
            method: 'PUT',
            body: JSON.stringify(client),
        });
        return response.json();
    },

    async buyTicket(clientId: string, evenimentId?: number, pachetId?: number) {
        const body: { evenimentId?: number; pachetId?: number } = {};
        if (evenimentId) body.evenimentId = evenimentId;
        if (pachetId) body.pachetId = pachetId;

        const response = await fetchWithAuth(`${CLIENTI_API}/clients/${clientId}/bilete`, {
            method: 'POST',
            body: JSON.stringify(body),
        });
        if (!response.ok) {
            throw new Error(`Purchase failed: ${response.status}`);
        }
        return response.json();
    },

    async getTickets(clientId: string) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients/${clientId}/bilete`);
        return response.json();
    },

    async returnTicket(clientId: string, codBilet: string) {
        const response = await fetchWithAuth(`${CLIENTI_API}/clients/${clientId}/bilete/${codBilet}`, {
            method: 'DELETE',
        });
        return response.json();
    },
};
