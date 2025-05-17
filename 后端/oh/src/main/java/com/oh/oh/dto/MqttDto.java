package com.oh.oh.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class MqttDto{
    private int signal;
    private double value;
}
