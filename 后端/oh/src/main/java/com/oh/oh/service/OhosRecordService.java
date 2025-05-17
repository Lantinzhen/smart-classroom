package com.oh.oh.service;

import com.oh.oh.entity.OhosRecord;
import com.oh.oh.mapper.OhosRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OhosRecordService {

    private final OhosRecordMapper mapper;

    public OhosRecordService(OhosRecordMapper mapper) {
        this.mapper = mapper;
    }

    public int saveRecord(OhosRecord record) {
        log.info("Saving record: {}", record);
        return mapper.insertRecord(record);
    }

    public List<OhosRecord> getAllRecords() {
        return mapper.findAll();
    }

    public List<Double> getAllTemperatures() {
        return mapper.findAllTemperatures();
    }

    public List<Double> getAllHumidities() {
        return mapper.findAllHumidities();
    }
}
