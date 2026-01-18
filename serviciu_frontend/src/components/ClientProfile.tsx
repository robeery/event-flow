import { useState, useEffect } from 'react';
import type { Client, ClientRequest } from '../types';
import { clientsApi } from '../api';
import { useAuth } from '../context/AuthContext';

export function ClientProfile() {
    const { user } = useAuth();
    const [client, setClient] = useState<Client | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    const [formData, setFormData] = useState<ClientRequest>({
        email: '',
        prenume: '',
        nume: '',
        dateSuntPublice: true,
    });

    useEffect(() => {
        loadClient();
    }, [user?.userId]);

    const loadClient = async () => {
        if (!user) return;

        setIsLoading(true);
        setError('');

        try {
            const response = await clientsApi.getAll(0, 100);
            const clients = response.clients || [];
            const foundClient = clients.find((c: any) => c.idmUserId === user.userId);

            if (foundClient) {
                // Obține detaliile complete
                const fullClient = await clientsApi.getById(foundClient.id);
                setClient(fullClient);
                setFormData({
                    email: fullClient.email,
                    prenume: fullClient.prenume,
                    nume: fullClient.nume,
                    dateSuntPublice: fullClient.dateSuntPublice,
                });
            }
        } catch (err) {
            setError('Eroare la încărcarea profilului');
        }

        setIsLoading(false);
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setMessage('');
        setIsLoading(true);

        try {
            const newClient = await clientsApi.create(formData);
            setClient(newClient);
            setMessage('Profil creat cu succes!');
            setIsEditing(false);
        } catch {
            setError('Eroare la crearea profilului');
        }

        setIsLoading(false);
    };

    const handleUpdate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!client) return;

        setError('');
        setMessage('');
        setIsLoading(true);

        try {
            const updated = await clientsApi.update(client.id, formData);
            setClient(updated);
            setMessage('Profil actualizat cu succes!');
            setIsEditing(false);
        } catch {
            setError('Eroare la actualizarea profilului');
        }

        setIsLoading(false);
    };

    if (isLoading) return <div className="loading">Se încarcă...</div>;

    // Formular pentru creare/editare
    if (!client || isEditing) {
        return (
            <div className="profile-container">
                <h2>{client ? 'Editare Profil' : 'Creare Profil Client'}</h2>

                {error && <div className="error-message">{error}</div>}

                <form onSubmit={client ? handleUpdate : handleCreate} className="profile-form">
                    <div className="form-group">
                        <label>Email:</label>
                        <input
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Prenume:</label>
                        <input
                            type="text"
                            value={formData.prenume}
                            onChange={(e) => setFormData({ ...formData, prenume: e.target.value })}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Nume:</label>
                        <input
                            type="text"
                            value={formData.nume}
                            onChange={(e) => setFormData({ ...formData, nume: e.target.value })}
                            required
                        />
                    </div>
                    <div className="form-group checkbox">
                        <label>
                            <input
                                type="checkbox"
                                checked={formData.dateSuntPublice}
                                onChange={(e) => setFormData({ ...formData, dateSuntPublice: e.target.checked })}
                            />
                            Date publice
                        </label>
                    </div>
                    <div className="form-actions">
                        {client && (
                            <button type="button" onClick={() => setIsEditing(false)}>Anulează</button>
                        )}
                        <button type="submit" disabled={isLoading}>
                            {client ? 'Salvează' : 'Crează Profil'}
                        </button>
                    </div>
                </form>
            </div>
        );
    }

    // Afișare profil
    return (
        <div className="profile-container">
            <h2>Profilul Meu</h2>

            {message && <div className="success-message">{message}</div>}
            {error && <div className="error-message">{error}</div>}

            <div className="profile-card">
                <div className="profile-row">
                    <strong>Email:</strong> {client.email}
                </div>
                <div className="profile-row">
                    <strong>Nume:</strong> {client.prenume} {client.nume}
                </div>
                <div className="profile-row">
                    <strong>Date publice:</strong> {client.dateSuntPublice ? 'Da' : 'Nu'}
                </div>
                <div className="profile-row">
                    <strong>Bilete achiziționate:</strong> {client.bileteAchizitionate?.length || 0}
                </div>

                <button onClick={() => setIsEditing(true)} className="edit-button">
                    Editează Profil
                </button>
            </div>
        </div>
    );
}
