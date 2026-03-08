package com.hr.service.impl;

import com.hr.entity.OrgUnit;
import com.hr.mapper.OrgUnitMapper;
import com.hr.service.OrgUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgUnitServiceImpl implements OrgUnitService {

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Override
    public List<OrgUnit> getActiveOrgUnits() {
        return orgUnitMapper.getActiveOrgUnits();
    }
}

