package org.id.bankspringbatch.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter @Setter  @AllArgsConstructor
public class StateAnalyticsConfig {

    public static Map<String, Double> state = new HashMap<>();
    static{
        state = new HashMap<>();
    }
}
