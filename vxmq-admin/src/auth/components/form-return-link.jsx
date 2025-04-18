import Link from '@mui/material/Link';

import { RouterLink } from 'src/routes/components';

import { Iconify } from 'src/components/iconify';

// ----------------------------------------------------------------------

export function FormReturnLink({ sx, href, label, icon, children, ...other }) {
  return (
    <Link
      component={RouterLink}
      href={href}
      color="inherit"
      variant="subtitle2"
      sx={[
        {
          mt: 3,
          gap: 0.5,
          mx: 'auto',
          alignItems: 'center',
          display: 'inline-flex',
        },
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      {...other}
    >
      {icon || <Iconify width={16} icon="eva:arrow-ios-back-fill" />}
      {label || 'Return to sign in'}
      {children}
    </Link>
  );
}
