import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect, lazy, Suspense } from 'react'
import { ConfigProvider, App as AntdApp, Spin } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { MessageProvider } from './contexts/MessageContext'
import { ParamProvider } from './contexts/ParamContext'
import { CacheManager } from './utils/api'
import { antdTheme } from './theme'
import MainLayout from './components/MainLayout'
import ProtectedRoute from './components/ProtectedRoute'
import './App.css'

const Login = lazy(() => import('./components/Login'))
const Dashboard = lazy(() => import('./components/Dashboard'))
const RecruitmentRequestList = lazy(() => import('./components/RecruitmentRequestList'))
const RecruitmentRequestForm = lazy(() => import('./components/RecruitmentRequestForm'))
const ApprovalManagement = lazy(() => import('./components/ApprovalManagement'))
const PositionPublishing = lazy(() => import('./components/PositionPublishing'))
const ResumeSubmission = lazy(() => import('./components/ResumeSubmission'))
const ResumeScreening = lazy(() => import('./components/ResumeScreening'))
const InterviewScheduling = lazy(() => import('./components/InterviewScheduling'))
const OfferManagement = lazy(() => import('./components/OfferManagement'))
const UserManagement = lazy(() => import('./components/UserManagement'))
const MessageManagement = lazy(() => import('./components/MessageManagement'))
const RoleManagement = lazy(() => import('./components/RoleManagement'))
const SupplierManagement = lazy(() => import('./components/SupplierManagement'))
const StaffingManagement = lazy(() => import('./components/StaffingManagement'))
const WorkflowCenter = lazy(() => import('./components/WorkflowCenter'))

function PageFallback() {
  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
      <Spin size="large" />
    </div>
  )
}

function AppContent() {
  const { user } = useAuth();
  const userId = user?.userId?.toString() || null;

  return (
    <MessageProvider userId={userId}>
      <ParamProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Suspense fallback={<PageFallback />}><Login /></Suspense>} />
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/dashboard" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><Dashboard /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><RecruitmentRequestList /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request/new" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><RecruitmentRequestForm /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/recruitment-request/edit/:id" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><RecruitmentRequestForm /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/approval-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><ApprovalManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/position-publishing" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><PositionPublishing /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/resume-submission" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><ResumeSubmission /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/resume-screening" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><ResumeScreening /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/interview-scheduling" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><InterviewScheduling /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/offer-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><OfferManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/user-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><UserManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/role-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><RoleManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/supplier-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><SupplierManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/staffing-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><StaffingManagement /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/workflow-center" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><WorkflowCenter /></Suspense></MainLayout></ProtectedRoute>} />
            <Route path="/message-management" element={<ProtectedRoute><MainLayout><Suspense fallback={<PageFallback />}><MessageManagement /></Suspense></MainLayout></ProtectedRoute>} />
          </Routes>
        </Router>
      </ParamProvider>
    </MessageProvider>
  );
}

function App() {
  const [cacheCleared, setCacheCleared] = useState(false);
  
  useEffect(() => {
    if (!cacheCleared) {
      setTimeout(() => {
        CacheManager.clearAllApiCache();
        setCacheCleared(true);
      }, 0);
    }
  }, [cacheCleared]);
  
  return (
    <ConfigProvider locale={zhCN} theme={antdTheme}>
      <AntdApp>
        <AuthProvider>
          <AppContent />
        </AuthProvider>
      </AntdApp>
    </ConfigProvider>
  )
}

export default App
