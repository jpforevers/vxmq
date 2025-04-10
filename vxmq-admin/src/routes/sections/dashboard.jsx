import { Outlet } from 'react-router';
import { lazy, Suspense } from 'react';

import { CONFIG } from 'src/global-config';
import { DashboardLayout } from 'src/layouts/dashboard';

import { LoadingScreen } from 'src/components/loading-screen';

import { AuthGuard } from 'src/auth/guard';

import { usePathname } from '../hooks';

// ----------------------------------------------------------------------

const IndexPage = lazy(() => import('src/pages/dashboard/monitor'));
const PageClients = lazy(() => import('src/pages/dashboard/clients'));
const PageTopics = lazy(() => import('src/pages/dashboard/topics'));
const PageSubscriptions = lazy(() => import('src/pages/dashboard/subscriptions'));
const PageRuleEngineStatistics = lazy(() => import('src/pages/dashboard/rule-engine/statistics'));
const PageRuleEngineRules = lazy(() => import('src/pages/dashboard/rule-engine/rules'));

// ----------------------------------------------------------------------

function SuspenseOutlet() {
  const pathname = usePathname();
  return (
    <Suspense key={pathname} fallback={<LoadingScreen />}>
      <Outlet />
    </Suspense>
  );
}

const dashboardLayout = () => (
  <DashboardLayout>
    <SuspenseOutlet />
  </DashboardLayout>
);

export const dashboardRoutes = [
  {
    path: 'dashboard',
    element: CONFIG.auth.skip ? dashboardLayout() : <AuthGuard>{dashboardLayout()}</AuthGuard>,
    children: [
      { element: <IndexPage />, index: true },
      { path: 'clients', element: <PageClients /> },
      { path: 'topics', element: <PageTopics /> },
      { path: 'subscriptions', element: <PageSubscriptions /> },
      {
        path: 'rule-engine',
        children: [
          { element: <PageRuleEngineStatistics />, index: true },
          { path: 'rules', element: <PageRuleEngineRules /> }
        ],
      },
    ],
  },
];
