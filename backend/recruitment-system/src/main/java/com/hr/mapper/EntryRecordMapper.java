package com.hr.mapper;

import com.hr.entity.EntryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntryRecordMapper {
    int insert(EntryRecord entryRecord);

    int updateByPrimaryKey(EntryRecord entryRecord);

    EntryRecord selectByPrimaryKey(@Param("entryRecordId") Long entryRecordId);

    EntryRecord selectByEvaluationId(@Param("evaluationId") Long evaluationId);

    List<EntryRecord> selectAll();
}
