package com.codibly.energymix.services;


import com.codibly.energymix.domain.dto.DailyEnergyMixDto;
import com.codibly.energymix.domain.dto.OptimalChargingWindowDto;

import java.util.List;

public interface EnergyService {

    List<DailyEnergyMixDto> getEnergyMixForThreeDays();

    OptimalChargingWindowDto getOptimalChargingWindow(int hours);
}
