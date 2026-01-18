import { useState, useEffect } from 'react';
import { clientsApi } from '../api';
import { useAuth } from '../context/AuthContext';

export function MyTickets() {
    const { user } = useAuth();
    const [tickets, setTickets] = useState<any[]>([]);
    const [clientId, setClientId] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    useEffect(() => {
        findClientAndLoadTickets();
    }, [user?.userId]);

    const findClientAndLoadTickets = async () => {
        if (!user) return;

        setIsLoading(true);
        setError('');

        try {
            // Găsește clientul după userId
            const response = await clientsApi.getAll(0, 100);
            const clients = response.clients || [];
            const client = clients.find((c: any) => c.idmUserId === user.userId);

            if (client) {
                setClientId(client.id);
                // Încarcă biletele
                const ticketsResponse = await clientsApi.getTickets(client.id);
                setTickets(ticketsResponse.bilete || []);
            } else {
                setError('Nu aveți un profil de client. Creați-vă un profil din secțiunea Profil.');
            }
        } catch (err) {
            setError('Eroare la încărcarea biletelor');
        }

        setIsLoading(false);
    };

    const handleReturnTicket = async (codBilet: string) => {
        if (!clientId) return;
        if (!confirm('Sigur doriți să returnați acest bilet?')) return;

        setMessage('');
        try {
            await clientsApi.returnTicket(clientId, codBilet);
            setMessage('Bilet returnat cu succes!');
            findClientAndLoadTickets();
        } catch {
            setMessage('Eroare la returnarea biletului');
        }
    };

    if (isLoading) return <div className="loading">Se încarcă...</div>;

    return (
        <div className="my-tickets">
            <h2>Biletele Mele</h2>

            {error && <div className="error-message">{error}</div>}
            {message && (
                <div className={message.includes('succes') ? 'success-message' : 'error-message'}>
                    {message}
                </div>
            )}

            {tickets.length === 0 ? (
                <p className="empty-message">Nu aveți bilete achiziționate.</p>
            ) : (
                <div className="tickets-grid">
                    {tickets.map((ticket, index) => (
                        <div key={ticket.codBilet || index} className="ticket-card">
                            <div className="ticket-header">
                                <span className="ticket-code">{ticket.codBilet}</span>
                            </div>
                            <div className="ticket-body">
                                {ticket.eveniment && (
                                    <>
                                        <p><strong>Eveniment:</strong> {ticket.eveniment.nume}</p>
                                        <p><strong>Locație:</strong> {ticket.eveniment.locatie}</p>
                                    </>
                                )}
                                {ticket.pachet && (
                                    <>
                                        <p><strong>Pachet:</strong> {ticket.pachet.nume}</p>
                                        <p><strong>Locație:</strong> {ticket.pachet.locatie}</p>
                                    </>
                                )}
                            </div>
                            <div className="ticket-actions">
                                <button
                                    className="delete-btn"
                                    onClick={() => handleReturnTicket(ticket.codBilet)}
                                >
                                    Returnează
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
