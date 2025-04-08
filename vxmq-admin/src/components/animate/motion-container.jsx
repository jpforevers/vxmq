import { m } from 'framer-motion';

import Box from '@mui/material/Box';

import { varContainer } from './variants';

// ----------------------------------------------------------------------

export function MotionContainer({ sx, animate, children, action = false, ...other }) {
  return (
    <Box
      component={m.div}
      variants={varContainer()}
      initial={action ? false : 'initial'}
      animate={action ? (animate ? 'animate' : 'exit') : 'animate'}
      exit={action ? undefined : 'exit'}
      sx={sx}
      {...other}
    >
      {children}
    </Box>
  );
}
