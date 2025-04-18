import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

// ----------------------------------------------------------------------

export function FormHead({ sx, icon, title, description, ...other }) {
  return (
    <>
      {icon && (
        <Box component="span" sx={{ mb: 3, mx: 'auto', display: 'inline-flex' }}>
          {icon}
        </Box>
      )}

      <Box
        sx={[
          () => ({
            mb: 5,
            gap: 1.5,
            display: 'flex',
            textAlign: 'center',
            whiteSpace: 'pre-line',
            flexDirection: 'column',
          }),
          ...(Array.isArray(sx) ? sx : [sx]),
        ]}
        {...other}
      >
        <Typography variant="h5">{title}</Typography>

        {description && (
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {description}
          </Typography>
        )}
      </Box>
    </>
  );
}
