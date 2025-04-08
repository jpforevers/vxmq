import { Fragment } from 'react';

import Portal from '@mui/material/Portal';
import { styled } from '@mui/material/styles';
import LinearProgress from '@mui/material/LinearProgress';

// ----------------------------------------------------------------------

export function LoadingScreen({ portal, sx, ...other }) {
  const PortalWrapper = portal ? Portal : Fragment;

  return (
    <PortalWrapper>
      <LoadingContent sx={sx} {...other}>
        <LinearProgress color="inherit" sx={{ width: 1, maxWidth: 360 }} />
      </LoadingContent>
    </PortalWrapper>
  );
}

// ----------------------------------------------------------------------

const LoadingContent = styled('div')(({ theme }) => ({
  flexGrow: 1,
  width: '100%',
  display: 'flex',
  minHeight: '100%',
  alignItems: 'center',
  justifyContent: 'center',
  paddingLeft: theme.spacing(5),
  paddingRight: theme.spacing(5),
}));
