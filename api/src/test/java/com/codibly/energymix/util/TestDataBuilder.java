package com.codibly.energymix.util;

import com.codibly.energymix.domain.dto.GenerationItemDto;
import com.codibly.energymix.domain.dto.GenerationMixDto;

import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {

    public static class GenerationItemBuilder {
        private String from;
        private String to;
        private final List<GenerationMixDto> mix = new ArrayList<>();

        public GenerationItemBuilder from(String from) {
            this.from = from;
            return this;
        }

        public GenerationItemBuilder to(String to) {
            this.to = to;
            return this;
        }

        public GenerationItemBuilder withMix(String fuel, double percentage) {
            this.mix.add(new GenerationMixDto(fuel, percentage));
            return this;
        }

        public GenerationItemDto build() {
            GenerationItemDto item = new GenerationItemDto();
            item.setFrom(from);
            item.setTo(to);
            item.setGenerationMix(mix);
            return item;
        }
    }

    public static GenerationItemBuilder generationItem() {
        return new GenerationItemBuilder();
    }
}
