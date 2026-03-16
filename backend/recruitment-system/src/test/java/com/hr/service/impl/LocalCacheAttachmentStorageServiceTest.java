package com.hr.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalCacheAttachmentStorageServiceTest {

    private final LocalCacheAttachmentStorageService service = new LocalCacheAttachmentStorageService();

    @Test
    void interviewEvaluationOnlyAcceptsExcelAttachments() {
        MockMultipartFile pdfFile = new MockMultipartFile(
            "file",
            "evaluation.pdf",
            "application/pdf",
            "fake-pdf".getBytes()
        );

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> service.store("interview_evaluation", pdfFile)
        );

        assertEquals("面试评价附件仅支持 Excel 格式", exception.getMessage());
    }
}
