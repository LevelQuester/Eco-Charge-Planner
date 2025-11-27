import type { OptimalChargingWindowDto } from '../api/energy';

export default function ResultsDisplay({ result }: { result: OptimalChargingWindowDto }) {

    const formatUtcTime = (iso: string) => {
        return new Date(iso).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            timeZone: 'UTC'
        });
    };

    const formatUtcDate = (iso: string) => {
        return new Date(iso).toLocaleDateString([], {
            timeZone: 'UTC'
        });
    };

    return (
        <div className="card results-container">
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '16px' }}>
                <h2 style={{ margin: 0, color: 'var(--success-color)' }}>Optimal Charging Window Found!</h2>
            </div>

            <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '16px', marginBottom: 0 }}>
                <div className="result-item">
                    <span className="result-label">Date (UTC)</span>
                    <div className="result-value">

                        {formatUtcDate(result.startTime)}
                    </div>
                </div>

                <div className="result-item">
                    <span className="result-label">Time Window (UTC)</span>
                    <div className="result-value">
                        {formatUtcTime(result.startTime)} - {formatUtcTime(result.endTime)}
                    </div>
                </div>

                <div className="result-item">
                    <span className="result-label">Average Clean Energy</span>
                    <div style={{ marginTop: '4px' }}>
                        <span className="chip">{result.cleanEnergyPercentage.toFixed(1)}%</span>
                    </div>
                </div>
            </div>
        </div>
    );
}