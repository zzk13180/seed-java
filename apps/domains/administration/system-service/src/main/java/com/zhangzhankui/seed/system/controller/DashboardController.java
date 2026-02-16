package com.zhangzhankui.seed.system.controller;

import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.system.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "仪表盘统计")
@RestController
@RequestMapping("/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public ApiResult<Map<String, Long>> stats() {
        return ApiResult.ok(dashboardService.getStats());
    }
}
