import { useState } from 'react';

export default function ChargingWindowForm({ onSubmit, isLoading }: { onSubmit: (h: number) => void, isLoading: boolean }) {
    const [hours, setHours] = useState('');
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const h = parseInt(hours);
        if (isNaN(h) || h < 1 || h > 6) {
            setError('Please enter a number between 1 and 6.');
            return;
        }
        setError(null);
        onSubmit(h);
    };

    return (
        <div className="card">
            <h3>Find Optimal Charging Window</h3>
            <form onSubmit={handleSubmit} className="form-group">
                <label htmlFor="hours">Charging Duration (Hours)</label>
                <input
                    id="hours"
                    type="number"
                    className="form-input"
                    value={hours}
                    onChange={(e) => setHours(e.target.value)}
                    min="1"
                    max="6"
                    required
                    placeholder="Enter hours (1-6)"
                />
                {error && <div className="alert alert-error" style={{ marginTop: '8px' }}>{error}</div>}

                <button type="submit" className="btn" disabled={isLoading} style={{ marginTop: '16px' }}>
                    {isLoading ? 'Calculating...' : 'Find Best Time'}
                </button>
            </form>
        </div>
    );
}
