import React, { useState, useEffect } from 'react';
import type { Eveniment } from '../types';
import { eventsApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface EventListProps {
    onSelectEvent?: (event: Eveniment) => void;
    ownerOnly?: boolean;
}

export function EventList({ onSelectEvent, ownerOnly = false }: EventListProps) {
    const { user } = useAuth();
    const [events, setEvents] = useState<Eveniment[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchName, setSearchName] = useState('');
    const [searchLocation, setSearchLocation] = useState('');

    const loadEvents = async () => {
        setIsLoading(true);
        setError('');
        try {
            const response = await eventsApi.getAll(
                searchLocation || undefined,
                searchName || undefined
            );

            let eventList = response._embedded?.evenimentDTOes || response._embedded?.evenimentDTOList || response.content || [];

            // Dacă ownerOnly, filtrează doar evenimentele utilizatorului
            if (ownerOnly && user) {
                eventList = eventList.filter((e: Eveniment) => e.idOwner === user.userId);
            }

            setEvents(eventList);
        } catch (err) {
            setError('Eroare la încărcarea evenimentelor');
        }
        setIsLoading(false);
    };

    useEffect(() => {
        loadEvents();
    }, [ownerOnly, user?.userId]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        loadEvents();
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Sigur doriți să ștergeți acest eveniment?')) return;

        try {
            await eventsApi.delete(id);
            loadEvents();
        } catch {
            setError('Eroare la ștergere');
        }
    };

    if (isLoading) return <div className="loading">Se încarcă...</div>;

    return (
        <div className="event-list">
            <div className="list-header">
                {!ownerOnly && <h2>Evenimente</h2>}
                <form onSubmit={handleSearch} className="search-form">
                    <input
                        type="text"
                        placeholder="Cauta dupa nume..."
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                    />
                    <input
                        type="text"
                        placeholder="Cauta dupa locatie..."
                        value={searchLocation}
                        onChange={(e) => setSearchLocation(e.target.value)}
                    />
                    <button type="submit">Cauta</button>
                </form>
            </div>

            {error && <div className="error-message">{error}</div>}

            {events.length === 0 ? (
                <p className="empty-message">Nu există evenimente.</p>
            ) : (
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nume</th>
                            <th>Locație</th>
                            <th>Descriere</th>
                            <th>Locuri</th>
                            <th>Acțiuni</th>
                        </tr>
                    </thead>
                    <tbody>
                        {events.map((event) => (
                            <tr key={event.id}>
                                <td>{event.id}</td>
                                <td>{event.nume}</td>
                                <td>{event.locatie}</td>
                                <td>{event.descriere}</td>
                                <td>{event.numarLocuri}</td>
                                <td className="actions">
                                    <button onClick={() => onSelectEvent?.(event)}>Detalii</button>
                                    {user && (user.role === 'admin' || user.userId === event.idOwner) && (
                                        <button className="delete-btn" onClick={() => handleDelete(event.id)}>
                                            Șterge
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
