import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';

import { Iconify } from 'src/components/iconify';

// ----------------------------------------------------------------------

export function FormSocials({
  sx,
  signInWithGoogle,
  singInWithGithub,
  signInWithTwitter,
  ...other
}) {
  return (
    <Box
      sx={[
        {
          gap: 1.5,
          display: 'flex',
          justifyContent: 'center',
        },
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
      {...other}
    >
      <IconButton color="inherit" onClick={signInWithGoogle}>
        <Iconify width={22} icon="socials:google" />
      </IconButton>
      <IconButton color="inherit" onClick={singInWithGithub}>
        <Iconify width={22} icon="socials:github" />
      </IconButton>
      <IconButton color="inherit" onClick={signInWithTwitter}>
        <Iconify width={22} icon="socials:twitter" />
      </IconButton>
    </Box>
  );
}
