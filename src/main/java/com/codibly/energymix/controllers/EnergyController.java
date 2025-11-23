package com.codibly.energymix.controllers;

import com.codibly.energymix.domain.dto.DailyEnergyMixDto;
import com.codibly.energymix.services.EnergyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping(path = "energyMixAverage")
    public ResponseEntity<List< DailyEnergyMixDto >> energyMix() {
        List<DailyEnergyMixDto> report = energyService.getEnergyMixForThreeDays();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
