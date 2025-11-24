import ChargingWindowForm from './components/ChargingWindowForm';
import EnergyMixChart from './components/EnergyMixChart';
import ResultsDisplay from './components/ResultsDisplay';
import { useEnergyData } from './hooks/useEnergyData';

function App() {
    const {
        energyMix,
        optimalWindow,
        loadingMix,
        loadingWindow,
        error,
        findOptimalWindow
    } = useEnergyData();

    return (
        <div className="container">
            <div className="header">
                <h1>Eco Charge Planner</h1>
                <h2>Optimize your energy consumption and save money</h2>
            </div>

            {error && (
                <div className="alert alert-error">
                    {error}
                </div>
            )}

            <div className="charts-row">
                {loadingMix ? (
                    <div style={{ width: '100%', textAlign: 'center', alignSelf: 'center' }}>
                        <div className="loading-spinner"></div>
                    </div>
                ) : energyMix && energyMix.length > 0 ? (
                    energyMix.map((mix, index) => (
                        <div key={index} className="chart-wrapper">
                            <EnergyMixChart data={mix} />
                        </div>
                    ))
                ) : (
                    <div style={{ width: '100%' }}>
                        <div className="alert alert-info">No energy mix data available.</div>
                    </div>
                )}
            </div>

            <div className="bottom-row">
                <div className="bottom-col">
                    <ChargingWindowForm onSubmit={findOptimalWindow} isLoading={loadingWindow} />
                </div>

                <div className="bottom-col">
                    {optimalWindow ? (
                        <ResultsDisplay result={optimalWindow} />
                    ) : (
                        <div className="card" style={{ alignItems: 'center', opacity: 0.7 }}>
                            <p style={{ fontSize: '1rem', margin: 0 }}>
                                Enter duration to see optimal window
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default App;
