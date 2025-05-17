package com.oh.oh.controller;

import com.oh.oh.entity.OhosRecord;
import com.oh.oh.service.OhosRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ohos")
@Tag(name = "Ohos Record", description = "管理温湿度记录")
@Slf4j
public class OhosRecordController {

    private final OhosRecordService service;

    public OhosRecordController(OhosRecordService service) {
        this.service = service;
    }

    @PostMapping("/record")
    @Operation(summary = "新增温湿度记录")
    public String addRecord(@RequestBody OhosRecord record) {
        service.saveRecord(record);
        return "Record saved";
    }

    @GetMapping("/all")
    @Operation(summary = "获取全部记录")
    public List<OhosRecord> getAll() {
        return service.getAllRecords();
    }

    @GetMapping("/temperatures")
    @Operation(summary = "获取所有温度数据")
    public List<Double> getTemperatures() {
        log.info("前端获取所有温度数据");
        return service.getAllTemperatures();
    }

    @GetMapping("/humidities")
    @Operation(summary = "获取所有湿度数据")
    public List<Double> getHumidities() {
        log.info("前端获取所有湿度数据");
        return service.getAllHumidities();
    }
}
