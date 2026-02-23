package com.hr.mapper;

import com.hr.entity.SysParam;
import java.util.List;

/**
 * 系统参数Mapper接口
 */
public interface SysParamMapper {
    /**
     * 根据ID查询参数
     * @param paramId 参数ID
     * @return 系统参数
     */
    SysParam selectByPrimaryKey(String paramId);
    
    /**
     * 根据参数类型查询参数列表
     * @param paramType 参数类型
     * @return 参数列表
     */
    List<SysParam> selectByParamType(String paramType);
    
    /**
     * 根据参数编码查询参数列表
     * @param paramCode 参数编码
     * @return 参数列表
     */
    List<SysParam> selectByParamCode(String paramCode);
    
    /**
     * 查询所有参数
     * @return 参数列表
     */
    List<SysParam> selectAll();
    
    /**
     * 查询启用的参数
     * @return 参数列表
     */
    List<SysParam> selectActive();
    
    /**
     * 根据参数类型查询启用的参数
     * @param paramType 参数类型
     * @return 参数列表
     */
    List<SysParam> selectActiveByParamType(String paramType);
    
    /**
     * 插入参数
     * @param sysParam 系统参数
     * @return 影响行数
     */
    int insert(SysParam sysParam);
    
    /**
     * 更新参数
     * @param sysParam 系统参数
     * @return 影响行数
     */
    int updateByPrimaryKey(SysParam sysParam);
    
    /**
     * 删除参数
     * @param paramId 参数ID
     * @return 影响行数
     */
    int deleteByPrimaryKey(String paramId);
}
