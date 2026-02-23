package com.hr.service;

import com.hr.entity.SysParam;
import java.util.List;

/**
 * 系统参数Service接口
 */
public interface SysParamService {
    /**
     * 根据ID查询参数
     * @param paramId 参数ID
     * @return 系统参数
     */
    SysParam getParamById(String paramId);
    
    /**
     * 根据参数类型查询参数列表
     * @param paramType 参数类型
     * @return 参数列表
     */
    List<SysParam> getParamsByType(String paramType);
    
    /**
     * 根据参数编码查询参数列表
     * @param paramCode 参数编码
     * @return 参数列表
     */
    List<SysParam> getParamsByCode(String paramCode);
    
    /**
     * 查询所有参数
     * @return 参数列表
     */
    List<SysParam> getAllParams();
    
    /**
     * 查询启用的参数
     * @return 参数列表
     */
    List<SysParam> getActiveParams();
    
    /**
     * 根据参数类型查询启用的参数
     * @param paramType 参数类型
     * @return 参数列表
     */
    List<SysParam> getActiveParamsByType(String paramType);
    
    /**
     * 保存参数
     * @param sysParam 系统参数
     * @return 是否保存成功
     */
    boolean saveParam(SysParam sysParam);
    
    /**
     * 更新参数
     * @param sysParam 系统参数
     * @return 是否更新成功
     */
    boolean updateParam(SysParam sysParam);
    
    /**
     * 删除参数
     * @param paramId 参数ID
     * @return 是否删除成功
     */
    boolean deleteParam(String paramId);
    
    /**
     * 批量获取参数类型
     * @param paramTypes 参数类型列表
     * @return 参数映射，key为参数类型，value为该类型的参数列表
     */
    java.util.Map<String, List<SysParam>> getParamsByTypes(List<String> paramTypes);
}
