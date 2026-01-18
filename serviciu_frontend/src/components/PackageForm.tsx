import React, { useState } from 'react';
import { packagesApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface PackageFormProps {
    onSuccess: () => void;
    onCancel: () => void;
}

export function PackageForm({ onSuccess, onCancel }: PackageFormProps) {
    const { user } = useAuth();
    const [formData, setFormData] = useState({
        nume: '',
        locatie: '',
        descriere: '',
        numarLocuri: 100,
    });
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!user) return;

        setIsLoading(true);
        setError('');

        try {
            await packagesApi.create({
                idOwner: user.userId,
                ...formData,
            });
            onSuccess();
        } catch {
            setError('Eroare la crearea pachetului');
        }

        setIsLoading(false);
    };

    return (
        <div className="form-container">
            <h2>Pachet Nou</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Nume:</label>
                    <input
                        type="text"
                        value={formData.nume}
                        onChange={(e) => setFormData({ ...formData, nume: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Locatie:</label>
                    <input
                        type="text"
                        value={formData.locatie}
                        onChange={(e) => setFormData({ ...formData, locatie: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Descriere:</label>
                    <textarea
                        value={formData.descriere}
                        onChange={(e) => setFormData({ ...formData, descriere: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Numar locuri:</label>
                    <input
                        type="number"
                        value={formData.numarLocuri}
                        onChange={(e) => setFormData({ ...formData, numarLocuri: parseInt(e.target.value) })}
                        min="1"
                        required
                    />
                </div>
                {error && <div className="error-message">{error}</div>}
                <div className="form-actions">
                    <button type="button" onClick={onCancel}>Anuleaza</button>
                    <button type="submit" disabled={isLoading}>
                        {isLoading ? 'Se salveaza...' : 'Salveaza'}
                    </button>
                </div>
            </form>
        </div>
    );
}
