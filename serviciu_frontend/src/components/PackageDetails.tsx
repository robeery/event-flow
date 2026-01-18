import { useState, useEffect } from 'react';
import type { Pachet, Client, Eveniment } from '../types';
import { packagesApi, clientsApi, ticketsApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface TicketWithClient {
    cod: string;
    client?: Client;
}

interface PackageDetailsProps {
    pkg: Pachet;
    onBack: () => void;
}

export function PackageDetails({ pkg, onBack }: PackageDetailsProps) {
    const { user } = useAuth();
    const [ticketsWithClients, setTicketsWithClients] = useState<TicketWithClient[]>([]);
    const [packageEvents, setPackageEvents] = useState<Eveniment[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [clientId, setClientId] = useState<string | null>(null);

    useEffect(() => {
        loadPackageEvents();
        if (user && (user.role === 'admin' || user.userId === pkg.idOwner)) {
            loadTicketsWithClients();
        }
        if (user && user.role === 'client') {
            findClientId();
        }
    }, [pkg.id, user]);

    const loadPackageEvents = async () => {
        try {
            const response = await packagesApi.getEvents(pkg.id);
            setPackageEvents(response._embedded?.evenimentDTOes || response._embedded?.evenimentDTOList || response.content || []);
        } catch {
            setPackageEvents([]);
        }
    };

    const findClientId = async () => {
        try {
            const response = await clientsApi.getAll(0, 100);
            const clients = response.clients || [];
            const client = clients.find((c: any) => c.idmUserId === user?.userId);
            if (client) {
                setClientId(client.id);
            }
        } catch {
            console.error('Nu s-a putut gasi clientul');
        }
    };

    const loadTicketsWithClients = async () => {
        setIsLoading(true);
        try {
            // Incarca biletele pachetului
            const ticketsResponse = await packagesApi.getTickets(pkg.id);
            const ticketList = ticketsResponse._embedded?.biletDTOes || ticketsResponse._embedded?.biletDTOList || ticketsResponse.content || [];

            // Incarca toti clientii pentru a gasi cine a cumparat biletele
            const clientsResponse = await clientsApi.getAll(0, 1000);
            const allClients = clientsResponse.clients || [];

            // Pentru fiecare client, incarca detaliile complete (inclusiv bileteAchizitionate)
            const clientsWithTickets: TicketWithClient[] = [];

            for (const ticket of ticketList) {
                let foundClient: Client | undefined;

                for (const clientSummary of allClients) {
                    try {
                        const fullClient = await clientsApi.getById(clientSummary.id);
                        if (fullClient.bileteAchizitionate?.includes(ticket.cod)) {
                            // Returneaza doar info publica daca dateSuntPublice e true
                            if (fullClient.dateSuntPublice) {
                                foundClient = fullClient;
                            } else {
                                foundClient = {
                                    id: fullClient.id,
                                    idmUserId: fullClient.idmUserId,
                                    email: '[Privat]',
                                    prenume: '[Privat]',
                                    nume: '[Privat]',
                                    dateSuntPublice: false
                                };
                            }
                            break;
                        }
                    } catch {
                        // Ignoră erori la încărcarea clientului
                    }
                }

                clientsWithTickets.push({
                    cod: ticket.cod,
                    client: foundClient
                });
            }

            setTicketsWithClients(clientsWithTickets);
        } catch {
            setTicketsWithClients([]);
        }
        setIsLoading(false);
    };

    const handleBuyTicket = async () => {
        if (!clientId) {
            setMessage('Nu s-a găsit profilul de client. Creați-vă un profil mai întâi.');
            return;
        }

        setIsLoading(true);
        setMessage('');

        try {
            await clientsApi.buyTicket(clientId, undefined, pkg.id);
            setMessage('Bilet achiziționat cu succes!');
        } catch {
            setMessage('Eroare la achiziționarea biletului');
        }

        setIsLoading(false);
    };

    const handleCreateTicket = async () => {
        setIsLoading(true);
        setMessage('');

        try {
            await ticketsApi.createForPacket(pkg.id);
            setMessage('Bilet creat cu succes!');
            loadTicketsWithClients();
        } catch {
            setMessage('Eroare la crearea biletului');
        }

        setIsLoading(false);
    };

    const canManage = user && (user.role === 'admin' || user.userId === pkg.idOwner);
    const canBuy = user && user.role === 'client';

    return (
        <div className="package-details">
            <button onClick={onBack} className="back-button">← Înapoi</button>

            <div className="details-card">
                <h2>{pkg.nume}</h2>
                <div className="detail-row">
                    <strong>Locație:</strong> {pkg.locatie}
                </div>
                <div className="detail-row">
                    <strong>Descriere:</strong> {pkg.descriere}
                </div>
                <div className="detail-row">
                    <strong>Locuri:</strong> {pkg.numarLocuri}
                </div>
                <div className="detail-row">
                    <strong>ID Proprietar:</strong> {pkg.idOwner}
                </div>

                {message && (
                    <div className={message.includes('succes') ? 'success-message' : 'error-message'}>
                        {message}
                    </div>
                )}

                <div className="package-events-list">
                    <h3>Evenimente incluse:</h3>
                    {packageEvents.length === 0 ? (
                        <p>Niciun eveniment în acest pachet.</p>
                    ) : (
                        <ul>
                            {packageEvents.map((event) => (
                                <li key={event.id}>
                                    <strong>{event.nume}</strong> - {event.locatie}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div className="action-buttons">
                    {canBuy && (
                        <button onClick={handleBuyTicket} disabled={isLoading}>
                            Cumpără Bilet Pachet
                        </button>
                    )}
                    {canManage && (
                        <button onClick={handleCreateTicket} disabled={isLoading}>
                            + Crează Bilet
                        </button>
                    )}
                </div>

                {canManage && ticketsWithClients.length > 0 && (
                    <div className="tickets-section">
                        <h3>Bilete Pachet ({ticketsWithClients.length})</h3>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Cod Bilet</th>
                                    <th>Client</th>
                                    <th>Email</th>
                                </tr>
                            </thead>
                            <tbody>
                                {ticketsWithClients.map((item) => (
                                    <tr key={item.cod}>
                                        <td>{item.cod}</td>
                                        <td>
                                            {item.client
                                                ? `${item.client.prenume} ${item.client.nume}`
                                                : '-'}
                                        </td>
                                        <td>
                                            {item.client?.email || '-'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}
