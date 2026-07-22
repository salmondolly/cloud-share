import { useEffect, useState } from 'react'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import PageLoader from '../components/PageLoader.jsx'
import PlanCard from '../components/PlanCard.jsx'
import { useAuth } from '../context/AuthContext.jsx'

function loadRazorpayCheckout() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true)
      return
    }
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.async = true
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export default function Plans() {
  const { user, refreshUser } = useAuth()
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [busyPlan, setBusyPlan] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    apiClient.get('/plans')
      .then((response) => setPlans(response.data))
      .catch((loadError) => setError(apiErrorMessage(loadError, 'Could not load plans')))
      .finally(() => setLoading(false))
  }, [])

  const buyPlan = async (plan) => {
    setBusyPlan(plan.planType)
    setError(null)
    setMessage(null)
    try {
      const checkoutLoaded = await loadRazorpayCheckout()
      if (!checkoutLoaded) throw new Error('Razorpay Checkout could not be loaded')

      const orderResponse = await apiClient.post('/payments/create-order', { planType: plan.planType })
      const order = orderResponse.data

      const checkout = new window.Razorpay({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'Cloud Share',
        description: `${plan.displayName} credit pack`,
        order_id: order.orderId,
        prefill: {
          name: user.name,
          email: user.email,
        },
        theme: { color: '#2563eb' },
        handler: async (response) => {
          try {
            await apiClient.post('/payments/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            await refreshUser()
            setMessage(`${plan.displayName} activated and credits added.`)
          } catch (verifyError) {
            setError(apiErrorMessage(verifyError, 'Payment verification failed'))
          } finally {
            setBusyPlan(null)
          }
        },
        modal: {
          ondismiss: () => setBusyPlan(null),
        },
      })
      checkout.on('payment.failed', (response) => {
        setError(response.error?.description || 'Payment failed')
        setBusyPlan(null)
      })
      checkout.open()
    } catch (purchaseError) {
      setError(apiErrorMessage(purchaseError, 'Could not start payment'))
      setBusyPlan(null)
    }
  }

  if (loading) return <PageLoader label="Loading plans…" />

  return (
    <div className="space-y-7">
      <section>
        <p className="text-sm font-bold uppercase tracking-widest text-blue-700">Subscriptions</p>
        <h1 className="page-title mt-2">Choose a plan</h1>
        <p className="mt-2 max-w-2xl text-slate-600">
          Paid plans are implemented as one-time credit packs. Credits are added after backend signature verification.
        </p>
      </section>
      <Alert type="error">{error}</Alert>
      <Alert type="success">{message}</Alert>
      <section className="grid gap-5 lg:grid-cols-3">
        {plans.map((plan) => (
          <PlanCard
            key={plan.planType}
            plan={plan}
            currentPlan={user.planType}
            busy={busyPlan === plan.planType}
            onBuy={buyPlan}
          />
        ))}
      </section>
      <section className="rounded-2xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-900">
        <p className="font-bold">Payment testing</p>
        <p className="mt-1">Use Razorpay test-mode credentials and test payment methods. Never put the key secret in frontend code.</p>
      </section>
    </div>
  )
}
