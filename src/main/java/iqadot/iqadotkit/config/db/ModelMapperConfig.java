package iqadot.iqadotkit.config.db;

import org.modelmapper.*;
import org.springframework.context.annotation.*;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

}
