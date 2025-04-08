import { cloneElement } from 'react';
import { useBackToTop } from 'minimal-shared/hooks';

import Fab from '@mui/material/Fab';

import { Iconify } from '../iconify';

// ----------------------------------------------------------------------

export function BackToTopButton({
  sx,
  isDebounce,
  renderButton,
  scrollThreshold = '90%',
  ...other
}) {
  const { onBackToTop, isVisible } = useBackToTop(scrollThreshold, isDebounce);

  if (renderButton) {
    return cloneElement(renderButton(isVisible), {
      onClick: onBackToTop,
    });
  }

  return (
    <Fab
      aria-label="Back to top"
      onClick={onBackToTop}
      sx={[
        (theme) => ({
          width: 48,
          height: 48,
          position: 'fixed',
          transform: 'scale(0)',
          right: { xs: 24, md: 32 },
          bottom: { xs: 24, md: 32 },
          zIndex: theme.zIndex.speedDial,
          transition: theme.transitions.create(['transform']),
          ...(isVisible && { transform: 'scale(1)' }),
        }),
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      {...other}
    >
      <Iconify width={24} icon="solar:double-alt-arrow-up-bold-duotone" />
    </Fab>
  );
}
