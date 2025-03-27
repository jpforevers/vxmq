import Divider from '@mui/material/Divider';

// ----------------------------------------------------------------------

export function FormDivider({ sx, label = 'OR' }) {
  return (
    <Divider
      sx={[
        () => ({
          my: 3,
          typography: 'overline',
          color: 'text.disabled',
          '&::before, :after': { borderTopStyle: 'dashed' },
        }),
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
    >
      {label}
    </Divider>
  );
}
