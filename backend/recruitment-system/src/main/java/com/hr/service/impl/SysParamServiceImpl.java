package com.hr.service.impl;

import com.hr.entity.SysParam;
import com.hr.mapper.SysParamMapper;
import com.hr.service.SysParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统参数Service实现类
 */
@Service
public class SysParamServiceImpl implements SysParamService {
    
    private static final Logger logger = LoggerFactory.getLogger(SysParamServiceImpl.class);
    
    @Autowired
    private SysParamMapper sysParamMapper;
    
    @Override
    public SysParam getParamById(String paramId) {
        logger.debug("根据ID查询参数: paramId={}", paramId);
        try {
            return sysParamMapper.selectByPrimaryKey(paramId);
        } catch (Exception e) {
            logger.error("根据ID查询参数失败: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<SysParam> getParamsByType(String paramType) {
        logger.debug("根据参数类型查询参数列表: paramType={}", paramType);
        try {
            return sysParamMapper.selectByParamType(paramType);
        } catch (Exception e) {
            logger.error("根据参数类型查询参数列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysParam> getParamsByCode(String paramCode) {
        logger.debug("根据参数编码查询参数列表: paramCode={}", paramCode);
        try {
            return sysParamMapper.selectByParamCode(paramCode);
        } catch (Exception e) {
            logger.error("根据参数编码查询参数列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysParam> getAllParams() {
        logger.debug("查询所有参数");
        try {
            return sysParamMapper.selectAll();
        } catch (Exception e) {
            logger.error("查询所有参数失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysParam> getActiveParams() {
        logger.debug("查询启用的参数");
        try {
            return sysParamMapper.selectActive();
        } catch (Exception e) {
            logger.error("查询启用的参数失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<SysParam> getActiveParamsByType(String paramType) {
        logger.debug("根据参数类型查询启用的参数: paramType={}", paramType);
        try {
            return sysParamMapper.selectActiveByParamType(paramType);
        } catch (Exception e) {
            logger.error("根据参数类型查询启用的参数失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean saveParam(SysParam sysParam) {
        logger.debug("保存参数: {}", sysParam.getParamId());
        try {
            Date now = new Date();
            if (sysParam.getCreateTime() == null) {
                sysParam.setCreateTime(now);
            }
            if (sysParam.getUpdateTime() == null) {
                sysParam.setUpdateTime(now);
            }
            if (sysParam.getUpdateUserId() == null && sysParam.getCreateUserId() != null) {
                sysParam.setUpdateUserId(sysParam.getCreateUserId());
            }
            if (sysParam.getUpdateUserName() == null && sysParam.getCreateUserName() != null) {
                sysParam.setUpdateUserName(sysParam.getCreateUserName());
            }
            int result = sysParamMapper.insert(sysParam);
            return result > 0;
        } catch (Exception e) {
            logger.error("保存参数失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateParam(SysParam sysParam) {
        logger.debug("更新参数: {}", sysParam.getParamId());
        try {
            int result = sysParamMapper.updateByPrimaryKey(sysParam);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新参数失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteParam(String paramId) {
        logger.debug("删除参数: {}", paramId);
        try {
            int result = sysParamMapper.deleteByPrimaryKey(paramId);
            return result > 0;
        } catch (Exception e) {
            logger.error("删除参数失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, List<SysParam>> getParamsByTypes(List<String> paramTypes) {
        logger.debug("批量获取参数类型: {}", paramTypes);
        try {
            Map<String, List<SysParam>> resultMap = new HashMap<>();
            for (String paramType : paramTypes) {
                List<SysParam> params = sysParamMapper.selectActiveByParamType(paramType);
                resultMap.put(paramType, params);
            }
            return resultMap;
        } catch (Exception e) {
            logger.error("批量获取参数类型失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
