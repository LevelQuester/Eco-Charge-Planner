import client from './client';

export interface DailyEnergyMixDto {
    date: string;
    cleanEnergyPercent: number;
    fuelMix: Record<string, number>;
}

export interface OptimalChargingWindowDto {
    startTime: string;
    endTime: string;
    cleanEnergyPercentage: number;
}

export const getEnergyMixAverage = async (): Promise<DailyEnergyMixDto[]> => {
    const response = await client.get<DailyEnergyMixDto[]>('/energyMixAverage');
    return response.data;
};

export const getOptimalChargingWindow = async (hours: number): Promise<OptimalChargingWindowDto> => {
    const response = await client.get<OptimalChargingWindowDto>('/optimalChargingWindow', {
        params: { hours },
    });
    return response.data;
};
