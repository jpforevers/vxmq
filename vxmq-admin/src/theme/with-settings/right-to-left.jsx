import { useEffect } from 'react';
import createCache from '@emotion/cache';
import rtlPlugin from 'stylis-plugin-rtl';
import { CacheProvider } from '@emotion/react';

// ----------------------------------------------------------------------

export function Rtl({ children, direction }) {
  const cacheRtl = createCache({
    key: 'rtl',
    prepend: true,
    stylisPlugins: [rtlPlugin],
  });

  useEffect(() => {
    document.dir = direction;
  }, [direction]);

  if (direction === 'rtl') {
    return <CacheProvider value={cacheRtl}>{children}</CacheProvider>;
  }

  return <>{children}</>;
}
