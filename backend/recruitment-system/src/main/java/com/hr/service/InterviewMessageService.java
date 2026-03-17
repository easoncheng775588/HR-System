package com.hr.service;

public interface InterviewMessageService {
    void sendInterviewerConfirmedMessage(String candidateName, String interviewerName, String supplierHrUserId);

    void sendInterviewerConfirmedMessageToOutsourcingManagers(String candidateName, String interviewerName);

    void sendInterviewTimeConfirmedMessage(String candidateName, String interviewerUserId, String supplierHrUserId);

    void sendEvaluationRejectedMessage(String interviewerUserId);

    void sendEvaluationCompletedMessage(String interviewerUserId,
                                        String roomManagerUserId,
                                        String supplierHrUserId,
                                        String candidateName);
}
