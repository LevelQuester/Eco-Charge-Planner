package com.codibly.energymix.services;


import com.codibly.energymix.domain.dto.DailyEnergyMixDto;

import java.util.List;

public interface EnergyService {

    List<DailyEnergyMixDto> getEnergyMixForThreeDays();
}
