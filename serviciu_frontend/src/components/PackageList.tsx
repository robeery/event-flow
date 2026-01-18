import { useState, useEffect } from 'react';
import type { Pachet } from '../types';
import { packagesApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { PackageDetails } from './PackageDetails';

interface PackageListProps {
    onSelectPackage?: (pkg: Pachet) => void;
    ownerOnly?: boolean;
}

export function PackageList({ onSelectPackage, ownerOnly = false }: PackageListProps) {
    const { user } = useAuth();
    const [packages, setPackages] = useState<Pachet[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [selectedPackage, setSelectedPackage] = useState<Pachet | null>(null);

    const loadPackages = async () => {
        setIsLoading(true);
        setError('');
        try {
            const response = await packagesApi.getAll();
            let pkgList = response._embedded?.pachetDTOes || response._embedded?.pachetDTOList || response.content || [];

            if (ownerOnly && user) {
                pkgList = pkgList.filter((p: Pachet) => p.idOwner === user.userId);
            }

            setPackages(pkgList);
        } catch {
            setError('Eroare la încărcarea pachetelor');
        }
        setIsLoading(false);
    };

    useEffect(() => {
        loadPackages();
    }, [ownerOnly, user?.userId]);

    const handleSelectPackage = (pkg: Pachet) => {
        setSelectedPackage(pkg);
        onSelectPackage?.(pkg);
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Sigur doriți să ștergeți acest pachet?')) return;

        try {
            await packagesApi.delete(id);
            loadPackages();
            setSelectedPackage(null);
        } catch {
            setError('Eroare la ștergere');
        }
    };

    if (selectedPackage) {
        return (
            <PackageDetails
                pkg={selectedPackage}
                onBack={() => setSelectedPackage(null)}
            />
        );
    }

    if (isLoading) return <div className="loading">Se încarcă...</div>;

    return (
        <div className="package-list">
            {!ownerOnly && (
                <div className="list-header">
                    <h2>Pachete Evenimente</h2>
                </div>
            )}

            {error && <div className="error-message">{error}</div>}

            <div className="package-grid">
                {packages.length === 0 ? (
                    <p className="empty-message">Nu există pachete.</p>
                ) : (
                    packages.map((pkg) => (
                        <div
                            key={pkg.id}
                            className={`package-card`}
                            onClick={() => handleSelectPackage(pkg)}
                        >
                            <h3>{pkg.nume}</h3>
                            <p className="location">{pkg.locatie}</p>
                            <p className="description">{pkg.descriere}</p>
                            <p className="seats">Locuri: {pkg.numarLocuri}</p>
                            {user && (user.role === 'admin' || user.userId === pkg.idOwner) && (
                                <button
                                    className="delete-btn"
                                    onClick={(e) => { e.stopPropagation(); handleDelete(pkg.id); }}
                                >
                                    Șterge
                                </button>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
