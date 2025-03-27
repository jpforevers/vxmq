import { styled } from '@mui/material/styles';
import Collapse from '@mui/material/Collapse';

import { navSectionClasses } from '../styles';

// ----------------------------------------------------------------------

export const NavCollapse = styled(Collapse, {
  shouldForwardProp: (prop) => !['depth', 'sx'].includes(prop),
})(({ depth, theme }) => {
  const verticalLineStyles = {
    top: 0,
    left: 0,
    width: '2px',
    content: '""',
    position: 'absolute',
    backgroundColor: 'var(--nav-bullet-light-color)',
    bottom: 'calc(var(--nav-item-sub-height) - 2px - var(--nav-bullet-size) / 2)',
    ...theme.applyStyles('dark', {
      backgroundColor: 'var(--nav-bullet-dark-color)',
    }),
  };

  return {
    ...(depth && {
      ...(depth + 1 !== 1 && {
        paddingLeft: 'calc(var(--nav-item-pl) + var(--nav-icon-size) / 2)',
        [`& .${navSectionClasses.ul}`]: {
          position: 'relative',
          paddingLeft: 'var(--nav-bullet-size)',
          '&::before': verticalLineStyles,
        },
      }),
    }),
  };
});
