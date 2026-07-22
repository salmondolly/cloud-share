import { Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Login from './pages/Login.jsx'
import MyFiles from './pages/MyFiles.jsx'
import NotFound from './pages/NotFound.jsx'
import Plans from './pages/Plans.jsx'
import Register from './pages/Register.jsx'
import SharedFile from './pages/SharedFile.jsx'

function PrivateLayout({ children }) {
  return (
    <ProtectedRoute>
      <Navbar />
      <main className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-8">{children}</main>
    </ProtectedRoute>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/share/:token" element={<SharedFile />} />
      <Route path="/dashboard" element={<PrivateLayout><Dashboard /></PrivateLayout>} />
      <Route path="/files" element={<PrivateLayout><MyFiles /></PrivateLayout>} />
      <Route path="/plans" element={<PrivateLayout><Plans /></PrivateLayout>} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
