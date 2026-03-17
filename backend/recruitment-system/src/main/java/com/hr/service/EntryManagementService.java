package com.hr.service;

import com.hr.entity.EntryRecord;
import com.hr.entity.EntryRecordUpdateRequest;
import com.hr.entity.InterviewEvaluation;

import java.util.List;

public interface EntryManagementService {
    void createFromApprovedEvaluation(InterviewEvaluation evaluation);

    List<EntryRecord> getEntryList(String viewerId, String viewerRole);

    EntryRecord updateEntry(Long entryRecordId, EntryRecordUpdateRequest request);
}
