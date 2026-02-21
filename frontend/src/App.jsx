import './App.css'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import { MessageProvider } from './contexts/MessageContext'
import Login from './components/Login'
import Dashboard from './components/Dashboard'
import MainLayout from './components/MainLayout'
import ProtectedRoute from './components/ProtectedRoute'
import RecruitmentRequestList from './components/RecruitmentRequestList'
import RecruitmentRequestForm from './components/RecruitmentRequestForm'
import ApprovalManagement from './components/ApprovalManagement'
import PositionPublishing from './components/PositionPublishing'
import ResumeSubmission from './components/ResumeSubmission'
import ResumeScreening from './components/ResumeScreening'
import InterviewScheduling from './components/InterviewScheduling'
import UserManagement from './components/UserManagement'
import MessageManagement from './components/MessageManagement'

function App() {
  return (
    <AuthProvider>
      <MessageProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/dashboard" element={<ProtectedRoute><MainLayout><Dashboard /></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request" element={<ProtectedRoute><MainLayout><RecruitmentRequestList /></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request/new" element={<ProtectedRoute><MainLayout><RecruitmentRequestForm /></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request/edit/:id" element={<ProtectedRoute><MainLayout><RecruitmentRequestForm /></MainLayout></ProtectedRoute>} />
            <Route path="/approval-management" element={<ProtectedRoute><MainLayout><ApprovalManagement /></MainLayout></ProtectedRoute>} />
            <Route path="/position-publishing" element={<ProtectedRoute><MainLayout><PositionPublishing /></MainLayout></ProtectedRoute>} />
            <Route path="/resume-submission" element={<ProtectedRoute><MainLayout><ResumeSubmission /></MainLayout></ProtectedRoute>} />
            <Route path="/resume-screening" element={<ProtectedRoute><MainLayout><ResumeScreening /></MainLayout></ProtectedRoute>} />
            <Route path="/interview-scheduling" element={<ProtectedRoute><MainLayout><InterviewScheduling /></MainLayout></ProtectedRoute>} />
            <Route path="/user-management" element={<ProtectedRoute><MainLayout><UserManagement /></MainLayout></ProtectedRoute>} />
            <Route path="/message-management" element={<ProtectedRoute><MainLayout><MessageManagement /></MainLayout></ProtectedRoute>} />
          </Routes>
        </Router>
      </MessageProvider>
    </AuthProvider>
  )
}

export default App