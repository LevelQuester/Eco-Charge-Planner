import { PieChart } from '@mui/x-charts/PieChart';
import type { DailyEnergyMixDto } from '../api/energy';

const FUEL_COLORS: Record<string, string> = {
    'solar': '#FFCE56',
    'wind': '#4D73FC',
    'hydro': '#36A2EB',
    'nuclear': '#9966FF',
    'gas': '#FF6384',
    'coal': '#4D5360',
    'biomass': '#4BC0C0',
    'imports': '#FF9F40',
    'other': '#C9CBCF'
};

const getColor = (fuel: string) => FUEL_COLORS[fuel.toLowerCase()] || '#9E9E9E';

const formatUtcDate = (iso: string) => {
    return new Date(iso).toLocaleDateString([], {
        timeZone: 'UTC'
    });
};

export default function EnergyMixChart({ data }: { data: DailyEnergyMixDto }) {
    return (
        <div className="card chart-container">
            <h3>{formatUtcDate(data.date)}</h3>
            <div style={{ color: 'var(--success-color)', fontWeight: 'bold', marginBottom: '16px' }}>
                Clean Energy: {data.cleanEnergyPercent.toFixed(1)}%
            </div>
            <div style={{ flex: 1, width: '100%', display: 'flex', justifyContent: 'center', minHeight: 0 }}>
                <PieChart
                    series={[{
                        data: Object.entries(data.fuelMix).map(([label, value], id) => ({
                            id,
                            value,
                            label,
                            color: getColor(label)
                        })),
                        innerRadius: 40, outerRadius: 90, paddingAngle: 2, cornerRadius: 4
                    }]}
                    slotProps={{ legend: { position: { vertical: 'bottom', horizontal: 'center' } } }}
                    margin={{ top: 10, bottom: 50, left: 10, right: 10 }}
                    height={250}
                />
            </div>
        </div>
    );
}
