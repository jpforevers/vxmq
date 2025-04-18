import { useState, useEffect } from 'react';

import { useSearchParams } from 'src/routes/hooks';

import { CONFIG } from 'src/global-config';

import { SplashScreen } from 'src/components/loading-screen';

import { useAuthContext } from '../hooks';

// ----------------------------------------------------------------------

export function GuestGuard({ children }) {
  const { loading, authenticated } = useAuthContext();

  const searchParams = useSearchParams();
  const returnTo = searchParams.get('returnTo') || CONFIG.auth.redirectPath;

  const [isChecking, setIsChecking] = useState(true);

  const checkPermissions = async () => {
    if (loading) {
      return;
    }

    if (authenticated) {
      // Redirect authenticated users to the returnTo path
      // Using `window.location.href` instead of `router.replace` to avoid unnecessary re-rendering
      // that might be caused by the AuthGuard component
      window.location.href = returnTo;
      return;
    }

    setIsChecking(false);
  };

  useEffect(() => {
    checkPermissions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authenticated, loading]);

  if (isChecking) {
    return <SplashScreen />;
  }

  return <>{children}</>;
}
