
package com.fastbj.activeMQ.service;

import com.alibaba.nacos.console.paramcheck.ConsoleDefaultHttpParamExtractor;
import com.alibaba.nacos.core.cluster.health.ModuleHealthCheckerHolder;
import com.alibaba.nacos.core.cluster.health.ReadinessResult;
import com.alibaba.nacos.core.controller.compatibility.Compatibility;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ApiType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Controller.
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 */
@RestController("consoleHealth")
@RequestMapping("/v1/console/health")
@ExtractorManager.Extractor(httpExtractor = ConsoleDefaultHttpParamExtractor.class)
public class HealthController {
    
    /**
     * Whether the Nacos is in broken states or not, and cannot recover except by being restarted.
     *
     * @return HTTP code equal to 200 indicates that Nacos is in right states. HTTP code equal to 500 indicates that
     * Nacos is in broken states.
     */
    @GetMapping("/liveness")
    @Compatibility(apiType = ApiType.CONSOLE_API, alternatives = "GET ${contextPath:nacos}/v3/console/health/liveness")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok().body("OK");
    }
    
    /**
     * Ready to receive the request or not.
     *
     * @return HTTP code equal to 200 indicates that Nacos is ready. HTTP code equal to 500 indicates that Nacos is not
     * ready.
     */
    @GetMapping("/readiness")
    @Compatibility(apiType = ApiType.CONSOLE_API, alternatives = "GET ${contextPath:nacos}/v3/console/health/readiness")
    public ResponseEntity<String> readiness(HttpServletRequest request) {
        ReadinessResult result = ModuleHealthCheckerHolder.getInstance().checkReadiness();
        if (result.isSuccess()) {
            return ResponseEntity.ok().body("OK");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getResultMessage());
    }
    
}
