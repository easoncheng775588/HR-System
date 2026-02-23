package com.hr.controller;

import com.hr.entity.SysParam;
import com.hr.service.SysParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统参数Controller
 */
@RestController
@RequestMapping("/api/sys/params")
@CrossOrigin(origins = "*")
public class SysParamController {
    
    private static final Logger logger = LoggerFactory.getLogger(SysParamController.class);
    
    @Autowired
    private SysParamService sysParamService;
    
    /**
     * 根据ID查询参数
     * @param paramId 参数ID
     * @return 参数信息
     */
    @GetMapping("/{paramId}")
    public Map<String, Object> getParamById(@PathVariable String paramId) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据ID查询参数: paramId={}", paramId);
        try {
            SysParam param = sysParamService.getParamById(paramId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", param);
        } catch (Exception e) {
            logger.error("根据ID查询参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 根据参数类型查询参数列表
     * @param paramType 参数类型
     * @return 参数列表
     */
    @GetMapping("/type/{paramType}")
    public Map<String, Object> getParamsByType(@PathVariable String paramType) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据参数类型查询参数列表: paramType={}", paramType);
        try {
            List<SysParam> params = sysParamService.getParamsByType(paramType);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", params);
        } catch (Exception e) {
            logger.error("根据参数类型查询参数列表失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 根据参数编码查询参数列表
     * @param paramCode 参数编码
     * @return 参数列表
     */
    @GetMapping("/code/{paramCode}")
    public Map<String, Object> getParamsByCode(@PathVariable String paramCode) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据参数编码查询参数列表: paramCode={}", paramCode);
        try {
            List<SysParam> params = sysParamService.getParamsByCode(paramCode);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", params);
        } catch (Exception e) {
            logger.error("根据参数编码查询参数列表失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 查询所有参数
     * @return 参数列表
     */
    @GetMapping
    public Map<String, Object> getAllParams() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("查询所有参数");
        try {
            List<SysParam> params = sysParamService.getAllParams();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", params);
        } catch (Exception e) {
            logger.error("查询所有参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 查询启用的参数
     * @return 参数列表
     */
    @GetMapping("/active")
    public Map<String, Object> getActiveParams() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("查询启用的参数");
        try {
            List<SysParam> params = sysParamService.getActiveParams();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", params);
        } catch (Exception e) {
            logger.error("查询启用的参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 根据参数类型查询启用的参数
     * @param paramType 参数类型
     * @return 参数列表
     */
    @GetMapping("/active/type/{paramType}")
    public Map<String, Object> getActiveParamsByType(@PathVariable String paramType) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据参数类型查询启用的参数: paramType={}", paramType);
        try {
            List<SysParam> params = sysParamService.getActiveParamsByType(paramType);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", params);
        } catch (Exception e) {
            logger.error("根据参数类型查询启用的参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 保存参数
     * @param sysParam 系统参数
     * @return 是否保存成功
     */
    @PostMapping
    public Map<String, Object> saveParam(@RequestBody SysParam sysParam) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("保存参数: {}", sysParam.getParamId());
        try {
            boolean saveResult = sysParamService.saveParam(sysParam);
            if (saveResult) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "保存参数成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "保存参数失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            logger.error("保存参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "保存参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 更新参数
     * @param paramId 参数ID
     * @param sysParam 系统参数
     * @return 是否更新成功
     */
    @PutMapping("/{paramId}")
    public Map<String, Object> updateParam(@PathVariable String paramId, @RequestBody SysParam sysParam) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("更新参数: {}", paramId);
        try {
            sysParam.setParamId(paramId);
            boolean updateResult = sysParamService.updateParam(sysParam);
            if (updateResult) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "更新参数成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "更新参数失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            logger.error("更新参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "更新参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    /**
     * 删除参数
     * @param paramId 参数ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{paramId}")
    public Map<String, Object> deleteParam(@PathVariable String paramId) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("删除参数: {}", paramId);
        try {
            boolean deleteResult = sysParamService.deleteParam(paramId);
            if (deleteResult) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "删除参数成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "删除参数失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            logger.error("删除参数失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "删除参数失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}

/**
 * 系统参数Controller
 * 提供获取可用编制数量的接口
 */
@RestController
@RequestMapping("/api/sys-param")
@CrossOrigin(origins = "*")
class SysParamQuotaController {
    
    private static final Logger logger = LoggerFactory.getLogger(SysParamQuotaController.class);
    
    @Autowired
    private SysParamService sysParamService;
    
    /**
     * 获取可用编制数量
     * @return 可用编制数量
     */
    @GetMapping("/quota")
    public Map<String, Object> getAvailableQuota() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("获取可用编制数量");
        try {
            // 从系统参数中获取可用编制数量
            List<SysParam> params = sysParamService.getParamsByCode("AVAILABLE_QUOTA");
            int availableQuota = 0;
            if (!params.isEmpty()) {
                try {
                    availableQuota = Integer.parseInt(params.get(0).getParamValue());
                } catch (NumberFormatException e) {
                    logger.error("编制数量格式错误: {}", e.getMessage());
                }
            }
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", availableQuota);
        } catch (Exception e) {
            logger.error("获取可用编制数量失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取可用编制数量失败: " + e.getMessage());
            result.put("body", 0);
        }
        return result;
    }
}
