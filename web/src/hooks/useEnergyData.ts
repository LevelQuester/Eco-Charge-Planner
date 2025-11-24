import { useState, useEffect } from 'react';
import { getEnergyMixAverage, getOptimalChargingWindow } from '../api/energy';
import type { DailyEnergyMixDto, OptimalChargingWindowDto } from '../api/energy';

export const useEnergyData = () => {
    const [energyMix, setEnergyMix] = useState<DailyEnergyMixDto[] | null>(null);
    const [optimalWindow, setOptimalWindow] = useState<OptimalChargingWindowDto | null>(null);
    const [loadingMix, setLoadingMix] = useState(true);
    const [loadingWindow, setLoadingWindow] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getEnergyMixAverage()
            .then(setEnergyMix)
            .catch(() => setError("Failed to load energy mix data."))
            .finally(() => setLoadingMix(false));
    }, []);

    const findOptimalWindow = async (hours: number) => {
        setLoadingWindow(true);
        setOptimalWindow(null);
        setError(null);
        try {
            setOptimalWindow(await getOptimalChargingWindow(hours));
        } catch {
            setError("Failed to calculate optimal charging window.");
        } finally {
            setLoadingWindow(false);
        }
    };

    return { energyMix, optimalWindow, loadingMix, loadingWindow, error, findOptimalWindow };
};
